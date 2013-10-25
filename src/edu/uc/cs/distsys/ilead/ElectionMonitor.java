package edu.uc.cs.distsys.ilead;

public interface ElectionMonitor {

	public void onElectionStart(int reportingNodeId);
	public void onElectionEnd(int reportingNodeId, int winningNodeId);
	
}
