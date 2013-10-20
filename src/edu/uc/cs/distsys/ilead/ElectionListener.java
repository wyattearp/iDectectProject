package edu.uc.cs.distsys.ilead;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.comms.MessageListener;

public class ElectionListener implements MessageListener<ElectionMessage> {

	private final int myId;
	private ElectionTracker tracker;
	private Logger logger;
	
	
	public ElectionListener(int nodeId, ElectionTracker tracker, Logger logger) {
		this.myId = nodeId;
		this.tracker = tracker;
		this.logger = logger;
	}
	
	@Override
	public void notifyMessage(ElectionMessage message) {
		// Only respond to election requests from lower processes
		if (message.getSenderId() < this.myId) {
			tracker.answerElectionQuery(message);
		}
	}
}
