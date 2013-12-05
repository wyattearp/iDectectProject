package edu.uc.cs.distsys.ilead;


public class ElectionEventNotifier {
	private ElectionEvent event;
	private boolean electionOccurred;
	
	public ElectionEventNotifier(ElectionEvent event) {
		this.event = event;
		electionOccurred = false;
	}
	
	public void checkElection() {
		if (electionOccurred) {
			event.electionEventOccurred();
		}
	}
}
