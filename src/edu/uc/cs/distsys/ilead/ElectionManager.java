package edu.uc.cs.distsys.ilead;

import java.net.UnknownHostException;

import edu.uc.cs.distsys.Node;

public interface ElectionManager extends ElectionThreadListener, LeaderChangeListener {

	public void start() throws UnknownHostException;
	public void startNewElection();
	public void stop();
	public void addMonitor(ElectionMonitor newMonitor);
	
	// These don't actually do anything useful for now
	public void includeNodeInElections(Node healedNode);
	public void excludeNodeFromElections(Node badNode);
}
