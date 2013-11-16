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
import edu.uc.cs.distsys.comms.MessageHandler;
import edu.uc.cs.distsys.comms.NotifyThread;
import edu.uc.cs.distsys.ilead.ElectionManager;
import edu.uc.cs.distsys.ilead.ElectionMonitor;
import edu.uc.cs.distsys.ilead.LeaderChangeListener;
import edu.uc.cs.distsys.ilead.LeaderMain;
import edu.uc.cs.distsys.init.GroupJoinException;
import edu.uc.cs.distsys.init.GroupManager;
import edu.uc.cs.distsys.properties.PropertiesManager;
import edu.uc.cs.distsys.ui.NodeStatusViewThread;

public class DetectMain implements LeaderChangeListener, FailureListener {

	private class HeartbeatListener extends MessageHandler<Heartbeat> {
		public HeartbeatListener(Logger logger) {
			super(logger);
		}
	
		@Override
		public void handleMessage(Heartbeat status) {
			try {
				DetectMain.this.heartbeatLock.lock();
				if (!nodes.containsKey(status.getNodeId())) {
					logger.log("Discovered new node - " + status.getNodeId());
					Node n = new Node(status);
					DetectMain.this.nodes.put(status.getNodeId(), n);
					DetectMain.this.statusViewThread.addMonitoredNode(n);
				} else {
					logger.debug("Received heartbeat from node " + status.getNodeId());
					if (DetectMain.this.nodes.get(status.getNodeId()).updateStatus(status)) {
						// TODO: check leader id of other node? 
						if (DetectMain.this.myNode.getLeaderId() == DetectMain.this.myNode.getId() && DetectMain.this.myNode.getLeaderId() > status.getLeaderId())
							DetectMain.this.electionMgr.startNewElection();
						// Go through all reported failed nodes and update local state if necessary
						for (Node failNode : status.getFailedNodes()) {
							DetectMain.this.verifyFailedNode(failNode);
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
	private Thread detectorThread;
	private Thread uiThread;
	private LogHelper logger;
	private NodeStatusViewThread statusViewThread;
	private ElectionManager electionMgr;
	private Node myNode;
	private HeartbeatThread hbThread;
	private HeartbeatListener hbListener;
	private PropertiesManager nodeProperties;
	private GroupManager groupManager;

	public DetectMain(int nodeId, List<Integer> peers) {
		this.logger = new LogHelper(nodeId, System.out, System.err, null);
		this.nodes = new HashMap<Integer, Node>();
		this.failedNodes = new LinkedList<Node>();
		this.heartbeatLock = new ReentrantLock();
		// load up stored properties if available
		this.myNode = new Node(nodeId);
		this.nodeProperties = new PropertiesManager(myNode, this.logger);
		if (!this.nodeProperties.load()) {
			// unable to load properties from file, we're starting fresh
			this.nodeProperties.save();
		} else {
			// there's previously stored data, use that node inst
			this.myNode = this.nodeProperties.getNode();

		}
		// TODO: these methods need to be called when / where we get updates
		// this.nodeProperties.setProperties(this.myNode);
		// this.nodeProperties.save();
		this.scheduledExecutor = new ScheduledThreadPoolExecutor(1);
		this.statusViewThread = new NodeStatusViewThread(this.myNode.getId());
		this.uiThread = new Thread(statusViewThread);
		
		if (peers != null) {
			for (int peer : peers) {
				this.nodes.put(peer, new Node(peer));
			}
		}
	}

	public void start() throws UnknownHostException, GroupJoinException {
		this.uiThread.start();
		
		// Join a group
		this.groupManager = new GroupManager(myNode, logger);
		this.groupManager.locateAndJoinGroup();
		
		this.hbListener = new HeartbeatListener(logger);
		this.hbThread = new HeartbeatThread(this.myNode.getId(), failedNodes, heartbeatLock, logger); 
		this.detectorThread = Executors.defaultThreadFactory().newThread(
				new NotifyThread<Heartbeat>(this.myNode.getId(), hbThread.getCommsWrapper(), hbListener, logger));
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
		this.scheduledExecutor.shutdownNow();
		this.hbThread.getCommsWrapper().close();
		this.detectorThread.interrupt();
		this.hbListener.stop();
		this.uiThread.interrupt();
		this.electionMgr.stop();
		this.nodeProperties.save();
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
	
	public void setGroupId(int groupId) {
		this.myNode.setGroupId(groupId);
	}
	
	public int getId() {
		return this.myNode.getId();
	}
	
	/***
	 * @return Number of known group members including self
	 */
	public int getNumGroupNodes() {
		return this.nodes.size() + 1;
	}
	
	private void verifyFailedNode(Node node) {
		if (node.getId() == this.myNode.getId())
			return;
		try {
			this.heartbeatLock.lock();
			if (!nodes.containsKey(node.getId())) {
				logger.log("Discovered new node (offline) - " + node.getId());
				this.nodes.put(node.getId(), Node.createFailedNode(node.getId(), node.getState(), node.getSeqHighWaterMark()));
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
		int group = 0;
		if (args.length < 1) {
			//System.err.println("Usage: " + args[0] + "<port#> [peer#1] ... [peer#N]");
			// DEBUGGING
			//port = new Random(System.currentTimeMillis()).nextInt(1000) + 1024;
			node = new Random(System.currentTimeMillis()).nextInt(1000);
		} else {
			if (args.length >= 1) {
				// first arg is node id
				node = Integer.parseInt(args[0]);
			}
			if (args.length >= 2) {
				// second arg is group id
				group = Integer.parseInt(args[1]);
			}
		}
		
		// DEBUG FOR TESTING!!!
		//System.getProperties().setProperty("packetloss", "20");
		
		List<Integer> peers = new LinkedList<Integer>();
		for (int i = 1; i < args.length; i++) {
			peers.add(Integer.parseInt(args[i]));
		}
				
		try {
			final DetectMain detector = new DetectMain(node, peers);
			detector.setGroupId(group);
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
		} catch (GroupJoinException e) {
			System.err.println("Unable to join group: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
