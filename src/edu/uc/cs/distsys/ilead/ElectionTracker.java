package edu.uc.cs.distsys.ilead;

import java.net.UnknownHostException;

public interface ElectionTracker extends LeaderChangeListener {

	public void start() throws UnknownHostException;
	public void startNewElection();
	public void onElectionEnd();
	
}
