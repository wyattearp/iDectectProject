package edu.uc.cs.distsys.ilead;

import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

public class ElectionAnswerMessage extends Message {
	
	private int transactionId;
	private static final long serialVersionUID = -7535177922144612878L;
	
	public ElectionAnswerMessage(int nodeId) {
		super(nodeId);
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}
	
	public static class ElectionFactory implements MessageFactory<Election> {
		@Override
		public Election create(byte[] rawMsg) {
			return (Election) Message.deserialize(rawMsg);
		}
		
	}

}
