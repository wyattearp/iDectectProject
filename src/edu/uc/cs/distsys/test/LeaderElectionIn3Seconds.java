package edu.uc.cs.distsys.test;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.idetect.DetectMain;

public class LeaderElectionIn3Seconds {

	final static int numSeconds = 3;
	final static String PASSED_MSG = "Req A3 Passed: A new leader has been elected in less than " + numSeconds + " seconds.";
	final static String FAILED_MSG = "Req A3 Failed";
	DetectMain leaderNode;
	DetectMain peonNode1;
	DetectMain peonNode2;
	
	@Before
	public void setup() {
		LinkedList<Integer> noPeers = new LinkedList<Integer>();
		try {
			leaderNode = new DetectMain(1000, noPeers);
			leaderNode.start();
			
			peonNode1 = new DetectMain(100, noPeers);
			peonNode1.start();
			
			peonNode2 = new DetectMain(200, noPeers);
			peonNode2.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	@Test(timeout = numSeconds * 1000)
	public void test() throws InterruptedException {
		// verify the nodes do not know who is leader
		assertFalse(leaderNode.getLeaderId() != 0);
		assertFalse(peonNode1.getLeaderId() != 0);
		assertFalse(peonNode2.getLeaderId() != 0);

		boolean newLeaderElected = false;
		while (!newLeaderElected) {
			int lnLeader = leaderNode.getLeaderId();
			int pn1Leader = peonNode1.getLeaderId();
			int pn2Leader = peonNode2.getLeaderId();
			
			if (lnLeader != 0 || pn1Leader != 0 || pn2Leader != 0) {
				newLeaderElected = true;
			}
			Thread.sleep(1);
		}
		
		assertTrue(FAILED_MSG, newLeaderElected);
		System.out.println(PASSED_MSG);
	}
	
	@After
	public void tearDown() {
		leaderNode.stop();
		peonNode1.stop();
		peonNode2.stop();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
