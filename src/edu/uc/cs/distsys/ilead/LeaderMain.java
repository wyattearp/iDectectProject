package edu.uc.cs.distsys.ilead;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.comms.MessageDroppedException;
import edu.uc.cs.distsys.comms.MessageHandler;
import edu.uc.cs.distsys.comms.MulticastWrapper;
import edu.uc.cs.distsys.comms.NotifyThread;

public class LeaderMain implements ElectionManager {

	private static final int ELECTION_MSG_PORT = 5100;
	private static final int ELECTION_ANSWER_MSG_PORT = 5200;
	private static final int COORD_MSG_PORT = 5300;
	
	private final int myId;	
	
	private List<LeaderChangeListener> newLeaderListeners;
	private List<ElectionMonitor> monitorList;
	
	private Logger logger;
	
	private ElectionComms electionComms;
	private Thread electionMsgThread;
	private Thread electionAnswerThread;
	private Thread coordinatorThread;
	
	private boolean electionInProgress;
//	private Thread electionThread;
	private Lock electionLock;
	private BlockingQueue<Thread> activeElectionThreads;
	private BlockingQueue<Thread> pendingElectionThreads;

	public LeaderMain(int nodeId, List<LeaderChangeListener> leaderListeners, Logger logger) throws UnknownHostException {
		this.myId = nodeId;
		this.logger = logger;
		this.newLeaderListeners = leaderListeners;
		this.monitorList = new LinkedList<ElectionMonitor>();
		this.electionLock = new ReentrantLock();
		this.activeElectionThreads = new LinkedBlockingQueue<Thread>();
		this.pendingElectionThreads = new LinkedBlockingQueue<Thread>();
		this.electionInProgress = false;
		
		this.electionComms = new ElectionComms();
		this.electionComms.electionComms = new MulticastWrapper<ElectionMessage>(
				ELECTION_MSG_PORT, myId, new ElectionMessage.ElectionFactory(), logger);
		this.electionComms.electionAnswerComms = new MulticastWrapper<ElectionAnswerMessage>(
				ELECTION_ANSWER_MSG_PORT, myId, new ElectionAnswerMessage.ElectionAnswerFactory(), logger);
		this.electionComms.coordinatorComms = new MulticastWrapper<CoordinatorMessage>(
				COORD_MSG_PORT, myId, new CoordinatorMessage.CoordinatorFactory(), logger);

		this.electionComms.electionNotifier = new NotifyThread<ElectionMessage>(nodeId, this.electionComms.electionComms, new ElectionListener(logger), logger);
		this.electionComms.electionAnswerNotifier = new NotifyThread<ElectionAnswerMessage>(nodeId, this.electionComms.electionAnswerComms, null, logger);
		this.electionComms.coordinatorNotifier = new NotifyThread<CoordinatorMessage>(nodeId, this.electionComms.coordinatorComms, new CoordinatorListener(logger), logger);
		
		this.electionMsgThread = Executors.defaultThreadFactory().newThread(electionComms.electionNotifier);
		this.electionAnswerThread = Executors.defaultThreadFactory().newThread(electionComms.electionAnswerNotifier);
		this.coordinatorThread = Executors.defaultThreadFactory().newThread(electionComms.coordinatorNotifier);
	}
	
	@Override
	public void addMonitor(ElectionMonitor newMonitor) {
		this.monitorList.add(newMonitor);
	}

	@Override
	public void start() {
		this.electionMsgThread.start();
		this.electionAnswerThread.start();
		this.coordinatorThread.start();
	}

	@Override
	public void stop() {
		this.electionComms.electionComms.close();
		this.electionComms.electionAnswerComms.close();
		this.electionComms.coordinatorComms.close();
		this.electionMsgThread.interrupt();
		this.electionAnswerThread.interrupt();
		this.coordinatorThread.interrupt();
		for (Thread t : pendingElectionThreads)
			t.interrupt();
		for (Thread t : activeElectionThreads)
			t.interrupt();
	}

	@Override
	public void startNewElection() {
		Thread electionThread = Executors.defaultThreadFactory().newThread(
				new ElectionNotifierThread(this.myId, this, this.electionComms, this.logger));
		this.pendingElectionThreads.add(electionThread);
		electionThread.start();
	}
	
	@Override
	public void onElectionThreadStart() throws InterruptedException {
		if (! this.electionLock.tryLock()) {
			logger.error("TRIED TO START ANOTHER ELECTION THREAD");
			this.electionLock.lockInterruptibly();
		}
		logger.error("Election thread has started");
		this.electionInProgress = true;
		this.pendingElectionThreads.remove(Thread.currentThread());
		this.activeElectionThreads.add(Thread.currentThread());
		for (ElectionMonitor monitor : this.monitorList) {
			monitor.onElectionStart(this.myId);
		}
	}
	
	@Override
	public void onElectionThreadEnd() {
		this.electionLock.unlock();
		logger.error("Election thread has ended");
		this.electionInProgress = false;
	}
	
	@Override
	public void onNewLeader(int leaderId) {
		if (leaderId < this.myId) {
			startNewElection();
		} else {
			for (ElectionMonitor monitor : this.monitorList)
				monitor.onElectionEnd(myId, leaderId);
			for (LeaderChangeListener listener : this.newLeaderListeners)
				listener.onNewLeader(leaderId);
			for (Thread t : this.pendingElectionThreads)
				t.interrupt();
		}
	}
	
	@Override
	public void onLeaderFailed() {
logger.error("LEADER FAILED!");
		for (LeaderChangeListener listener : this.newLeaderListeners)
			listener.onLeaderFailed();
		if (! electionInProgress) {
			startNewElection();
		}
	}
	
	private class ElectionListener extends MessageHandler<ElectionMessage> {
		public ElectionListener(Logger logger) {
			super(logger);
		}

		@Override
		public void handleMessage(ElectionMessage message) {
			// Only respond to election requests from lower processes
			if (message.getSenderId() < LeaderMain.this.myId) {
				try {
					LeaderMain.this.logger.log("Received a election message: " + message.getSenderId());
					LeaderMain.this.electionComms.electionAnswerComms.send(
							new ElectionAnswerMessage(LeaderMain.this.myId, message.getSenderId(), message.getTransactionId()));
					LeaderMain.this.startNewElection();
				} catch (MessageDroppedException mde) {
					logger.debug("ERROR: " + mde);
				} catch (IOException e) {
					logger.error("ERROR (LeaderMain-ElectionListener): " + e);
				}
			}
		}
	}
	
	private class CoordinatorListener extends MessageHandler<CoordinatorMessage> {
		public CoordinatorListener(Logger logger) {
			super(logger);
		}

		@Override
		public void handleMessage(CoordinatorMessage message) {
			LeaderMain.this.logger.debug("Received notification of a new leader: " + message.getSenderId());
			LeaderMain.this.onNewLeader(message.getSenderId());
		}
	}
}
