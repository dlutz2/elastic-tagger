package org.opensextant.tagger.action;

import java.io.IOException;

import org.elasticsearch.action.support.broadcast.BroadcastShardOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class ShardTaggerRequest extends BroadcastShardOperationRequest {

	// the tagger request which triggered this shard request
	private TaggerRequest tagRequest;

	// empty constructor
	ShardTaggerRequest() {
	}

	// public constructor
	public ShardTaggerRequest(String index, int shardId, TaggerRequest request) {
		super(index, shardId, request);
		this.tagRequest = request;

	}

	public TaggerRequest getTagRequest() {
		return tagRequest;
	}

	public void setTagRequest(TaggerRequest tagRequest) {
		this.tagRequest = tagRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elasticsearch.action.support.broadcast.BroadcastShardOperationRequest
	 * #readFrom(org.elasticsearch.common.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		this.tagRequest.readFrom(in);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.elasticsearch.action.support.broadcast.BroadcastShardOperationRequest
	 * #writeTo(org.elasticsearch.common.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		this.tagRequest.writeTo(out);
	}

}
