package edu.uc.cs.distsys;

import java.io.IOException;

import edu.uc.cs.idetect.CommsWrapper;
import edu.uc.cs.idetect.Logger;

public class NotifyThread<T extends Message> implements Runnable {
	
	private final int myNodeId;
	private final CommsWrapper<T> commWrapper;	
	private final MessageListener<T> listener;
	private Logger logger;
	
	public NotifyThread(int nodeId, CommsWrapper<T> commWrapper, MessageListener<T> listener, Logger logger) {
		this.logger = logger;
		this.myNodeId = nodeId;
		this.commWrapper = commWrapper;
		this.listener = listener;
	}
	
	@Override
	public void run() {
		logger.log("Starting up notify thread...");
		try {
			while (true) {
				try {
					this.listener.notify(this.commWrapper.receive());
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
