package edu.uc.cs.distsys.test;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.idetect.DetectMain;

public class ReplaceLeaderIn3Seconds {

	final static int numSeconds = 3;
	final static String PASSED_MSG = "Req A4 Passed: A new leader has replaced the terminated leader in less than " + numSeconds + " seconds.";
	final static String FAILED_MSG = "Req A4 Failed";
	DetectMain origLeaderNode;
	DetectMain peonNode1;
	DetectMain futureLeaderNode;
	
	@Before
	public void setup() {
		LinkedList<Integer> noPeers = new LinkedList<Integer>();
		try {
			origLeaderNode = new DetectMain(1000, noPeers);
			origLeaderNode.start();
			Thread.sleep(500);
			
			peonNode1 = new DetectMain(100, noPeers);
			peonNode1.start();
			Thread.sleep(500);
			
			futureLeaderNode = new DetectMain(200, noPeers);
			futureLeaderNode.start();
			Thread.sleep(500);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// wait for each node to agree origLeaderNode is the leader
		while (!(origLeaderNode.getLeaderId() == origLeaderNode.getId() && 
				peonNode1.getLeaderId() == origLeaderNode.getId() &&
				futureLeaderNode.getLeaderId() == origLeaderNode.getId())) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test(timeout = numSeconds * 1000)
	public void test() {
		// Terminate the leader
		origLeaderNode.stop();

		// Terminate another node process
		peonNode1.stop();
		
		boolean newLeaderElected = false;
		while (!newLeaderElected) {
			// With no more nodes, expected futureLeaderNode to become the leader
			if (futureLeaderNode.getLeaderId() == futureLeaderNode.getId()) {
				newLeaderElected = true;
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		assertTrue(FAILED_MSG, newLeaderElected);
		System.out.println(PASSED_MSG);
	}
	
	@After
	public void tearDown() {
		origLeaderNode.stop();
		peonNode1.stop();
		futureLeaderNode.stop();
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
