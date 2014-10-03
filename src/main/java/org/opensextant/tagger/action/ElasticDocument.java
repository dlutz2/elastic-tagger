package org.opensextant.tagger.action;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class ElasticDocument {

	String id;
	@JsonRawValue
	String contents;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

}
