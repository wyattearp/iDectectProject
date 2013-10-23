package edu.uc.cs.distsys.ilead;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.comms.MessageListener;

public class ElectionNotifierThread implements Runnable {

	private static final long ELECTION_ANSWER_TIMEOUT_MS = 500;
	private static final long COORDINATOR_MSG_TIMEOUT_MS = 750;
	
	private int id;
	private Random randomNumberGen;
	private int transactionId;
	private ElectionTracker tracker;
	private ElectionComms comms;
	private Logger logger;
	
	private MessageListener<ElectionAnswerMessage> answerListener;
	private MessageListener<CoordinatorMessage> coordinatorListener;
	
	private BlockingQueue<ElectionAnswerMessage> electionAnswers;
	private BlockingQueue<CoordinatorMessage> coordinatorMessages;
	
	public ElectionNotifierThread(int currentNodeId, ElectionTracker tracker, ElectionComms comms, Logger logger) {
		this.id = currentNodeId;
		this.randomNumberGen = new Random(System.currentTimeMillis());
		this.comms = comms;
		this.tracker = tracker;
		this.logger = logger;
		this.answerListener = new ElectionAnswerListener();
		this.coordinatorListener = new CoordinatorListener();
		this.comms.electionAnswerNotifier.addListener(answerListener);
		this.comms.coordinatorNotifier.addListener(coordinatorListener);
		this.electionAnswers = new LinkedBlockingQueue<ElectionAnswerMessage>();
		this.coordinatorMessages = new LinkedBlockingQueue<CoordinatorMessage>();
	}
	
	@Override
	public void run() {		
		logger.log("Starting a new election...");
		// P broadcasts an election message (inquiry) to all other processes with higher process IDs, expecting an "I am alive" response from them if they are alive.
		this.transactionId = this.randomNumberGen.nextInt();
		ElectionMessage e = new ElectionMessage(this.id, this.transactionId);
		try {
			this.electionAnswers.clear();
			this.coordinatorMessages.clear();
			this.comms.electionComms.send(e);
		} catch (IOException ioException) {
			logger.error("ERROR (ElectionNotifierThread-" + this.id +"): " + ioException);
		}
		
		try {
			// Wait for a certain amount of time for answers
			ElectionAnswerMessage msg = this.electionAnswers.poll(ELECTION_ANSWER_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			
			// If it hears from 1 (or more), wait for a coordinator msg
			if (msg != null) {
				this.logger.log("Received an election answer from " + msg.getSenderId());
				CoordinatorMessage coordMsg = this.coordinatorMessages.poll(COORDINATOR_MSG_TIMEOUT_MS, TimeUnit.MILLISECONDS);
				// If we get a coordinator message, we have a new leader
				if (coordMsg != null) {
					this.logger.log("Found a new leader: " + msg.getSenderId());
					this.tracker.onNewLeader(coordMsg.getSenderId());
				}
				// Restart the election process :-(
				else {
					this.logger.log("Never got the coordinator message, restarting the election");
					this.tracker.startNewElection();
				}
			} 
			// If it doesn't hear back from anyone, it is the leader
			else {
				try {
					this.logger.log("Didn't receive an election response, I'm the leader!");
					this.comms.coordinatorComms.send(new CoordinatorMessage(this.id));
					this.tracker.onNewLeader(this.id);
				} catch (IOException e1) {
					logger.error("ERROR (ElectionNotifierThread-ElectionResponse" + this.id +"): " + e1);
				}
			}
		} catch (InterruptedException ex) {
			logger.debug("DEBUG: (ElectionNotifierThread-ElectionResponse" + this.id + ")" + ex);
		}
		
		this.comms.electionAnswerNotifier.removeListener(this.answerListener);
		this.comms.coordinatorNotifier.removeListener(this.coordinatorListener);
		this.tracker.onElectionEnd();
	}
	
	private class ElectionAnswerListener implements MessageListener<ElectionAnswerMessage> {
		@Override
		public void notifyMessage(ElectionAnswerMessage message) {
			if (message.getDestinationNodeId() == ElectionNotifierThread.this.id && 
				message.getTransactionId() == ElectionNotifierThread.this.transactionId) {
				ElectionNotifierThread.this.electionAnswers.add(message);
			}
		}
	}
	
	private class CoordinatorListener implements MessageListener<CoordinatorMessage> {
		@Override
		public void notifyMessage(CoordinatorMessage message) {
			ElectionNotifierThread.this.coordinatorMessages.add(message);
		}
	}
	
}
