package edu.uc.cs.distsys.ilead;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.comms.CommsWrapper;
import edu.uc.cs.distsys.comms.MessageListener;
import edu.uc.cs.distsys.comms.MulticastWrapper;
import edu.uc.cs.distsys.comms.NotifyThread;
import edu.uc.cs.distsys.ilead.Election.ElectionFactory;

/*
 * EXAMPLE CLASS
 * 
 * This demonstrates how to add comm classes
 */
public class LeaderMain implements ElectionTracker /*implements MessageListener<Election>*/ {

	private final int myId;
	
	private CommsWrapper<ElectionMessage> electionComms;
	private CommsWrapper<ElectionAnswerMessage> electionAnswerComms;
	private CommsWrapper<CoordinatorMessage> coordinatorComms;
	private Thread electionThread;
	private Thread electionAnswerThread;
	private Thread coordinatorThread;
	
	public LeaderMain(int nodeId, Logger logger) {
		this.myId = nodeId;
		this.electionComms = new MulticastWrapper<>(1234, nodeId, new ElectionMessage.ElectionFactory(), logger);
		this.electionAnswerComms = new MulticastWrapper<>(1235, nodeId, new ElectionAnswerMessage.ElectionAnswerFactory(), logger);
		this.coordinatorComms = new MulticastWrapper<>(1236, nodeId, new CoordinatorMessage.CoordinatorFactory(), logger);
		this.electionThread = Executors.defaultThreadFactory().newThread(
				new NotifyThread<>(nodeId, electionComms, TODO, logger));
		this.electionAnswerThread = Executors.defaultThreadFactory().newThread(
				new NotifyThread<>(nodeId, electionComms, TODO, logger));
		this.coordinatorThread = Executors.defaultThreadFactory().newThread(
				new NotifyThread<>(nodeId, electionComms, TODO, logger));
	}

	public void start() {
		this.electionThread.start();
		this.electionAnswerThread.start();
		this.coordinatorThread.start();
	}

	public void stop() {
		this.electionThread.interrupt();
		this.electionAnswerThread.interrupt();
		this.coordinatorThread.interrupt();
	}

	@Override
	public void answerElectionQuery(ElectionMessage msg) {
		try {
			this.electionAnswerComms.send(new ElectionAnswerMessage(this.myId, msg.getSenderId(), msg.getTransactionId()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void startNewElection() {
		// TODO Auto-generated method stub
		
	}
	
}
