package edu.uc.cs.idetect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Heartbeat implements Serializable {

	private static final long serialVersionUID = 8406620436481247617L;

	private final int nodeId;
	private final int seqNum;
	private final long timestamp;
	private final List<Node> failedNodes;

	public Heartbeat(int nodeId, int seqNum, long timestamp, List<Node> failedNodes) {
		this.nodeId = nodeId;
		this.seqNum = seqNum;
		this.timestamp = timestamp;
		this.failedNodes = failedNodes;
	}
	
	public Heartbeat(int nodeId, int seqNum) {
		this(nodeId, seqNum, System.currentTimeMillis(), new LinkedList<Node>());
	}

	public int getNodeId() {
		return nodeId;
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

	public static Heartbeat deserialize(byte[] rawData) {
		Heartbeat hb = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(rawData);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			hb = (Heartbeat) in.readObject();
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
		return hb;
	}
}
