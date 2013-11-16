package edu.uc.cs.distsys;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import edu.uc.cs.distsys.idetect.DetectMain;
import edu.uc.cs.distsys.idetect.Heartbeat;
import edu.uc.cs.distsys.init.Cookie;
import edu.uc.cs.distsys.properties.NodePropertiesManager;

public class Node implements Serializable {

	private static final long serialVersionUID = 9034156178527052520L;
	private static final String timeStampFormat = "HH:mm:ss.SSS";
	
	private final int id;
	private long lastCheckinRcv;
	private long lastCheckinSent;
	private long suspectTime;
	private int seqHighWaterMark;
	private NodeState state;
	private int leaderId;
	private int groupId;
	private Cookie groupCookie;
	private NodePropertiesManager properties;

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
		this.groupCookie = Cookie.INVALID_COOKIE;
		
		// now, load up the node if we can
		LogHelper logger = new LogHelper(id, System.out, System.err, null);
		this.properties = new NodePropertiesManager(this,logger);
		this.properties.load();
		this.persistProperties();
	}

	public int getId() {
		return id;
	}

	public long getLastCheckinRcv() {
		return lastCheckinRcv;
	}
	
	public String getLastCheckinRcvString() {
		Date d = new Date(this.lastCheckinRcv);
		SimpleDateFormat df2 = new SimpleDateFormat(timeStampFormat);
		return df2.format(d);
	}

	public long getLastCheckinSent() {
		return lastCheckinSent;
	}
	public String getLastCheckinSentString() {
		Date d = new Date(this.lastCheckinSent);
		SimpleDateFormat df2 = new SimpleDateFormat(timeStampFormat);
		return df2.format(d);
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
	
	public String getSuspectTimeString() {
		Date d = new Date(this.suspectTime);
		SimpleDateFormat df2 = new SimpleDateFormat(timeStampFormat);
		return df2.format(d);
	}
	
	public Cookie getGroupCookie() {
		return groupCookie;
	}
	
	public void setGroupCookie(Cookie groupCookie) {
		this.groupCookie = groupCookie;
		this.persistProperties();
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
			if((currentTime - suspectTime) >= (3 * DetectMain.HB_PERIOD_MS)) {
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
	public String toString() {
		return "{ID: " + id +
			   " lastCheckinRcv: " + lastCheckinRcv + 
			   " lastCheckinSent: " + lastCheckinSent + 
			   " suspectTime: " + suspectTime + 
			   " seqHighWater: " + seqHighWaterMark +
			   " state: " + state +
			   " leader: " + leaderId + "}";
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
			System.out.println("Unable to serialize node");
			this.printNode();
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				bos.close();
			} catch (IOException e) {
				System.out.println("Unable to serialize the node");
				this.printNode();
				e.printStackTrace();
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
			System.out.println("Unable to deserialize node");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Unable to locate the class during deserialize");
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				bis.close();
			} catch (IOException e) {
				System.out.println("Some sort of weird I/O problem");
				if (node != null) {
					node.printNode();
				}
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
		this.persistProperties();
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
		this.persistProperties();
		
	}
	
	private void persistProperties() {
		this.properties.setProperties(this);
		this.properties.save();
	}
	
	public void printNode() {

		System.out.println("==== Node ID: " + this.id + "====");
		System.out.println("\tLast Checkin Rcv: " + this.getLastCheckinRcvString());
		System.out.println("\tLast Checkin Snd: " + this.getLastCheckinSentString());
		System.out.println("\tSuspect Time: " + this.getSuspectTimeString());
		System.out.println("\tSeq HighWaterMakr: " + this.seqHighWaterMark);
		System.out.println("\tState: " + this.state);
		System.out.println("\tLeader ID: " + this.leaderId);
		System.out.println("\tGroup ID: " + this.groupId);
		
	}

	public boolean isLeader() {
		return (this.leaderId == this.id);
	}

}
