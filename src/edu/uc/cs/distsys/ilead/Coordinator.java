package edu.uc.cs.distsys.ilead;

import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

/***
 * Sent to announce the identify of the election process
 */
public class Coordinator extends Message {

	public static class CoordinatorFactory implements MessageFactory<Coordinator> {
		@Override
		public Coordinator create(byte[] rawMsg) {
			return (Coordinator) Message.deserialize(rawMsg);
		}
		
	}

	private static final long serialVersionUID = -169736102347916306L;
	
	public Coordinator(int nodeId) {
		super(nodeId);
	}
}
