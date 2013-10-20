package edu.uc.cs.distsys.ilead;

import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

public class CoodrinatorMessage extends Message {
	
	private int nodeId;
	private static final long serialVersionUID = 8275339187735104749L;
	
	public CoodrinatorMessage(int nodeId) {
		super(nodeId);
	}

	public int getNode() {
		return this.nodeId;
	}
	public void setNode(int id) {
		this.nodeId = id;
	}
	
	public static class ElectionFactory implements MessageFactory<Election> {
		@Override
		public Election create(byte[] rawMsg) {
			return (Election) Message.deserialize(rawMsg);
		}
		
	}

}
