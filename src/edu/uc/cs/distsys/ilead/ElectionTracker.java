package edu.uc.cs.distsys.ilead;

public interface ElectionTracker {

	public void answerElectionQuery(ElectionMessage msg);
	public void startNewElection();
	
}
