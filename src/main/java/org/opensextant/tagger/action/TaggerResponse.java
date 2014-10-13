package org.opensextant.tagger.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.support.broadcast.BroadcastOperationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class TaggerResponse extends BroadcastOperationResponse {

	// the list of tags
	private List<Tag> tags = new ArrayList<Tag>();

	TaggerResponse() {
	}

	TaggerResponse(int totalShards, int successfulShards, int failedShards,
			List<ShardOperationFailedException> shardFailures, List<Tag> tags) {
		super(totalShards, successfulShards, failedShards, shardFailures);
		this.tags = tags;

	}

	/**
	 * @return the tags
	 */
	public List<Tag> getTags() {
		if (tags == null) {
			return new ArrayList<Tag>();
		}
		return tags;
	}

	/**
	 * @param tags
	 *            the tags to set
	 */
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elasticsearch.action.support.broadcast.BroadcastOperationResponse
	 * #readFrom(org.elasticsearch.common.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		int n = in.readInt();
		for (int i = 0; i < n; i++) {
			Tag t = new Tag();
			t.readFrom(in);
			this.tags.add(t);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elasticsearch.action.support.broadcast.BroadcastOperationResponse
	 * #writeTo(org.elasticsearch.common.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		out.writeInt(this.tags.size());
		for (Tag t : this.tags) {
			t.writeTo(out);
		}

	}

}
