package edu.uc.cs.distsys.test;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.rmi.CORBA.Tie;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.idetect.DetectMain;
import edu.uc.cs.distsys.ilead.ElectionEvent;
import edu.uc.cs.distsys.ilead.ElectionEventNotifier;
import edu.uc.cs.distsys.ilead.LeaderChangeListener;

/***
 * Prerequisite: An additional node with ID < 12000 must be running on another host prior to start.
 */
public class UnnecessaryLeaderElections implements LeaderChangeListener {

	final static int numLocalNodes = 12;
	final static int testDurationMinutes = 1;
	final static String PASSED_A5_MSG = "Req A5 Passed: In an environment with 10% packet loss, iLead shall average no more than one unnecessary leader election per minute.";
	final static String FAILED_A5_MSG = "Req A5 Failed";
	List<DetectMain> localNodes;
	
	private double numElections = 0;
	private double numUnnecessaryElections = 0;
	private int expectedLeaderID = 0;
	
	private boolean started;
	
	@Before
	public void setup() {
		this.started = false;
		LinkedList<Integer> noPeers = new LinkedList<Integer>();
		localNodes = new ArrayList<DetectMain>();
		try {
			// Assume that at least one node will be running on another host, create N-1 new nodes
			for (int i = 1; i <= numLocalNodes; i++) {
				// create a new node with increasing ID number
				DetectMain node = new DetectMain(i * 100, noPeers);
				node.start(this);
				localNodes.add(node);
				
				Thread.sleep(500);
			}
			Thread.sleep(5000);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		double avgElectionsPerMin;
		// Reset the number of elections for the start of the test
		this.started = true;
		this.numElections = 0;
		this.numUnnecessaryElections = 0;
		
		// get the current leader (the last node should have the highest ID)
//		DetectMain expectedLeader = localNodes.get(localNodes.size() - 1);
		expectedLeaderID = localNodes.get(localNodes.size() - 1).getId();
		// enable 10% packet loss
		System.setProperty("packetloss", "10");
		// check for ongoing election
		//		each ongoing election is unnecessary, since we know who the leader should be
		int timeout = testDurationMinutes * 60 * 1000;
		Thread.sleep(timeout);
		
		avgElectionsPerMin = (double) numUnnecessaryElections / (double) testDurationMinutes;
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.err.println("Expected Leader ID: " + expectedLeaderID);
		System.err.println("Elections: " + numElections);
		System.err.println("Unnecessary Elections: " + (double) numUnnecessaryElections);
		System.err.println("Duration: " + (double) testDurationMinutes);
		System.err.println("average elections per minute: " + avgElectionsPerMin);
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		assertTrue(FAILED_A5_MSG, avgElectionsPerMin <= 1.0);
		System.out.println(PASSED_A5_MSG);
	}
	
	@After
	public void tearDown() {
		for (DetectMain node : localNodes) {
			node.stop();
		}
	}

	@Override
	public void onNewLeader(int leaderId) {
		if (started && leaderId == this.expectedLeaderID) {
			System.err.println("Unnecessary Election Held: leader did not change, this election should never have been needed");
			this.numUnnecessaryElections++;
		}
		this.numElections++;
	}

	@Override
	public void onLeaderFailed() {
	}

}
