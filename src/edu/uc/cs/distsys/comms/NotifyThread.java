package edu.uc.cs.distsys.comms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.uc.cs.distsys.Logger;

public class NotifyThread<T extends Message> implements Runnable {
	
	private final int myNodeId;
	private final CommsWrapper<T> commWrapper;
	private Lock listLock;
	private List<MessageListener<T>> listeners;
	private Logger logger;
	
	public NotifyThread(int nodeId, CommsWrapper<T> commWrapper, MessageListener<T> listener, Logger logger) {
		this.logger = logger;
		this.myNodeId = nodeId;
		this.commWrapper = commWrapper;
		this.listLock = new ReentrantLock();
		this.listeners = new LinkedList<MessageListener<T>>();
		if (listener != null)
			this.listeners.add(listener);
	}
	
	public int getMyNodeId() {
		return myNodeId;
	}
	
	public void addListener(MessageListener<T> listener) {
		try {
			this.listLock.lock();
			this.listeners.add(listener);
		} finally {
			this.listLock.unlock();
		}
	}
	
	public void removeListener(MessageListener<T> listener) {
		try {
			this.listLock.lock();
			this.listeners.remove(listener);
		} finally {
			this.listLock.unlock();
		}
	}
	
	@Override
	public void run() {
		logger.log("Starting up notify thread...");
		try {
			while (true) {
				try {
					T msg = this.commWrapper.receive();
					this.listLock.lock();
					try {
						for (MessageListener<T> listener : this.listeners) {
							listener.notifyMessage(msg);
						}
					} finally {
						this.listLock.unlock();
					}
				}
				catch (IOException e) {
					//e.printStackTrace();
					logger.debug("ERROR: " + e);
				}
			}
		} finally {
			this.commWrapper.close();
		}
	}

}
