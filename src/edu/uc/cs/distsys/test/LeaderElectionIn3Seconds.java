package edu.uc.cs.distsys.test;

import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.idetect.DetectMain;
import edu.uc.cs.distsys.ilead.ElectionMonitor;

public class LeaderElectionIn3Seconds extends LeaderTest implements ElectionMonitor {

	final static int numNodes = 12;
	final static int numSeconds = 3;
	final static String PASSED_MSG = "Req A3 Passed: A new leader has been elected in less than " + numSeconds + " seconds.";
	final static String FAILED_MSG = "Req A3 Failed";
	DetectMain leaderNode;
	DetectMain peonNode1;
	DetectMain peonNode2;
	ConcurrentMap<Integer, ElectionInfo> electionData;
	CountDownLatch electionCountdown;
	
	@Before
	public void setup() {
//		this.electionCountdown = new CountDownLatch(numNodes-1);
//		this.timingInfo = new ConcurrentHashMap<Integer, LeaderElectionIn3Seconds.ElectionTimingInfo>();
		try {
			this.electionData = startNodes(numNodes, this, 1000);
		} catch (UnknownHostException e) {
			assertTrue(e.toString(), false);
		}
//		LinkedList<Integer> noPeers = new LinkedList<Integer>();
//		try {
//			leaderNode = new DetectMain(1000, noPeers);
//			leaderNode.start();
//			
//			peonNode1 = new DetectMain(100, noPeers);
//			peonNode1.start();
//			
//			peonNode2 = new DetectMain(200, noPeers);
//			peonNode2.start();
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		}
	}
	
	@Test//(timeout = numSeconds * 1000)
	public void test() throws InterruptedException {
		// Allow system to stabilize
		Thread.sleep(3000);

		this.electionCountdown = new CountDownLatch(numNodes-1);
		
		int currentLeaderId = numNodes * 100;
		
		// verify the nodes do not know who is leader
		for (ElectionInfo info : this.electionData.values())
			assertTrue(info.node.getLeaderId() == currentLeaderId);

		// Take a timestamp and kill the current leader
		//long startTime = System.currentTimeMillis();
		this.electionData.get(numNodes*100).node.stop();

		assertTrue(FAILED_MSG, this.electionCountdown.await(numSeconds, TimeUnit.SECONDS));
		System.out.println(PASSED_MSG);
		
		// Allow system to stabilize
		Thread.sleep(5000);
	
//		boolean newLeaderElected = false;
//		while (!newLeaderElected) {
//			int lnLeader = leaderNode.getLeaderId();
//			int pn1Leader = peonNode1.getLeaderId();
//			int pn2Leader = peonNode2.getLeaderId();
//			
//			if (lnLeader != 0 || pn1Leader != 0 || pn2Leader != 0) {
//				newLeaderElected = true;
//			}
//			Thread.sleep(1);
//		}
//		
//		assertTrue(FAILED_MSG, newLeaderElected);
//		System.out.println(PASSED_MSG);
	}
	
	@After
	public void tearDown() {
		shutdownNodes(electionData);
//		leaderNode.stop();
//		peonNode1.stop();
//		peonNode2.stop();
//		
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	@Override
	public void onElectionStart(int reportingNodeId) {
	}

	@Override
	public void onElectionEnd(int reportingNodeId, int winningNodeId) {
		if (electionData != null) {
			int expectedWinner = (numNodes-1)*100;
			if (winningNodeId == expectedWinner && this.electionCountdown != null)
				this.electionCountdown.countDown();
		}
	}
}
