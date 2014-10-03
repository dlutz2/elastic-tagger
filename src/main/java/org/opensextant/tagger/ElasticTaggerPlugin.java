package org.opensextant.tagger;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.opensextant.tagger.action.TaggerAction;
import org.opensextant.tagger.action.TransportTaggerAction;
import org.opensextant.tagger.rest.ElasticTaggerRestAction;

public class ElasticTaggerPlugin extends AbstractPlugin {
	@Override
	public String name() {
		return "ElasticTaggerPlugin";
	}

	@Override
	public String description() {
		return "Tags documents using the contents of an ElasticSearch index";
	}

	// for Rest API
	public void onModule(final RestModule module) {
		module.addRestAction(ElasticTaggerRestAction.class);
	}

	public void onModule(ActionModule module) {
		module.registerAction(TaggerAction.INSTANCE,
				TransportTaggerAction.class);
	}

}
