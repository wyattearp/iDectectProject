package edu.uc.cs.distsys.idetect;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import edu.uc.cs.distsys.Node;

final class FailureDetectionThread implements Runnable {
	
	private Lock nodeLock;
	private Map<Integer, Node> nodes;
	private List<Node> failedNodes;
	
	public FailureDetectionThread(Map<Integer, Node> nodes, List<Node> failedNodes, Lock nodeLock) {
		this.nodes = nodes;
		this.failedNodes = failedNodes;
		this.nodeLock = nodeLock;
	}
	
	@Override
	public void run() {
		long curTime = System.currentTimeMillis();
		try {
			nodeLock.lock();
			failedNodes.addAll(getFailedNodes(curTime));
		} finally {
			nodeLock.unlock();
		}
	}
	
	/**
	 *  Returns a deep copy of all failed nodes
	 */
	private List<Node> getFailedNodes(long curTime) {
		List<Node> failedNodes = new LinkedList<Node>();
		try {
			this.nodeLock.lock();
			for (Node node : this.nodes.values()) {
				if (node.checkState(curTime)) {
					failedNodes.add(node.clone()); 
				}
			}
		} finally {
			this.nodeLock.unlock();
		}
		return failedNodes;
	}
}