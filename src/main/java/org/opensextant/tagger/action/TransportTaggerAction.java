package org.opensextant.tagger.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.IntsRef;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.DefaultShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.TransportBroadcastOperationAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.routing.GroupShardsIterator;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.shard.service.InternalIndexShard;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.opensextant.solrtexttagger.TagClusterReducer;
import org.opensextant.solrtexttagger.Tagger;
import org.opensextant.tagger.interval.TagTree;

public class TransportTaggerAction
		extends
		TransportBroadcastOperationAction<TaggerRequest, TaggerResponse, ShardTaggerRequest, ShardTaggerResponse> {

	// the service that gets us access to lucene stuff
	private final IndicesService indicesService;

	@Inject
	public TransportTaggerAction(Settings settings, ThreadPool threadPool,
			ClusterService clusterService, TransportService transportService,
			IndicesService indicesService) {
		super(settings, TaggerAction.NAME, threadPool, clusterService,
				transportService);
		this.indicesService = indicesService;

	}

	@Override
	protected String executor() {
		return ThreadPool.Names.GENERIC;
	}

	@Override
	protected TaggerRequest newRequest() {
		return new TaggerRequest();
	}

	// build a Tagger response from all of the shard reponses
	@SuppressWarnings("rawtypes")
	@Override
	protected TaggerResponse newResponse(TaggerRequest request,
			AtomicReferenceArray shardsResponses, ClusterState clusterState) {
		// how many shards failed/succeeded and cause
		int successfulShards = 0;
		int failedShards = 0;
		List<ShardOperationFailedException> shardFailures = null;

		// an interval tree to store all returned Tags
		TagTree tree = new TagTree();

		// look at all the shard responses
		for (int i = 0; i < shardsResponses.length(); i++) {
			Object shardResponse = shardsResponses.get(i);
			// did shard fail?
			if (shardResponse instanceof BroadcastShardOperationFailedException) {
				failedShards++;
				if (shardFailures == null) {
					shardFailures = new ArrayList<ShardOperationFailedException>();
				}
				shardFailures
						.add(new DefaultShardOperationFailedException(
								(BroadcastShardOperationFailedException) shardResponse));
			} else {// shard succeeded
				successfulShards++;
				// check if response is what we expect
				if (shardResponse instanceof ShardTaggerResponse) {
					// cast to expected type
					ShardTaggerResponse shardresp = (ShardTaggerResponse) shardResponse;
					// add the tags from this shard to the tree
					tree.addTags(shardresp.getTags());
				} else {
					// what to do here? we got a response that succeeded but not
					// expected type
				}
			}// end of successful shard
		}// end of shard loop

		// build the tree
		tree.build();
		// reduce the total set of overlapping/interacting tags according to the
		// reduce mode
		tree.reduceTree(request.getReduceMode());

		// get all tags after reduction
		//List<Tag> tags = tree.getIncludedTags();
		List<Tag> tags = tree.getAllTags();

		// create a response including the accumulated and reduced tags
		return new TaggerResponse(shardsResponses.length(), successfulShards,
				failedShards, shardFailures, tags);
	}

	@Override
	protected ShardTaggerRequest newShardRequest() {
		return new ShardTaggerRequest();
	}

	@Override
	protected ShardTaggerRequest newShardRequest(int numShards,
			ShardRouting shard, TaggerRequest request) {
		return new ShardTaggerRequest(shard.index(), shard.id(), request);
	}

	@Override
	protected ShardTaggerResponse newShardResponse() {
		return new ShardTaggerResponse();
	}

	// core operation: tag text against a shards contents
	@Override
	protected ShardTaggerResponse shardOperation(ShardTaggerRequest request)
			throws ElasticsearchException {

		// get the tagger request from the shard request
		TaggerRequest tagRequest = request.getTagRequest();
		// get the type and field to use as the key to tag against
		String field = tagRequest.getField();
		// the type(s) to include in the response
		final Set<String> types = tagRequest.getType();
		// get the text to tag
		final String textToBeTagged = tagRequest.getTextToBeTagged();
		// get the tagging options
		boolean skipAltTokens = tagRequest.isSkipAltTokens();
		boolean ignoreStopWords = tagRequest.isIgnoreStopwords();
		final boolean idsOnly = tagRequest.isIdsOnly();
		final boolean addMatchText = tagRequest.isIncludeMatchText();

		// get the Reader associated with the requested index
		final InternalIndexShard indexShard = (InternalIndexShard) indicesService
				.indexServiceSafe(request.index()).shardSafe(request.shardId());
		Engine.Searcher searcher = indexShard.engine()
				.acquireSearcher("tagger");

		final IndexReader reader = searcher.reader();

		// get the search (not the index) analyzer associated with the requested
		// field
		Analyzer analyzer = indicesService.indexService(request.index())
				.mapperService().fieldSearchAnalyzer(field);

		// get the terms and desired (live) docs to use
		Terms terms = null;
		Bits docsToUse = null;
		try {
			Fields fields = MultiFields.getFields(reader);
			terms = fields.terms(field);
			docsToUse = docsToUse(reader);
		} catch (IOException e1) {
			logger.error("Error trying to get fields or terms from Lucene index in TaggerTransportAction:"
					+ e1.getMessage());
			throw new ElasticsearchException(e1.getMessage(), e1);
		}

		// the list of tags we found
		final List<Tag> tags = new ArrayList<Tag>();

		// get the token stream for the text to be tagged
		TokenStream tokenStream = null;
		try {
			tokenStream = analyzer.tokenStream(field, textToBeTagged);
		} catch (IOException e1) {
			logger.error("Error trying to get token stream from supplied document in TaggerTransportAction:"
					+ e1.getMessage());
			throw new ElasticsearchException(e1.getMessage(), e1);
		}

		// always get all tags, tag reduction is done on merged tags
		TagClusterReducer tagClusterReducer = TagClusterReducer.ALL;

		// get and call a tagger
		Tagger tagger = null;
		try {
			tagger = new Tagger(terms, docsToUse, tokenStream,
					tagClusterReducer, skipAltTokens, ignoreStopWords) {

				// callback to catch results from tagger
				@Override
				protected void tagCallback(int startOffset, int endOffset,
						Object docIdsKey) {

					// create and populate a Tag
					Tag tag = new Tag();
					tag.setStart(startOffset);
					tag.setEnd(endOffset);

					// add document ids and content to tag
					IntsRef docIds = (IntsRef) docIdsKey;
					addIDsandContents(tag, docIds, idsOnly);

					// check to see if any docs were added
					if (tag.getDocs().size() > 0) {

						// add the matching text if requested
						if (addMatchText) {
							tag.setMatchText(textToBeTagged.substring(
									startOffset, endOffset));
						}

						// add tag to list
						tags.add(tag);
					}
				}

				// add elasticsearch doc id and doc content to tag
				private void addIDsandContents(Tag tag, IntsRef docIds,
						boolean idOnly) {

					// loop over the lucene doc ids
					for (int i = docIds.offset; i < docIds.offset
							+ docIds.length; i++) {
						int docId = docIds.ints[i];
						// get the lucence doc
						Document doc = null;
						try {
							doc = reader.document(docId);
						} catch (IOException e) {
							logger.error("Error trying to get document using lucene document id from Lucene index in TaggerTransportAction:"
									+ e.getMessage());
							throw new ElasticsearchException(e.getMessage(), e);
						}

						// get the elasticsearch doc id from the "_uid" field
						String uid = doc.get("_uid");

						// the id string should have form <type>#<docid>
						String[] pieces = uid.split("#", 2);
						String docType = pieces[0];
						String docID = pieces[1];
						// only add documents that come from the requested
						// type(s)
						if (types == null || types.contains(docType)) {

							// create an Elastic doc to hold ID and contents
							ElasticDocument tmpDoc = new ElasticDocument();
							tmpDoc.setId(docID);
							// add the document contents if requested
							if (!idOnly) {
								// content comes from the "_source" field
								String cont = doc.getBinaryValue("_source")
										.utf8ToString();
								tmpDoc.setContents(cont);
							}

							// add the document to the tag
							tag.addDoc(docType, tmpDoc);
						}
					}// end doc id loop

				}
			};// end tagger inline code
		} catch (IOException e) {
			logger.error("Error trying to initialize tagger in TaggerTransportAction:"
					+ e.getMessage());
			throw new ElasticsearchException(e.getMessage(), e);
		}
		tagger.enableDocIdsCache(2000);
		try {
			// run the tagger
			tagger.process();
		} catch (IOException e) {
			logger.error("Error trying to tag document in TaggerTransportAction:"
					+ e.getMessage());
			throw new ElasticsearchException(e.getMessage(), e);
		} finally {
			try {
				tokenStream.close();
			} catch (IOException e) {
				logger.error("Error trying to close token stream int TaggerTransportAction:"
						+ e.getMessage());
			}
		}

		// create and send the shard response
		return new ShardTaggerResponse(request.index(), request.shardId(), tags);
	}

	// TODO how to get list of docs that match query and are live?
	// what documents to include in tagging
	private Bits docsToUse(IndexReader reader) {
		// for now get all live (non-deleted) docs
		return MultiFields.getLiveDocs(reader);
	}

	@Override
	protected GroupShardsIterator shards(ClusterState clusterState,
			TaggerRequest request, String[] concreteIndices) {
		return clusterState.routingTable().activePrimaryShardsGrouped(
				concreteIndices, true);
	}

	@Override
	protected ClusterBlockException checkGlobalBlock(ClusterState state,
			TaggerRequest request) {
		return state.blocks()
				.globalBlockedException(ClusterBlockLevel.METADATA);
	}

	@Override
	protected ClusterBlockException checkRequestBlock(ClusterState state,
			TaggerRequest request, String[] concreteIndices) {
		return state.blocks().indicesBlockedException(
				ClusterBlockLevel.METADATA, concreteIndices);
	}

}
