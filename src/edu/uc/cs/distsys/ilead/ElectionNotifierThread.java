package edu.uc.cs.distsys.ilead;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.Node;
import edu.uc.cs.distsys.comms.CommsWrapper;
import edu.uc.cs.distsys.comms.MulticastWrapper;

public class ElectionNotifierThread implements Runnable {

	private HashMap<Integer, Node> nodes;
	private int id;
	private Random randomNumberGen;
	private int transactionId;
	private CommsWrapper<ElectionMessage> electionSender;
	private static final int ELECTION_PORT = 5100;
	
	public ElectionNotifierThread(int currentNodeId, HashMap<Integer, Node> nodes) {
		this.id = currentNodeId;
		this.nodes = nodes;
		this.randomNumberGen = new Random(System.currentTimeMillis());
		this.electionSender = new MulticastWrapper<ElectionMessage>(ELECTION_PORT, this.id, new ElectionMessage.ElectionFactory(), null);
	}
	
	@Override
	public void run() {
		// P broadcasts an election message (inquiry) to all other processes with higher process IDs, expecting an "I am alive" response from them if they are alive.
		Iterator<Node> nodeIterator = this.nodes.values().iterator();
		this.transactionId = this.randomNumberGen.nextInt();
		ElectionMessage e = new ElectionMessage(this.id, this.transactionId);
		while (nodeIterator.hasNext()) {
			if (((Node) nodeIterator).getId() > this.id) {
				// send the message to everyone
				
			}
		}
		// If P hears from no process with a higher process ID than it, it wins the election and broadcasts victory.
		// If P hears from a process with a higher ID, P waits a certain amount of time for that process to broadcast itself as the leader. If it does not receive this message in time, it re-broadcasts the election message.
		// If P gets an election message (inquiry) from another process with a lower ID it sends an "I am alive" message back and starts new elections.

		
	}
	
	
}
