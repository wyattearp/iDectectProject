package edu.uc.cs.distsys.ilead;

import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

public class Election extends Message {

	public static class ElectionFactory implements MessageFactory<Election> {
		@Override
		public Election create(byte[] rawMsg) {
			return (Election) Message.deserialize(rawMsg);
		}
		
	}
	
	private static final long serialVersionUID = -3015844352647211839L;

	public Election(int nodeId) {
		super(nodeId);
	}
}
