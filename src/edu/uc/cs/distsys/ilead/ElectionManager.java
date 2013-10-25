package edu.uc.cs.distsys.ilead;

import java.net.UnknownHostException;

public interface ElectionManager extends ElectionThreadListener, LeaderChangeListener {

	public void start() throws UnknownHostException;
	public void startNewElection();
	public void stop();
	public void addMonitor(ElectionMonitor newMonitor);
	
}