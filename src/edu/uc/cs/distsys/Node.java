package edu.uc.cs.distsys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;

import edu.uc.cs.distsys.idetect.DetectMain;
import edu.uc.cs.distsys.idetect.Heartbeat;

public class Node implements Serializable {

	private static final long serialVersionUID = 9034156178527052520L;

	private final int id;
	private long lastCheckinRcv;
	private long lastCheckinSent;
	private long suspectTime;
	private int seqHighWaterMark;
	private NodeState state;
	private int leaderId;
	private int groupId;

	public static Node createFailedNode(int id, NodeState state, int seqNum) {
		return new Node(id, seqNum, 0, 0, state);
	}

	public Node(int id) {
		this(id, -1, 0, 0, NodeState.UNKNOWN);
	}

	public Node(Heartbeat hb) {
		this(hb.getNodeId(), hb.getSeqNum(), System.currentTimeMillis(), hb
				.getTimestamp(), NodeState.ONLINE);
		this.setLeaderId(hb.getLeaderId());
	}

	private Node(int id, int seqNum, long checkinRecv, long checkinSent,
			NodeState initialState) {
		this.id = id;
		this.seqHighWaterMark = seqNum;
		this.lastCheckinRcv = checkinRecv;
		this.lastCheckinSent = checkinSent;
		this.suspectTime = 0;
		this.state = initialState;
	}

	public int getId() {
		return id;
	}

	public long getLastCheckinRcv() {
		return lastCheckinRcv;
	}

	public long getLastCheckinSent() {
		return lastCheckinSent;
	}

	public int getSeqHighWaterMark() {
		return seqHighWaterMark;
	}

	public NodeState getState() {
		return state;
	}
	
	public long getSuspectTime() {
		return suspectTime;
	}

	public boolean isOffline() {
		return this.state == NodeState.OFFLINE;
	}
	
	public boolean updateStatus(Heartbeat hb) {
		return this.updateStatus(hb, System.currentTimeMillis());
	}

	public boolean updateStatus(Heartbeat hb, long recvTime) {
		boolean updated = false;
		if (hb.getSeqNum() > this.seqHighWaterMark) {
			this.leaderId = hb.getLeaderId();
			this.lastCheckinRcv = recvTime;
			this.lastCheckinSent = hb.getTimestamp();
			this.seqHighWaterMark = hb.getSeqNum();
			this.state = NodeState.ONLINE;
			updated = true;
		}
		return updated;
	}

	public void updateState(Node node) {
		this.seqHighWaterMark = node.getSeqHighWaterMark();
		this.state = node.getState();
		this.suspectTime = node.getSuspectTime();
	}

	public NodeState checkState(long currentTime) {
		Calendar curTime = Calendar.getInstance();
		Calendar lastCheckinTime = Calendar.getInstance();
		curTime.setTimeInMillis(currentTime);
		curTime.add(Calendar.MILLISECOND, (int) (-1 * DetectMain.HB_PERIOD_MS));
		lastCheckinTime.setTimeInMillis(this.getLastCheckinRcv());
		if (state.equals(NodeState.SUSPECT)) {
			if((currentTime - suspectTime) >= (2 * DetectMain.HB_PERIOD_MS)) {
				suspectTime = 0;
				this.state = NodeState.OFFLINE;
			}
		} else if (! state.equals(NodeState.OFFLINE)) {
			if (lastCheckinTime.before(curTime)) {
				this.state = NodeState.SUSPECT;
				this.suspectTime = currentTime;
			}
		}
		return state;
	}

	@Override
	public Node clone() {
		Node newNode = new Node(this.id);
		newNode.lastCheckinRcv = this.lastCheckinRcv;
		newNode.seqHighWaterMark = this.seqHighWaterMark;
		newNode.state = state;
		return newNode;
	}

	public byte[] serialize() {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(this);
			bytes = bos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				bos.close();
			} catch (IOException e) {
				// TODO
			}
		}
		return bytes;
	}

	public static Node deserialize(byte[] rawData) {
		Node node = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(rawData);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			node = (Node) in.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				bis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return node;
	}

	public int getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(int leaderId) {
		this.leaderId = leaderId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

}
