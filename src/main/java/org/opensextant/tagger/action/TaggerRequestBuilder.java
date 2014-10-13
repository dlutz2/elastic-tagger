package org.opensextant.tagger.action;

import java.util.Set;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.broadcast.BroadcastOperationRequestBuilder;
import org.elasticsearch.client.Client;

public class TaggerRequestBuilder
		extends
		BroadcastOperationRequestBuilder<TaggerRequest, TaggerResponse, TaggerRequestBuilder, Client> {

	public TaggerRequestBuilder(Client client) {
		super(client, new TaggerRequest());
	}

	public TaggerRequestBuilder index(String idx) {
		request.indices(idx);
		return this;
	}

	public TaggerRequestBuilder types(Set<String> types) {
		request.setType(types);
		return this;
	}

	public TaggerRequestBuilder field(String field) {
		request.setField(field);
		return this;
	}

	public TaggerRequestBuilder reduceMode(String mode) {
		request.setReduceMode(mode);
		return this;
	}

	public TaggerRequestBuilder tagLimit(int limit) {
		request.setTagsLimit(limit);
		return this;
	}

	public TaggerRequestBuilder includeMatchText(boolean includeText) {
		request.setIncludeMatchText(includeText);
		return this;
	}

	public TaggerRequestBuilder idsOnly(boolean idsOnly) {
		request.setIdsOnly(idsOnly);
		return this;
	}

	public TaggerRequestBuilder skipAltTokens(boolean skipAltTokens) {
		request.setSkipAltTokens(skipAltTokens);
		return this;
	}

	public TaggerRequestBuilder ignoreStopWords(boolean ignore) {
		request.setIgnoreStopwords(ignore);
		return this;
	}

	public TaggerRequestBuilder textToBeTagged(String text) {
		request.setTextToBeTagged(text);
		return this;
	}

	@Override
	protected void doExecute(ActionListener<TaggerResponse> listener) {
		((Client) client).execute(TaggerAction.INSTANCE, request, listener);
	}

}
