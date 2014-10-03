package org.opensextant.tagger.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;

public class Tag implements Streamable {

	// the start and end offsets for the text that matched
	int start;
	int end;

	// the string from the document that matched
	String matchText;

	Map<String, List<ElasticDocument>> docs = new HashMap<String, List<ElasticDocument>>();

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getMatchText() {
		return matchText;
	}

	public void setMatchText(String matchText) {
		this.matchText = matchText;
	}

	public Map<String, List<ElasticDocument>> getDocs() {
		return docs;
	}

	public List<ElasticDocument> getDocs(String type) {
		return docs.get(type);
	}
	
	public void setDocs(Map<String, List<ElasticDocument>> docs) {
		this.docs = docs;
	}

	public void addDoc(String type, ElasticDocument doc) {
		if (!this.docs.containsKey(type)) {
			this.docs.put(type, new ArrayList<ElasticDocument>());
		}
		this.docs.get(type).add(doc);
	}

	@Override
	public void readFrom(StreamInput in) throws IOException {
		this.start = in.readInt();
		this.end = in.readInt();
		this.matchText = in.readString();

		int docEntries = in.readInt();
		for (int i = 0; i < docEntries; i++) {
			String key = in.readString();
			List<ElasticDocument> value = (List<ElasticDocument>) in
					.readGenericValue();
			this.docs.put(key, value);
		}

	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeInt(this.start);
		out.writeInt(this.end);
		out.writeString(this.matchText);

		out.writeInt(this.docs.size());
		for (Entry<String, List<ElasticDocument>> entry : this.docs.entrySet()) {
			out.writeString(entry.getKey());
			out.writeGenericValue(entry.getValue());
		}

	}

}