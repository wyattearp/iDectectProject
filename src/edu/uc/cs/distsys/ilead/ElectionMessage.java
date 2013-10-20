package edu.uc.cs.distsys.ilead;

import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

public class ElectionMessage extends Message {
	
	private int transactionId;
	private int id;
	private static final long serialVersionUID = -4572059794836848985L;
	
	public ElectionMessage(int nodeId) {
		super(nodeId);
	}
	
	public ElectionMessage(int nodeId,int transactionId) {
		super(nodeId);
		this.id = nodeId;
		this.transactionId = transactionId;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public static class ElectionFactory implements MessageFactory<Election> {
		@Override
		public Election create(byte[] rawMsg) {
			return (Election) Message.deserialize(rawMsg);
		}
		
	}

}
