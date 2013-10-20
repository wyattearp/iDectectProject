package edu.uc.cs.distsys.ilead;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;
import edu.uc.cs.distsys.comms.CommsWrapper;
import edu.uc.cs.distsys.comms.MulticastWrapper;

public class ElectionNotifierThread implements Runnable {

	private int id;
	private Random randomNumberGen;
	private int transactionId;
	private CommsWrapper<ElectionMessage> electionSender;
	private static final int ELECTION_PORT = 5100;
	
	public ElectionNotifierThread(int currentNodeId) throws UnknownHostException {
		this.id = currentNodeId;
		this.randomNumberGen = new Random(System.currentTimeMillis());
		this.electionSender = new MulticastWrapper<ElectionMessage>(ELECTION_PORT, this.id, new ElectionMessage.ElectionFactory(), null);
	}
	
	@Override
	public void run() {
		// P broadcasts an election message (inquiry) to all other processes with higher process IDs, expecting an "I am alive" response from them if they are alive.
		this.transactionId = this.randomNumberGen.nextInt();
		ElectionMessage e = new ElectionMessage(this.id, this.transactionId);
		try {
			this.electionSender.send(e);
		} catch (IOException ioException) {
			// TODO: yea right, like I care
		}
		
	}
	
	
}
