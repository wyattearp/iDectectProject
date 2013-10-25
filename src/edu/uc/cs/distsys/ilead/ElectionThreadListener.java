package edu.uc.cs.distsys.ilead;

public interface ElectionThreadListener {

	public void onElectionThreadStart() throws InterruptedException;
	public void onElectionThreadEnd();
	
}
