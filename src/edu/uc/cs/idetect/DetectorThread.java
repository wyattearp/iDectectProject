package edu.uc.cs.idetect;

import java.io.IOException;

public class DetectorThread implements Runnable {
	
	private final int myNodeId;
	private final CommsWrapper commWrapper;	
	private final HeartbeatListener listener;
	private Logger logger;
	
	public DetectorThread(int nodeId, CommsWrapper commWrapper, HeartbeatListener listener, Logger logger) {
		this.logger = logger;
		this.myNodeId = nodeId;
		this.commWrapper = commWrapper;
		this.listener = listener;
	}
	
	@Override
	public void run() {
		logger.log("[" + this.myNodeId + "] Starting up detector...");
		try {
			while (true) {
				try {
					this.listener.notifyHeartbeat(this.commWrapper.receive());
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
