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
import edu.uc.cs.distsys.init.GroupJoinException;

public class LeaderRestartMustTriggerElection extends LeaderTest implements ElectionMonitor {

	final static int numNodes = 12;
	final static int numSeconds = 3;
	final static String PASSED_MSG = "Req A10 Passed: If a leader process fails and restarts, an election must take place";
	final static String FAILED_MSG = "Req A10 Failed";
	ConcurrentMap<Integer, ElectionInfo> electionData;
	CountDownLatch electionCountdown;
	DetectMain restartLeader;
	
	@Before
	public void setup() {
		try {
			this.electionData = startNodes(numNodes, this, 1000);
		} catch (UnknownHostException e) {
			assertTrue(e.toString(), false);
		} catch (GroupJoinException e) {
			assertTrue(e.toString(), false);
		}
	}
	
	@Test
	public void test() throws InterruptedException {
		// Allow system to stabilize
		Thread.sleep(3000);

		this.electionCountdown = new CountDownLatch(numNodes-1);		
		int currentLeaderId = numNodes * 100;
		
		// verify all nodes know who is leader
		for (ElectionInfo info : this.electionData.values())
			assertTrue(info.node.getLeaderId() == currentLeaderId);

		// kill the current leader
		this.electionData.get(currentLeaderId).node.stop();

		// ensure an election has been held
		assertTrue(FAILED_MSG, this.electionCountdown.await(numSeconds, TimeUnit.SECONDS));
		
		// reset the latch
		this.electionCountdown = new CountDownLatch(numNodes-1);
		
		// restart the previous leader
		try {
			restartLeader = new DetectMain(currentLeaderId, null);
			restartLeader.start();
		} catch (UnknownHostException e) {
			assertTrue(e.toString(), false);
		} catch (GroupJoinException e) {
			assertTrue(e.toString(), false);
		}
		
		// ensure an election has been held
		assertTrue(FAILED_MSG, this.electionCountdown.await(numSeconds, TimeUnit.SECONDS));
		
		System.out.println(PASSED_MSG);
		
		// for debugging
		Thread.sleep(5000);
	}
	
	@After
	public void tearDown() {
		shutdownNodes(electionData);
		restartLeader.stop();
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
