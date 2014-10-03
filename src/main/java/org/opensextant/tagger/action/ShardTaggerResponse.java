package org.opensextant.tagger.action;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.action.support.broadcast.BroadcastShardOperationResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class ShardTaggerResponse extends BroadcastShardOperationResponse {

	// the tags found by this shard
	private List<Tag> tags;

	ShardTaggerResponse() {
	}

	public ShardTaggerResponse(String index, int shardId, List<Tag> tags) {
		super(index, shardId);
		this.tags = tags;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elasticsearch.action.support.broadcast.BroadcastShardOperationResponse
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
	 * org.elasticsearch.action.support.broadcast.BroadcastShardOperationResponse
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
