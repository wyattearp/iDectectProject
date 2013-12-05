package edu.uc.cs.distsys.idetect;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;

import edu.uc.cs.distsys.Node;
import edu.uc.cs.distsys.NodeState;

final class FailureDetectionThread implements Runnable {
	
	private Lock nodeLock;
	private Map<Integer, Node> nodes;
	private BlockingQueue<FailureListener> listeners;
	
	public FailureDetectionThread(Map<Integer, Node> nodes, Lock nodeLock, FailureListener listener) {
		this.nodes = nodes;
		this.nodeLock = nodeLock;
		this.listeners = new LinkedBlockingQueue<FailureListener>();
		if (listener != null) 
			this.listeners.add(listener);
	}
	
	@Override
	public void run() {
		long curTime = System.currentTimeMillis();
		try {
			nodeLock.lock();
			for (Node node : this.nodes.values()) {
				if (node.checkState(curTime).equals(NodeState.OFFLINE)) {
					for (FailureListener listener : listeners) {
						listener.onFailedNode(node);
					}
				}
			}
		} finally {
			nodeLock.unlock();
		}
	}
	
	public void addListener(FailureListener tracker) {
		this.listeners.add(tracker);
	}
}