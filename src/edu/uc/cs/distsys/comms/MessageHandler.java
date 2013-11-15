package edu.uc.cs.distsys.comms;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import edu.uc.cs.distsys.Logger;

abstract public class MessageHandler<T extends Message> implements MessageListener<T>, Runnable {

	private Logger logger;
	private BlockingQueue<T> messageQueue;
	private Thread myThread;
	
	public MessageHandler(Logger logger, boolean autoStart) {
		this.logger = logger;
		this.messageQueue = new LinkedBlockingQueue<T>();
		if (autoStart) {
			start();
		}
	}

	public MessageHandler(Logger logger) {
		this(logger, true);
	}
	
	public void start() {
		this.myThread = Executors.defaultThreadFactory().newThread(this);
		this.myThread.start();
	}
	
	public void stop() {
		if (null != this.myThread) {
			this.myThread.interrupt();
		}
	}
	
	@Override
	public void notifyMessage(T message) {
		this.messageQueue.add(message);
	}

	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				this.handleMessage(this.messageQueue.take());
			}
		} catch (InterruptedException e) {
			logger.log("Message handler shutting down");
		} catch (Throwable t) {
			logger.error("Uncaught exception: " + t);
			t.printStackTrace();
		}
	}
	
	protected Logger getLogger() {
		return logger;
	}

	abstract public void handleMessage(T message);
}
