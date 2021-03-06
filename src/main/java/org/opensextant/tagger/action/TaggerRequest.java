package org.opensextant.tagger.action;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.elasticsearch.action.support.broadcast.BroadcastOperationRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.rest.RestRequest;

public class TaggerRequest extends BroadcastOperationRequest<TaggerRequest> {

	// the text to be tagged
	private String textToBeTagged ="";

	// the ES type and field to use to tag
	private Set<String> type = new HashSet<String>();
	private String field;

	// how to reduce overlapping/interacting tags
	// valid values: "ALL", "LONGEST", "OVERLAP_RIGHT"
	private String reduceMode = "SUB";

	// maximum number of tags to include in a response
	private int tagsLimit = -1;

	// include the text from the document in the response?
	private boolean includeMatchText = true;

	// return only document ids, not document content
	private boolean idsOnly = false;

	// correct for stopwords and/or alternate tokens?
	private boolean skipAltTokens = false;
	private boolean ignoreStopwords = false;
	
	// the query to select a subset of documents to use for tagging
	private String query = null;

	// Request parameters
	private static final String INDEX = "index";
	private static final String SOURCE = "source";
	private static final String TYPES = "type";
	private static final String FIELD = "field";
	private static final String REDUCE_MODE = "reducemode";
	private static final String TAGS_LIMIT = "tagsLimit";// int
	private static final String INCLUDE_MATCH_TEXT = "matchText";// boolean
	private static final String DOC_IDS_ONLY = "docIDsonly";// boolean
	private static final String SKIP_ALT_TOKENS = "skipAltTokens";// boolean
	private static final String IGNORE_STOPWORDS = "ignoreStopwords";// boolean
	private static final String QUERY = "query";// String

	ESLogger logger = ESLoggerFactory.getLogger(TaggerRequest.class.getName());

	public TaggerRequest() {
	}

	// build a TaggerRequest from a RESTRequest
	public TaggerRequest(RestRequest request) {
		super(Strings.splitStringByCommaToArray(request.param(INDEX)));

		// Check SOURCE and then check request content, fail if both null
		this.textToBeTagged = request.param(SOURCE);
		if (this.textToBeTagged == null) {
			this.textToBeTagged = request.content().toUtf8();
		}

		if (this.textToBeTagged == null) {
			logger.error("No content submitted in source parameter or request content");
		}

		// get the rest of the request params

		String typs = request.param(TYPES);
		if (typs != null) {
			this.type = Strings.splitStringByCommaToSet(typs);
		} else {

		}
		this.field = request.param(FIELD);
		this.reduceMode = request.param(REDUCE_MODE, "SUB");

		this.tagsLimit = request.paramAsInt(TAGS_LIMIT, -1);
		this.includeMatchText = request
				.paramAsBoolean(INCLUDE_MATCH_TEXT, true);
		this.idsOnly = request.paramAsBoolean(DOC_IDS_ONLY, false);
		this.skipAltTokens = request.paramAsBoolean(SKIP_ALT_TOKENS, false);
		this.ignoreStopwords = request.paramAsBoolean(IGNORE_STOPWORDS, false);
		
		this.query = request.param(QUERY,"");

	}

	public String getTextToBeTagged() {
		return textToBeTagged;
	}

	public void setTextToBeTagged(String textToBeTagged) {
		this.textToBeTagged = textToBeTagged;
	}

	public Set<String> getType() {
		return type;
	}

	public void setType(Set<String> type) {
		this.type = type;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getReduceMode() {
		return reduceMode;
	}

	public void setReduceMode(String reduceMode) {
		this.reduceMode = reduceMode;
	}

	public int getTagsLimit() {
		return tagsLimit;
	}

	public void setTagsLimit(int tagsLimit) {
		this.tagsLimit = tagsLimit;
	}

	public boolean isIncludeMatchText() {
		return includeMatchText;
	}

	public void setIncludeMatchText(boolean includeMatchText) {
		this.includeMatchText = includeMatchText;
	}

	public boolean isIdsOnly() {
		return idsOnly;
	}

	public void setIdsOnly(boolean idsOnly) {
		this.idsOnly = idsOnly;
	}

	public boolean isSkipAltTokens() {
		return skipAltTokens;
	}

	public void setSkipAltTokens(boolean skipAltTokens) {
		this.skipAltTokens = skipAltTokens;
	}

	public boolean isIgnoreStopwords() {
		return ignoreStopwords;
	}

	public void setIgnoreStopwords(boolean ignoreStopwords) {
		this.ignoreStopwords = ignoreStopwords;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elasticsearch.action.support.broadcast.BroadcastOperationRequest#
	 * writeTo(org.elasticsearch.common.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		if(this.textToBeTagged !=  null){
			out.writeString(this.textToBeTagged);
		}else{
			out.writeString(" ");
		}		
		out.writeInt(this.type.size());
		for (String t : this.type) {
			out.writeString(t);
		}
		if(this.field != null){
			out.writeString(this.field);
		}else{
			out.writeString(" ");
		}
		
		out.writeString(this.reduceMode);
		out.writeInt(this.tagsLimit);
		out.writeBoolean(this.includeMatchText);
		out.writeBoolean(this.idsOnly);
		out.writeBoolean(this.skipAltTokens);
		out.writeBoolean(this.ignoreStopwords);
		if(this.query != null){
			out.writeString(this.query);
		}else{
			out.writeString(" ");
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elasticsearch.action.support.broadcast.BroadcastOperationRequest#
	 * readFrom(org.elasticsearch.common.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		String tmpTxt = in.readString();
		if(tmpTxt != null && !tmpTxt.equals(" ")){
			this.textToBeTagged = tmpTxt;
		}else{
			this.textToBeTagged = null;
		}
		
		int num = in.readInt();
		for (int i = 0; i < num; i++) {
			this.type.add(in.readString());
		}
		String tmpFld = in.readString();
		if(tmpFld != null && !tmpFld.equals(" ")){
			this.field = tmpFld;
		}else{
			this.field = "";
		}
				
		this.reduceMode = in.readString();
		this.tagsLimit = in.readInt();
		this.includeMatchText = in.readBoolean();
		this.idsOnly = in.readBoolean();
		this.skipAltTokens = in.readBoolean();
		this.ignoreStopwords = in.readBoolean();
		String tmpQ = in.readString();
		if(tmpQ != null && !tmpQ.equals(" ")){
			this.query = tmpQ;
		}else{
			this.query = null;
		}
		
	}

}
