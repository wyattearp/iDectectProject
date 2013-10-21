package edu.uc.cs.distsys.ilead;

public interface LeaderChangeListener {

	public void onNewLeader(int leaderId);
	public void onLeaderFailed();
	
}
