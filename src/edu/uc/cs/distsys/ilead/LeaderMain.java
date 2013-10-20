package edu.uc.cs.distsys.ilead;

import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.comms.CommsWrapper;
import edu.uc.cs.distsys.comms.MessageListener;
import edu.uc.cs.distsys.comms.MulticastWrapper;
import edu.uc.cs.distsys.comms.NotifyThread;

/*
 * EXAMPLE CLASS
 * 
 * This demonstrates how to add comm classes
 */
public class LeaderMain /*implements MessageListener<Election>*/ {

	private CommsWrapper<Election> electionComms;
	private Thread electionThread;
	private Thread electionAnswerThread;
	private Thread coordinatorThread;
	
	public LeaderMain(int nodeId, Logger logger) {
		this.electionComms = new MulticastWrapper<Election>(1234, nodeId, new Election.ElectionFactory(), logger);
		this.electionThread = Executors.defaultThreadFactory().newThread(
				new NotifyThread<Election>(nodeId, electionComms, TODO, logger));
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
	
}
