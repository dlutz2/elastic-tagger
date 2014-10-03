package org.opensextant.tagger.rest;

import static org.elasticsearch.rest.action.support.RestActions.buildBroadcastShardsHeader;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.support.RestBuilderListener;
import org.opensextant.tagger.action.TaggerAction;
import org.opensextant.tagger.action.TaggerRequest;
import org.opensextant.tagger.action.TaggerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticTaggerRestAction extends BaseRestHandler {

	static ObjectMapper mapper = new ObjectMapper();

	@Inject
	public ElasticTaggerRestAction(final Settings settings,
			final Client client, final RestController restController) {
		super(settings, client);

		restController.registerHandler(RestRequest.Method.GET,"/{index}/_tag/{field}",this);
		restController.registerHandler(RestRequest.Method.GET,"/{index}/{type}/_tag/{field}", this);
	}

	@Override
	protected void handleRequest(final RestRequest request,
			final RestChannel channel, Client client) {
		// build TaggerRequest from incoming RestRequest
		TaggerRequest taggerRequest = new TaggerRequest(request);

		// execute TaggerRequest and listen for TaggerResponse,
		// and build RestResponse (in callback)
		client.execute(TaggerAction.INSTANCE, taggerRequest,
				new RestBuilderListener<TaggerResponse>(channel) {
					@Override
					public RestResponse buildResponse(TaggerResponse response,
							XContentBuilder builder) throws Exception {

						// TODO confirm that this produces acceptable JSON
						builder.startObject();
						buildBroadcastShardsHeader(builder, response);
						String json = mapper.writeValueAsString(response
								.getTags());
						builder.rawField("tags", json.getBytes());
						builder.endObject();
						return new BytesRestResponse(RestStatus.OK, builder);
					}
				});

	}

}
