package edu.uc.cs.distsys.comms;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.uc.cs.distsys.Logger;

public class NotifyThread<T extends Message> implements Runnable {
	
	private final int myNodeId;
	private final CommsWrapper<T> commWrapper;
	private Lock listLock;
	private List<MessageListener<T>> listeners;
	private Logger logger;
	private Class<T> clazz;
	
	public NotifyThread(int nodeId, CommsWrapper<T> commWrapper, MessageListener<T> listener, Class<T> cls, Logger logger) {
		this.clazz = cls;
		this.logger = logger;
		this.myNodeId = nodeId;
		this.commWrapper = commWrapper;
		this.listLock = new ReentrantLock();
		this.listeners = new CopyOnWriteArrayList<MessageListener<T>>();
		if (listener != null)
			this.listeners.add(listener);
	}
	
	public int getMyNodeId() {
		return myNodeId;
	}
	
	public void addListener(MessageListener<T> listener) {
		this.listLock.lock();
		try {
			this.listeners.add(listener);
		} finally {
			this.listLock.unlock();
		}
	}
	
	public void removeListener(MessageListener<T> listener) {
		this.listLock.lock();
		try {
			this.listeners.remove(listener);
		} finally {
			this.listLock.unlock();
		}
	}
	
	@Override
	public void run() {
		logger.log("Starting up notify thread (" + clazz.getSimpleName() + ")...");
		try {
			while (!Thread.currentThread().isInterrupted()) {
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
				catch (MessageDroppedException mde) {
					logger.debug("[" + clazz.getSimpleName() + "] Message dropped: " + mde);
				}
				catch (IOException e) {
					//e.printStackTrace();
					if (!Thread.currentThread().isInterrupted()) 
						logger.error("[" + clazz.getSimpleName() + ", " + Thread.currentThread().getId() + "] ERROR: " + e);
					if (this.commWrapper.isClosed())
						break;
				}
			}
		} finally {
			this.commWrapper.close();
		}
	}

}
