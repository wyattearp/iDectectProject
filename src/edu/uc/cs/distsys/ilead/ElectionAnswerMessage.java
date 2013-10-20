package edu.uc.cs.distsys.ilead;

import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

public class ElectionAnswerMessage extends Message {
		
	private static final long serialVersionUID = -7535177922144612878L;
	
	private final int destNodeId;
	private final int transactionId;

	public ElectionAnswerMessage(int senderId, int destNodeId, int transId) {
		super(senderId);
		this.destNodeId = destNodeId;
		this.transactionId = transId;
	}

	public int getTransactionId() {
		return transactionId;
	}
	
	public int getDestinationNodeId() {
		return destNodeId;
	}

	public static class ElectionAnswerFactory implements MessageFactory<ElectionAnswerMessage> {
		@Override
		public ElectionAnswerMessage create(byte[] rawMsg) {
			return (ElectionAnswerMessage) Message.deserialize(rawMsg);
		}
		
	}

}
