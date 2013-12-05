package edu.uc.cs.distsys.ilead;

import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

/***
 * Respond to an election message
 */
public class AnswerElection extends Message {

	public static class AnswerElectionFactory implements MessageFactory<AnswerElection> {
		@Override
		public AnswerElection create(byte[] rawMsg) {
			return (AnswerElection) Message.deserialize(rawMsg);
		}
		
	}
	
	private static final long serialVersionUID = 4417540419975170320L;
	
	public AnswerElection(int nodeId) {
		super(nodeId);
	}
}
