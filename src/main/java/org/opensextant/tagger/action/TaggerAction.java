package org.opensextant.tagger.action;

import org.elasticsearch.action.ClientAction;
import org.elasticsearch.client.Client;

public class TaggerAction extends
		ClientAction<TaggerRequest, TaggerResponse, TaggerRequestBuilder> {

	public static final TaggerAction INSTANCE = new TaggerAction();

	public static final String NAME = "indices/tag";

	private TaggerAction() {
		super(NAME);
	}

	@Override
	public TaggerRequestBuilder newRequestBuilder(Client client) {
		return new TaggerRequestBuilder(client);
	}

	@Override
	public TaggerResponse newResponse() {
		return new TaggerResponse();
	}

}
