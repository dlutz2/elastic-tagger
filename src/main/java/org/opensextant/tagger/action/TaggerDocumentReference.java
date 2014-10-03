package org.opensextant.tagger.action;

import java.io.IOException;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;

public class TaggerDocumentReference implements Streamable{

	String index;
	String type;
	String id;

	public TaggerDocumentReference() {

	}

	public TaggerDocumentReference(String host, String index, String type,
			String id) {

		this.index = index;
		this.type = type;
		this.id = id;
	}


	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void readFrom(StreamInput in) throws IOException {
		this.index = in.readString();
		this.type = in.readString();
		this.id = in.readString();
	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeString(this.getIndex());
		out.writeString(this.getType());
		out.writeString(this.getId());
	}

}
