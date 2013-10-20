package edu.uc.cs.distsys.ilead;

import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

public class CoordinatorMessage extends Message {
	
	private int nodeId;
	private static final long serialVersionUID = 8275339187735104749L;
	
	public CoordinatorMessage(int nodeId) {
		super(nodeId);
	}

	public int getNode() {
		return this.nodeId;
	}
	public void setNode(int id) {
		this.nodeId = id;
	}
	
	public static class CoordinatorFactory implements MessageFactory<CoordinatorMessage> {
		@Override
		public CoordinatorMessage create(byte[] rawMsg) {
			return (CoordinatorMessage) Message.deserialize(rawMsg);
		}
		
	}

}
