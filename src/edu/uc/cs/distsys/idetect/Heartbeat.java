package edu.uc.cs.distsys.idetect;

import java.util.LinkedList;
import java.util.List;

import edu.uc.cs.distsys.Node;
import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

public class Heartbeat extends Message {

	public static class HeartbeatFactory implements MessageFactory<Heartbeat> {
		public HeartbeatFactory() {}
		@Override
		public Heartbeat create(byte[] rawMsg) {
			return (Heartbeat) Message.deserialize(rawMsg);
		}
	}
	
	private static final long serialVersionUID = 8406620436481247617L;

	private final int leaderId;
	private final int seqNum;
	private final long timestamp;
	private final List<Node> failedNodes;

	public Heartbeat(int nodeId, int leaderId, int seqNum, long timestamp, List<Node> failedNodes) {
		super(nodeId);
		this.leaderId = leaderId;
		this.seqNum = seqNum;
		this.timestamp = timestamp;
		this.failedNodes = failedNodes;
	}
	
	public Heartbeat(int nodeId, int leaderId, int seqNum) {
		this(nodeId, leaderId, seqNum, System.currentTimeMillis(), new LinkedList<Node>());
	}

	public int getNodeId() {
		return getSenderId();
	}
	
	public int getLeaderId() {
		return leaderId;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public List<Node> getFailedNodes() {
		return failedNodes;
	}

}
