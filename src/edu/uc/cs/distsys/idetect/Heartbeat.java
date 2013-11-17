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

	private final Node myNode;
	private final int seqNum;
	private final long timestamp;
	private final List<Node> failedNodes;

	public Heartbeat(Node node, int seqNum, long timestamp, List<Node> failedNodes) {
		super(node.getId());
		this.myNode = node;
		this.seqNum = seqNum;
		this.timestamp = timestamp;
		this.failedNodes = failedNodes;
	}
	
	public Heartbeat(Node node, int seqNum) {
		this(node, seqNum, System.currentTimeMillis(), new LinkedList<Node>());
	}

	public Node getNode() {
		return myNode;
	}
	
	public int getNodeId() {
		return getSenderId();
	}
	
	public int getLeaderId() {
		return myNode.getLeaderId();
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
