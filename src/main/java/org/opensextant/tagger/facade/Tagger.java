package org.opensextant.tagger.facade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.opensextant.tagger.action.Tag;
import org.opensextant.tagger.action.TaggerAction;
import org.opensextant.tagger.action.TaggerRequest;
import org.opensextant.tagger.action.TaggerResponse;

public class Tagger {
	static Client client;
	static TransportClient tc;

	public static void main(String[] args) {

		String host = args[0];
		String index = args[1];
		String type = args[2];
		String field = args[3];
		String text = args[4];

		Tagger tagger = new Tagger(host);

		List<Tag> tags = tagger.tag(index, type, field, text);

		for (Tag tag : tags) {
			System.out.println(tag);
		}
		tagger.close();

	}

	public Tagger(String host) {
		tc = new TransportClient();
		client = tc.addTransportAddress(new InetSocketTransportAddress(host,
				9300));
		// TODO how to test if client actually connected?
	}

	public void close() {
		tc.close();
		client.close();
	};

	public List<Tag> tag(String index, String type, String field, String text) {

		TaggerRequest taggerRequest = new TaggerRequest();

		Set<String> types = new HashSet<String>();
		types.add(type);

		taggerRequest.setType(types);
		taggerRequest.setField(field);
		taggerRequest.setTextToBeTagged(text);
		taggerRequest.setReduceMode("NONE");
		taggerRequest.setIncludeMatchText(true);
		taggerRequest.setIdsOnly(false);

		// execute TaggerRequest and get TaggerResponse,

		TaggerResponse resp = client.execute(TaggerAction.INSTANCE,
				taggerRequest).actionGet();

		return (resp.getTags());

	}

}
