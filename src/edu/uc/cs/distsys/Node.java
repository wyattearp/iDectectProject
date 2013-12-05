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
	private int numProcOperating;
	transient private NodePropertiesManager properties;
	transient private NodeStateChangeListener stateChangeListener;

	public static Node createFailedNode(int id, NodeState state, 
			int seqNum, NodeStateChangeListener stateChangeListener) {
		return new Node(null, id, seqNum, 0, 0, state, stateChangeListener);
	}

	public Node(int id) {
		this(null, id, NodeState.UNKNOWN);
	}
	
	// This is only used to create the "local" node
	public Node(String name, int id, NodeState initialState) {
		this(name, id, -1, 0, 0, initialState, null);
	}
	
	public Node(Heartbeat hb, NodeStateChangeListener stateChangeListner) {
		this(null, hb.getNodeId(), hb.getSeqNum(), System.currentTimeMillis(), hb
				.getTimestamp(), NodeState.ONLINE, stateChangeListner);
		this.setLeaderId(hb.getLeaderId());
		this.setGroupId(hb.getNode().getGroupId());
	}

	private Node(String name, int id, int seqNum, long checkinRecv, long checkinSent,
			NodeState initialState, NodeStateChangeListener stateChangeListener) {
		this.id = id;
		this.seqHighWaterMark = seqNum;
		this.lastCheckinRcv = checkinRecv;
		this.lastCheckinSent = checkinSent;
		this.suspectTime = 0;
		this.state = initialState;
		this.groupCookie = Cookie.INVALID_COOKIE;
		
		// now, load up the node if we can
		LogHelper logger = new LogHelper(id, System.out, System.err, null);
		if (name != null) {
			this.properties = new NodePropertiesManager(name,this,logger);
			this.properties.load();
			this.persistProperties();
		}
		
		// Set the state change listener last so we don't fire off unnecessary state change notifications
		this.stateChangeListener = stateChangeListener;
		if (stateChangeListener != null) {
			stateChangeListener.onNodeStateChanged(this, NodeState.UNKNOWN);
		}
	}
	
	public void setStateChangeListener(NodeStateChangeListener stateChangeListener) {
		this.stateChangeListener = stateChangeListener;
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
	
	private void setState(NodeState state) {
		NodeState old = this.state;
		NodeStateChangeListener listener = this.stateChangeListener;
		this.state = state;
		if (!state.equals(old) && listener != null) {
			listener.onNodeStateChanged(this, old);
		}
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
		return getState() == NodeState.OFFLINE;
	}
	
	public boolean updateStatus(Heartbeat hb, int expectedNumSysProcs) {
		return this.updateStatus(hb, expectedNumSysProcs, System.currentTimeMillis());
	}

	public boolean updateStatus(Heartbeat hb, int expectedNumSysProcs, long recvTime) {
		boolean updated = false;
		if (hb.getSeqNum() > this.seqHighWaterMark) {
			this.leaderId = hb.getLeaderId();
			this.lastCheckinRcv = recvTime;
			this.lastCheckinSent = hb.getTimestamp();
			this.seqHighWaterMark = hb.getSeqNum();
			this.numProcOperating = hb.getNode().getNumProcOperating();
			if (expectedNumSysProcs == hb.getNode().getNumProcOperating()) {
				setState(NodeState.ONLINE);
			} else {
				setState(NodeState.INCOHERENT);
			}
			updated = true;
		}
		return updated;
	}

	public void updateState(Node node) {
		this.seqHighWaterMark = node.getSeqHighWaterMark();
		setState(node.getState());
		this.suspectTime = node.getSuspectTime();
	}

	public NodeState checkState(long currentTime) {
		Calendar curTime = Calendar.getInstance();
		Calendar lastCheckinTime = Calendar.getInstance();
		curTime.setTimeInMillis(currentTime);
		curTime.add(Calendar.MILLISECOND, (int) (-1.25 * (double)DetectMain.HB_PERIOD_MS));
		lastCheckinTime.setTimeInMillis(this.getLastCheckinRcv());
		if (getState().equals(NodeState.SUSPECT)) {
			if((currentTime - suspectTime) >= (3 * DetectMain.HB_PERIOD_MS)) {
				suspectTime = 0;
				setState(NodeState.OFFLINE);
			}
		} else if (! getState().equals(NodeState.OFFLINE)) {
			if (lastCheckinTime.before(curTime)) {
				setState(NodeState.SUSPECT);
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
			   " leader: " + leaderId +
			   " numProcOperating: " + numProcOperating +
			   "}";
	}
	
	public String toHTMLString() {
		String leaderString = "";
		if (id == leaderId) {
			leaderString = "This Node";
		} else {
			leaderString = Integer.toString(leaderId);
		}
		return 	"<html>" +
				"<div><table>" +
				"<tr>" +
					"<td>Node ID</td>" +
					"<td>" + id + "</td>" +
				"</tr><tr>" +
					"<td>Node State</td>" +
					"<td>" + state + "</td>" +
				"</tr><tr>" +
					"<td>Leader ID</td>" +
					"<td>" + leaderString + "</td>" +
				"</tr><tr>" +
					"<td>Group ID</td>" +
					"<td>" + groupId + "</td>" +
				"</tr><tr>" +
					"<td>Group Cookie</td>" +
					"<td>" + groupCookie + "</td>" +
				"</tr><tr>" +
					"<td>Number of Processes Operating</td>" +
					"<td>" + numProcOperating + "</td>" +
				"</tr>" +
				"</table></div>" +
				"<html>";
	}

	@Override
	public Node clone() {
		Node newNode = new Node(this.id);
		newNode.lastCheckinRcv = this.lastCheckinRcv;
		newNode.seqHighWaterMark = this.seqHighWaterMark;
		newNode.state = this.state;
		newNode.stateChangeListener = this.stateChangeListener;
		newNode.groupCookie = this.groupCookie;
		newNode.groupId = this.groupId;
		newNode.leaderId = this.leaderId;
		newNode.numProcOperating = this.numProcOperating;
		newNode.properties = this.properties;
		newNode.suspectTime = this.suspectTime;
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
	
	public int getNumProcOperating() {
		return numProcOperating;
	}

	public void setNumProcOperating(int numProcOperating) {
		this.numProcOperating = numProcOperating;
	}

	private void persistProperties() {
		if (this.properties != null) {
			this.properties.setProperties(this);
			this.properties.save();
		}
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
		System.out.println("\tNumber of Procs: " + this.numProcOperating);
		
	}

	public boolean isLeader() {
		return (this.leaderId == this.id);
	}

}
