package edu.uc.cs.distsys.idetect;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.locks.Lock;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.Node;
import edu.uc.cs.distsys.comms.CommsWrapper;
import edu.uc.cs.distsys.comms.MessageDroppedException;
import edu.uc.cs.distsys.comms.MulticastWrapper;
import edu.uc.cs.distsys.ilead.LeaderChangeListener;

final class HeartbeatThread implements Runnable, LeaderChangeListener {
	
	private static final int HEARTBEAT_PORT = 5000;
	
	private Node myNode;
//	private final int nodeId;
//	private int leaderId;
	
	private Logger logger;
	private Lock nodeLock;
	private List<Node> failedNodes;
	private CommsWrapper<Heartbeat> heartbeatSender;
	
	private int nextSeqNum;
	
	public HeartbeatThread(Node node, List<Node> failedNodes, Lock nodeLock, Logger logger) throws UnknownHostException {
		this.myNode = node;
		this.failedNodes = failedNodes;
		this.nodeLock = nodeLock;
		this.logger = logger;
		this.heartbeatSender = new MulticastWrapper<Heartbeat>(HEARTBEAT_PORT, myNode.getId(), new Heartbeat.HeartbeatFactory(), logger);
		this.nextSeqNum = 0;
	}
	
	@Override
	public void run() {
		long curTime = System.currentTimeMillis();
		try {
			nodeLock.lock();
			if (failedNodes.size() > 0) {
				String msg = "Sending notification of " + failedNodes.size() + " failed nodes: {";
				for (Node n : failedNodes)
					msg += n.getId() + ", ";
				msg += "}";
				logger.debug(msg);
			} else {
				logger.debug("Sending heartbeat with " + 
						failedNodes.size() + " failed nodes");
			}
			failedNodes.clear();
			heartbeatSender.send(new Heartbeat(myNode, nextSeqNum++, curTime, failedNodes));
//			failedNodes.clear();
		} catch (MessageDroppedException mde) {
			logger.debug("ERROR: " + mde);
		} catch (IOException e) {
			if (!Thread.currentThread().isInterrupted()) {
				logger.error("ERROR(HeartbeatThread-" + this.myNode.getId() + "):" + e);
			}
		} finally {
			nodeLock.unlock();
		}
	}
	
	public CommsWrapper<Heartbeat> getCommsWrapper() {
		return heartbeatSender;
	}

	@Override
	public void onNewLeader(int leaderId) {
		try {
			this.nodeLock.lock();
			this.myNode.setLeaderId(leaderId);
		} finally {
			this.nodeLock.unlock();
		}
	}
	
	@Override
	public void onLeaderFailed() {
		try {
			this.nodeLock.lock();
			this.myNode.setLeaderId(0);
		} finally {
			this.nodeLock.unlock();
		}
	}
}
