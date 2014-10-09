package org.opensextant.tagger.action;

import java.io.IOException;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;

import com.fasterxml.jackson.annotation.JsonRawValue;

// This class is just a container to associate an elsticsearch document id
// with the (optional) contents of that document. 
public class ElasticDocument implements Streamable {

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

	public String toString(){
		return "Document " + this.id;
	}
	
	
	@Override
	public void readFrom(StreamInput in) throws IOException {
		this.id =  in.readString();
		this.contents =  in.readString();
		
	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeString(this.id);
		out.writeString(this.contents);
		
	}

}
