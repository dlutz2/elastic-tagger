package org.opensextant.tagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;

public class Tag implements Streamable, Comparable<Tag> {

	// the start and end offsets for the text that matched
	int start;
	int end;

	// the string from the document that matched
	String matchText;

	// the matching documents, organized by elasticseach "type"
	Map<String, List<ElasticDocument>> docs = new HashMap<String, List<ElasticDocument>>();

	// to include this Tag or not in results
	boolean included = true;

	public Tag(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public Tag() {
		this.start = 0;
		this.end = 0;
	}

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

	public void mergeDocs(Map<String, List<ElasticDocument>> docs) {

		for (String typ : docs.keySet()) {
			List<ElasticDocument> newDocs = docs.get(typ);
			if (!this.docs.containsKey(typ)) {
				this.docs.put(typ, new ArrayList<ElasticDocument>());
			}
			this.docs.get(typ).addAll(newDocs);
		}
	}

	public void mergeTag(Tag i) {
		this.mergeDocs(i.getDocs());
	}

	public boolean isIncluded() {
		return included;
	}

	public void setIncluded(boolean include) {
		this.included = include;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(this.getMatchText());
		buf.append(" (" + this.getStart() + "," + this.getEnd() + ")");
		buf.append(" (Included:" + this.included +")");
		buf.append(this.docs);
		return buf.toString();
	}

	// does this Tag contain the given point?
	public boolean contains(int point) {
		return point <= end && point >= start;
	}

	// does this tag interact (is other than disjoint) with the given Tag
	public boolean interact(Tag other) {
		return !disjoint(other);
	}

	// is this tag completely disjoint with the given Tag
	public boolean disjoint(Tag other) {
		return other.getEnd() < start || other.getStart() > end;
	}

	// does this Tag fully contain but is not identical to the given Tag
	public boolean contains(Tag other) {
		return !this.identical(other) && this.contains(other.getStart())
				&& this.contains(other.getEnd());
	}

	// is this Tag fully contained by but not identical to the given Tag
	public boolean containedBy(Tag other) {
		return !this.identical(other) && other.contains(start)
				&& other.contains(end);
	}

	// does this Tag have identical span of given Tag
	public boolean identical(Tag other) {
		return other.getStart() == start && other.getEnd() == end;
	}

	// does this Tag overlap the left (lesser) edge of the given tag
	public boolean overlapLeft(Tag target) {
		return this.getStart() < target.getStart()
				&& target.contains(this.getEnd());
	}

	// does this Tag overlap the right (greater) edge of the given tag
	public boolean overlapRight(Tag target) {
		return this.getEnd() > target.getEnd()
				&& target.contains(this.getStart());
	}

	@Override
	public void readFrom(StreamInput in) throws IOException {
		this.start = in.readInt();
		this.end = in.readInt();
		this.matchText = in.readString();
		this.included = in.readBoolean();
		int docEntries = in.readInt();
		for (int i = 0; i < docEntries; i++) {
			String key = in.readString();
			int cnt = in.readInt();
			List<ElasticDocument> docList = new ArrayList<ElasticDocument>();
			for (int c = 0; c < cnt; c++) {
				ElasticDocument d = new ElasticDocument();
				d.readFrom(in);
				docList.add((d));
			}
			this.docs.put(key, docList);
		}

	}

	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeInt(this.start);
		out.writeInt(this.end);
		out.writeString(this.matchText);
		out.writeBoolean(this.included);
		out.writeInt(this.docs.size());
		for (Entry<String, List<ElasticDocument>> entry : this.docs.entrySet()) {
			out.writeString(entry.getKey());
			int cnt = entry.getValue().size();
			out.writeInt(cnt);
			for (int i = 0; i < cnt; i++) {
				entry.getValue().get(i).writeTo(out);
			}
		}

	}

	@Override
	public int compareTo(Tag o) {

		if (!(o instanceof Tag)) {
			return 0;
		}

		Tag other = (Tag) o;
		if (start < other.getStart())
			return -1;
		else if (start > other.getStart())
			return 1;
		else if (end < other.getEnd())
			return -1;
		else if (end > other.getEnd())
			return 1;
		else
			if(o.docs == this.docs){
				return 0;
			}
		if(o.docs.size() != this.docs.size()){
			return o.docs.size() - this.docs.size()  ;
		}

		return o.docs.hashCode() - this.docs.hashCode();
		
	}
	

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Tag)){
			return false;
		}
		Tag otherTag =  (Tag)other;
		
		boolean s = (otherTag.start == start);
		boolean e = (otherTag.end == end);
		boolean d = (otherTag.docs == docs);
		return s && e && d;
	}

	
	
	
}
