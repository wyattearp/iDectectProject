package edu.uc.cs.distsys.idetect;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.uc.cs.distsys.LogHelper;
import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.Node;
import edu.uc.cs.distsys.NodeState;
import edu.uc.cs.distsys.NodeStateChangeListener;
import edu.uc.cs.distsys.comms.MessageHandler;
import edu.uc.cs.distsys.comms.NotifyThread;
import edu.uc.cs.distsys.ilead.ElectionManager;
import edu.uc.cs.distsys.ilead.ElectionMonitor;
import edu.uc.cs.distsys.ilead.LeaderChangeListener;
import edu.uc.cs.distsys.ilead.LeaderMain;
import edu.uc.cs.distsys.init.GroupJoinException;
import edu.uc.cs.distsys.init.GroupManager;
import edu.uc.cs.distsys.ui.NodeStatusViewThread;

public class DetectMain implements LeaderChangeListener, FailureListener, NodeStateChangeListener {

	private class HeartbeatListener extends MessageHandler<Heartbeat> {
		public HeartbeatListener(Logger logger) {
			super(Heartbeat.class, logger);
		}
	
		@Override
		public void handleMessage(Heartbeat status) {
			try {
				DetectMain.this.heartbeatLock.lock();
				if (!nodes.containsKey(status.getNodeId())) {
					logger.log("Discovered new node - " + status.getNodeId());
					Node n = new Node(status, DetectMain.this);
					DetectMain.this.nodes.put(status.getNodeId(), n);
					DetectMain.this.statusViewThread.addMonitoredNode(n);
				} else {
					logger.debug("Received heartbeat from node " + status.getNodeId());
					Node reportingNode = DetectMain.this.nodes.get(status.getNodeId()); 
					NodeState oldState = reportingNode.getState();
					if (reportingNode.updateStatus(status, DetectMain.this.myNode.getNumProcOperating())) {
						if (oldState != reportingNode.getState()) {
							DetectMain.this.onNodeStateChanged(reportingNode, oldState);							
						}
						if (!reportingNode.getState().equals(NodeState.INCOHERENT)) {
							// TODO: check leader id of other node? 
							if (DetectMain.this.myNode.getLeaderId() == DetectMain.this.myNode.getId() && 
									DetectMain.this.myNode.getLeaderId() > status.getLeaderId())
								DetectMain.this.electionMgr.startNewElection();
							// Go through all reported failed nodes and update local state if necessary
							for (Node failNode : status.getFailedNodes()) {
								DetectMain.this.verifyFailedNode(failNode);
							}
						}
					} else {
						logger.error("Warning - Received out-of-order heartbeat from node " + status.getNodeId());
					}
				}
				DetectMain.this.statusViewThread.updateUI();
			} finally {
				DetectMain.this.heartbeatLock.unlock();
			}
		}		
	}
	
	private static final long HB_INIT_DELAY		    = 0;
	private static final long FAIL_DETECT_PERIOD_MS = 750;
	public static final long  HB_PERIOD_MS			= 500;

	private final ScheduledExecutorService scheduledExecutor;

	private Lock heartbeatLock;
	private HashMap<Integer, Node> nodes;
	private List<Node> failedNodes;
	private List<Node> incoherentNodes;
	private Thread detectorThread;
	private Thread uiThread;
	private LogHelper logger;
	private NodeStatusViewThread statusViewThread;
	private ElectionManager electionMgr;
	private Node myNode;
	private HeartbeatThread hbThread;
	private HeartbeatListener hbListener;
	private GroupManager groupManager;
	private boolean consensusPossible;

	public DetectMain(String nodeName, int nodeId, List<Integer> peers) {
		this.logger = new LogHelper(nodeId, System.out, System.err, null);
		this.nodes = new HashMap<Integer, Node>();
		this.failedNodes = new LinkedList<Node>();
		this.incoherentNodes = new LinkedList<Node>();
		this.heartbeatLock = new ReentrantLock();
		// load up stored properties if available
		this.myNode = new Node(nodeName, nodeId, NodeState.ONLINE);
		this.scheduledExecutor = new ScheduledThreadPoolExecutor(1);
		this.statusViewThread = new NodeStatusViewThread(this.myNode);
		this.uiThread = new Thread(statusViewThread);
		this.consensusPossible = false;
		
//		if (peers != null) {
//			for (int peer : peers) {
//				this.nodes.put(peer, new Node(peer));
//			}
//		}
	}
	
	public DetectMain(int nodeId, List<Integer> peers) {
		this("", nodeId, peers);
	}
	
	public DetectMain(String nodeName, int nodeId, List<Integer> peers, int numProcOperating) {
		this(nodeName, nodeId, peers);
		this.myNode.setNumProcOperating(numProcOperating);
	}

	public void start() throws UnknownHostException, GroupJoinException {		
		// Join a group
		this.groupManager = new GroupManager(myNode, logger);
		this.groupManager.locateAndJoinGroup();

		this.uiThread.start();

		this.hbListener = new HeartbeatListener(logger);
		this.hbThread = new HeartbeatThread(this.myNode, failedNodes, heartbeatLock, logger); 
		this.detectorThread = Executors.defaultThreadFactory().newThread(
				new NotifyThread<Heartbeat>(this.myNode.getId(), hbThread.getCommsWrapper(), hbListener, Heartbeat.class, logger));
		this.detectorThread.start();
		this.scheduledExecutor.scheduleAtFixedRate(hbThread, HB_INIT_DELAY, HB_PERIOD_MS, TimeUnit.MILLISECONDS);
		FailureDetectionThread fdt = new FailureDetectionThread(nodes, heartbeatLock, this);
		this.scheduledExecutor.scheduleAtFixedRate(fdt, HB_INIT_DELAY, FAIL_DETECT_PERIOD_MS, TimeUnit.MILLISECONDS);
		
		List<LeaderChangeListener> listeners = new LinkedList<LeaderChangeListener>();
		listeners.add(this);
		listeners.add(hbThread);
		this.electionMgr = new LeaderMain(this.myNode.getId(), listeners, logger);
		this.electionMgr.start();
		this.electionMgr.startNewElection();
	}
	
	public void stop() {
		if (this.groupManager != null) this.groupManager.shutdown();
		this.scheduledExecutor.shutdownNow();
		if (this.hbThread != null) this.hbThread.getCommsWrapper().close();
		if (this.detectorThread != null) this.detectorThread.interrupt();
		if (this.hbListener != null) this.hbListener.stop();
		this.uiThread.interrupt();
		if (this.electionMgr != null) this.electionMgr.stop();
		this.logger.log("Detector shutting down");
		this.logger.close();
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
	public void addElectionMonitor(ElectionMonitor monitor) {
		this.electionMgr.addMonitor(monitor);
	}
	
	@Override
	public void onFailedNode(Node failed) {
		try {
			this.heartbeatLock.lock();
			this.failedNodes.add(failed);
		} finally {
			this.heartbeatLock.unlock();
		}
//logger.error("NODE FAILED: " + failed);
		if (failed.getId() == myNode.getLeaderId())
			this.electionMgr.onLeaderFailed();
		this.statusViewThread.updateUI();
	}
	
	@Override
	public void onNewLeader(int leaderId) {
		this.logger.log("New Leader: " + leaderId);
		if (this.myNode.getId() == leaderId) {
			// update our UI to say we're the current user
			this.statusViewThread.setUIMessage("Currently The Leader");
		} else {
			this.statusViewThread.setUIMessage("Leader = " + leaderId);
		}
		// update who we believe the leader is
		this.myNode.setLeaderId(leaderId);
	}
	
	@Override
	public void onLeaderFailed() {
		this.logger.log("Leader " + myNode.getLeaderId() + " has failed!");
		myNode.setLeaderId(0);
	}
	
	public int getLeaderId() {
		return this.myNode.getLeaderId();
	}
	
	public int getGroupId() {
		return this.myNode.getGroupId();
	}
	
//	public void setGroupId(int groupId) {
//		this.myNode.setGroupId(groupId);
//	}
	
	public int getId() {
		return this.myNode.getId();
	}
	
	/***
	 * @return Number of known group members including self
	 */
	public int getNumGroupNodes() {
		return this.nodes.size() + 1;
	}

	@Override
	public void onNodeStateChanged(Node n, NodeState oldState) {
		this.logger.log("Node " + n.getId() + " has changed state from " + oldState + " => " + n.getState());
		switch(n.getState()) {
			case ONLINE:
			case OFFLINE:
				synchronized (this.incoherentNodes) {
					this.incoherentNodes.remove(n);					
				}
				break;
			case INCOHERENT:
				synchronized (this.incoherentNodes) {
					if (!this.incoherentNodes.contains(n)) {
						this.incoherentNodes.add(n);
					}
				}
				this.electionMgr.excludeNodeFromElections(n);
				break;
			case SUSPECT:
				break;
			case UNKNOWN:
				throw new IllegalStateException("Node has entered unknown state: " + n);
		}
		// Check if we are capable of reaching consensus
		int minCorrectNodes = 2 * myNode.getNumProcOperating() / 3 + 1;
		int numCorrectNodes = 1;	// always count myself
		for (Node node : this.nodes.values()) {
			if (node.getState().equals(NodeState.ONLINE)) {
				numCorrectNodes++;
			}
		}
		if (numCorrectNodes < minCorrectNodes) {
			this.consensusPossible = false;
			this.logger.log("Consensus is not possible (need " + minCorrectNodes + ", have " + numCorrectNodes + ")");
		} else if (! this.consensusPossible) {
			this.consensusPossible = true;
			this.logger.log("Consensus is possible (have " + numCorrectNodes + "/" + myNode.getNumProcOperating() + " correct nodes)");
		}
	}
	
	/**
	 * @return Number of nodes with the same group ID as this node, including self
	 */
	public int getNumSameGroupNodes() {
		int numSameGroup = 0;
		for (Node curNode : this.nodes.values()) {
			if (curNode.getGroupId() == this.myNode.getGroupId()) {
				numSameGroup++;
			}
		}
		return numSameGroup + 1;
	}
	
	/**
	 * @return Number of nodes with the same group ID as this node which are currently in the Online state, including self
	 */
	public int getNumOnlineSameGroupNodes() {
		int numOnlineSameGroupNodes = 0;
		for (Node curNode : this.nodes.values()) {
			if (curNode.getGroupId() == this.myNode.getGroupId() && curNode.getState().equals(NodeState.ONLINE)) {
				numOnlineSameGroupNodes++;
			}
		}
		return numOnlineSameGroupNodes + 1;
	}
	
	public void setNumOperatingProcs(int numProcOperating) {
		this.myNode.setNumProcOperating(numProcOperating);
	}
	
	private void verifyFailedNode(Node node) {
		if (node.getId() == this.myNode.getId())
			return;
		try {
			this.heartbeatLock.lock();
			if (!nodes.containsKey(node.getId())) {
				logger.log("Discovered new node (offline) - " + node.getId());
				this.nodes.put(node.getId(), Node.createFailedNode(node.getId(), node.getState(), node.getSeqHighWaterMark(), this));
				this.onNodeStateChanged(node, NodeState.UNKNOWN);
			} else {
				Node localNode = nodes.get(node.getId());
				if (! localNode.isOffline() && localNode.getSeqHighWaterMark() <= node.getSeqHighWaterMark()) {
					//update our node
					localNode.updateState(node);
					// Check if the node has been marked as failed
					if (localNode.isOffline())
						onFailedNode(localNode);
					// If it was the leader, we need to elect a new one
//					if (localNode.getId() == myNode.getLeaderId()) {
//						this.electionMgr.onLeaderFailed();
//					}
				} else if (! localNode.isOffline() && ! node.getState().equals(NodeState.SUSPECT)) {
					//discard out-of-date info
					//DEBUG
					logger.debug("Reported failed node is actually online (id=" + node.getId() + ")");
				}
			}
			this.statusViewThread.updateUI();
		} finally {
			this.heartbeatLock.unlock();
		}
	}
	
	public static void main(String[] args) {
		int node = 0;
//		int group = 0;
		String name = "";
		int numProcOperating = 0;
		if (args.length < 1) {
			//System.err.println("Usage: " + args[0] + "<port#> [peer#1] ... [peer#N]");
			// DEBUGGING
			//port = new Random(System.currentTimeMillis()).nextInt(1000) + 1024;
			node = new Random(System.currentTimeMillis()).nextInt(1000);
			numProcOperating = 9;
		} else {
			if (args.length >= 1) {
				// first arg is node id
				node = Integer.parseInt(args[0]);
			}
//			if (args.length >= 2) {
//				// second arg is group id
//				group = Integer.parseInt(args[1]);
//			}
			if (args.length >= 2) {
				// second arg is the node name
				name = args[1];
			}
			if (args.length >= 3) {
				// third arg is the number of processes
				numProcOperating = Integer.parseInt(args[2]);
			} 
		}
		// TODO: does this peer thing even work?? - WN
		List<Integer> peers = new LinkedList<Integer>();
		for (int i = 3; i < args.length; i++) {
			peers.add(Integer.parseInt(args[i]));
		}
		final DetectMain detector = new DetectMain(name, node, peers, numProcOperating);
		try {
			//detector.setGroupId(group);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					detector.stop();
				}
			});
			detector.start();
		} catch (UnknownHostException e) {
			System.out.println("Something horrible happened and there was nothing we could do about it");
			e.printStackTrace();
			detector.stop();
		} catch (GroupJoinException e) {
			System.err.println("Unable to join group: " + e.getMessage());
			e.printStackTrace();
			detector.stop();		
		}
		
		//Debugging
//		try { Thread.sleep(5000); } catch (Throwable t) {}
//		detector.stop();
	}

	public boolean isConsensusPossible() {
		return consensusPossible;
	}

	public Node getMyNode() {
		return myNode;
	}

}
