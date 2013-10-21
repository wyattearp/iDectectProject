package edu.uc.cs.distsys.ilead;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Executors;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.comms.MessageListener;
import edu.uc.cs.distsys.comms.MulticastWrapper;
import edu.uc.cs.distsys.comms.NotifyThread;

public class LeaderMain implements ElectionTracker {

	private static final int ELECTION_MSG_PORT = 5100;
	private static final int ELECTION_ANSWER_MSG_PORT = 5200;
	private static final int COORD_MSG_PORT = 5300;
	
	private final int myId;	
	private List<LeaderChangeListener> newLeaderListeners;
	private Logger logger;
	
	private ElectionComms electionComms;
	private Thread electionMsgThread;
	private Thread electionAnswerThread;
	private Thread coordinatorThread;
	
	private Thread electionThread;
	
	public LeaderMain(int nodeId, List<LeaderChangeListener> leaderListeners, Logger logger) throws UnknownHostException {
		this.myId = nodeId;
		this.logger = logger;
		this.newLeaderListeners = leaderListeners;
				
		this.electionComms = new ElectionComms();
		this.electionComms.electionComms = new MulticastWrapper<ElectionMessage>(
				ELECTION_MSG_PORT, myId, new ElectionMessage.ElectionFactory(), logger);
		this.electionComms.electionAnswerComms = new MulticastWrapper<ElectionAnswerMessage>(
				ELECTION_ANSWER_MSG_PORT, myId, new ElectionAnswerMessage.ElectionAnswerFactory(), logger);
		this.electionComms.coordinatorComms = new MulticastWrapper<CoordinatorMessage>(
				COORD_MSG_PORT, myId, new CoordinatorMessage.CoordinatorFactory(), logger);

		this.electionComms.electionNotifier = new NotifyThread<ElectionMessage>(nodeId, this.electionComms.electionComms, new ElectionListener(), logger);
		this.electionComms.electionAnswerNotifier = new NotifyThread<ElectionAnswerMessage>(nodeId, this.electionComms.electionAnswerComms, null, logger);
		this.electionComms.coordinatorNotifier = new NotifyThread<CoordinatorMessage>(nodeId, this.electionComms.coordinatorComms, new CoordinatorListener(), logger);
		
		this.electionMsgThread = Executors.defaultThreadFactory().newThread(electionComms.electionNotifier);
		this.electionAnswerThread = Executors.defaultThreadFactory().newThread(electionComms.electionAnswerNotifier);
		this.coordinatorThread = Executors.defaultThreadFactory().newThread(electionComms.coordinatorNotifier);
	}

	@Override
	public void start() {
		this.electionMsgThread.start();
		this.electionAnswerThread.start();
		this.coordinatorThread.start();
	}

	public void stop() {
		this.electionComms.electionComms.close();
		this.electionComms.electionAnswerComms.close();
		this.electionComms.coordinatorComms.close();
		this.electionMsgThread.interrupt();
		this.electionAnswerThread.interrupt();
		this.coordinatorThread.interrupt();
	}

	@Override
	public void startNewElection() {
		this.electionThread = Executors.defaultThreadFactory().newThread(
				new ElectionNotifierThread(this.myId, this, this.electionComms, this.logger));
		this.electionThread.start();
	}
	
	@Override
	public void onNewLeader(int leaderId) {
		for (LeaderChangeListener listener : this.newLeaderListeners)
			listener.onNewLeader(leaderId);
	}
	
	private class ElectionListener implements MessageListener<ElectionMessage> {
		@Override
		public void notifyMessage(ElectionMessage message) {
			// Only respond to election requests from lower processes
			if (message.getSenderId() < LeaderMain.this.myId) {
				try {
					LeaderMain.this.logger.log("Received a election message: " + message.getSenderId());
					LeaderMain.this.electionComms.electionAnswerComms.send(
							new ElectionAnswerMessage(LeaderMain.this.myId, message.getSenderId(), message.getTransactionId()));
					LeaderMain.this.startNewElection();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private class CoordinatorListener implements MessageListener<CoordinatorMessage> {
		@Override
		public void notifyMessage(CoordinatorMessage message) {
			LeaderMain.this.logger.log("Received notification of a new leader: " + message.getSenderId());
			LeaderMain.this.onNewLeader(message.getSenderId());
		}
	}
}
