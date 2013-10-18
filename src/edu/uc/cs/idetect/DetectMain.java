package edu.uc.cs.idetect;

import java.io.IOException;
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

public class DetectMain implements HeartbeatListener {

	private static final long HB_INIT_DELAY		 = 0;
	private static final long FAIL_DETECT_PERIOD = 5;
	static final long HB_PERIOD			 		 = 1;

	private static final String MCAST_GROUP_IP = "224.0.0.224";
		
	private final int nodeId;
	private final ScheduledExecutorService scheduledExecutor;

	private int nextSeqNum;

	private CommsWrapper commWrapper;
	private Lock heartbeatLock;
	private HashMap<Integer, Node> nodes;
	private List<Node> failedNodes;
	private Thread detectorThread;
	private LogHelper logger;
	private NodeStatusViewThread statusViewThread;

	public DetectMain(int nodeId, int port, List<Integer> peers) throws UnknownHostException {
		
		
		this.logger = new LogHelper(nodeId, System.out, System.err, null);
		this.nodeId = nodeId;
		this.commWrapper = new MulticastWrapper(MCAST_GROUP_IP, port, nodeId, logger);
		this.nodes = new HashMap<Integer, Node>();
		this.failedNodes = new LinkedList<Node>();
		this.heartbeatLock = new ReentrantLock();
		this.scheduledExecutor = new ScheduledThreadPoolExecutor(1);	//TODO
		this.statusViewThread = new NodeStatusViewThread(this.nodeId); // TODO: move to where this will finally be
		new Thread(statusViewThread).start();
		for (int peer : peers) {
			this.nodes.put(peer, new Node(peer));
		}
	}

	public void start() {
		this.detectorThread = Executors.defaultThreadFactory().newThread(
				new DetectorThread(this.nodeId, this.commWrapper, this, logger));
		this.detectorThread.start();
		
		this.scheduledExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				long curTime = System.currentTimeMillis();
				try {
					heartbeatLock.lock();
					//List<Node> failedNodes = getFailedNodes(curTime);
					commWrapper.send(new Heartbeat(nodeId, nextSeqNum++, curTime, failedNodes));
					failedNodes.clear();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("ERROR: " + e);
					//e.printStackTrace();
				} finally {
					heartbeatLock.unlock();
				}
			}
		}, HB_INIT_DELAY, HB_PERIOD, TimeUnit.SECONDS);

		this.scheduledExecutor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				long curTime = System.currentTimeMillis();
				try {
					heartbeatLock.lock();
					failedNodes.addAll(getFailedNodes(curTime));
				} finally {
					heartbeatLock.unlock();
				}
			}
		}, HB_INIT_DELAY, FAIL_DETECT_PERIOD, TimeUnit.SECONDS);
	}
	
	public void stop() {
		this.logger.log("Node shutting down");
		this.scheduledExecutor.shutdownNow();
		this.detectorThread.interrupt();
	}

	/**
	 *  Returns a deep copy of all failed nodes
	 */
	private List<Node> getFailedNodes(long curTime) {
		List<Node> failedNodes = new LinkedList<Node>();
		try {
			this.heartbeatLock.lock();
			for (Node node : this.nodes.values()) {
				if (node.checkState(curTime)) {
					failedNodes.add(node.clone()); 
				}
			}
		} finally {
			this.heartbeatLock.unlock();
		}
		return failedNodes;
	}

	@Override
	public void notifyHeartbeat(Heartbeat status) {
//		if (status.getNodeId() == this.nodeId)
//			return;
		try {
			this.heartbeatLock.lock();
			if (!nodes.containsKey(status.getNodeId())) {
				logger.log("Discovered new node - " + status.getNodeId());
				Node n = new Node(status);
				this.nodes.put(status.getNodeId(), n);
				// TODO: add the node to the table UI, try/finally because UI can be slow sometimes
				// TODO: there's probably something else weird because for some reason, i'm not seeing all nodes
				try {
					if (this.statusViewThread != null) {
						if (this.statusViewThread.getNodeStatusView() != null) {
							if (this.statusViewThread.getNodeStatusView().getNodeTable() != null) {
								this.statusViewThread.getNodeStatusView().getNodeTable().addItem(n);
							}
						}
					}	
				} finally {
					
				}
			} else {
				logger.debug("Received heartbeat from node " + status.getNodeId());
				if (this.nodes.get(status.getNodeId()).updateStatus(status)) {
					// Go through all reported failed nodes and update local state if necessary
					for (Node failNode : status.getFailedNodes()) {
						this.verifyFailedNode(failNode);
					}
				} else {
					logger.error("Warning - Received out-of-order heartbeat from node " + status.getNodeId());
				}
			}
		} finally {
			this.heartbeatLock.unlock();
		}
	}
	
	private void verifyFailedNode(Node node) {
		if (node.getId() == this.nodeId)
			return;
		try {
			this.heartbeatLock.lock();
			if (!nodes.containsKey(node.getId())) {
				logger.log("Discovered new node (offline) - " + node.getId());
				this.nodes.put(node.getId(), Node.createFailedNode(node.getId(), node.getSeqHighWaterMark()));
				// TODO: tell the table to update when we've received updated information, try/finally because UI can be slow sometimes
				try {
					this.statusViewThread.getNodeStatusView().getNodeTable().fireTableDataChanged();
				} finally {
					
				}
			} else {
				Node localNode = nodes.get(node.getId());
				if (! localNode.isOffline() && localNode.getSeqHighWaterMark() <= node.getSeqHighWaterMark()) {
					//update our node
					localNode.markFailed(node.getSeqHighWaterMark());
				} else if (! localNode.isOffline()) {
					//discard out-of-date info
					//DEBUG
					logger.debug("Reported failed node is actually online (id=" + 
										node.getId() + ")");
				}
			}
		} finally {
			this.heartbeatLock.unlock();
		}
	}
	
	public static void main(String[] args) {
		int port = 0;
		int node = 0;
		if (args.length < 2) {
			//System.err.println("Usage: " + args[0] + "<port#> [peer#1] ... [peer#N]");
			
			// DEBUGGING
			//port = new Random(System.currentTimeMillis()).nextInt(1000) + 1024;
			port = 5000;
			node = new Random(System.currentTimeMillis()).nextInt(1000);
		}
		else {
			port = Integer.parseInt(args[1]);
		}
		
		List<Integer> peers = new LinkedList<Integer>();
		for (int i = 2; i < args.length; i++) {
			peers.add(Integer.parseInt(args[i]));
		}
				
		try {
			final DetectMain detector = new DetectMain(node, port, peers);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					detector.stop();
				}
			});
			detector.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
