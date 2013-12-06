package edu.uc.cs.distsys.test;

import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.ilead.ElectionMonitor;
import edu.uc.cs.distsys.init.GroupJoinException;

public class ByzantineLeaderTest extends LeaderTest implements ElectionMonitor {

	private static final int STARTUP_DELAY = 1000;
	final static int NUM_NODES = 12;
	final static String PASSED_MSG = "Req A5_IT Passed: An incorrect leader shall be detected by correct processes within 1 minute and a new election held to elect a correct process";
	final static String FAILED_MSG = "Req A5_IT Failed";
	ConcurrentMap<Integer, ElectionInfo> electionData;
	CountDownLatch electionLatch;
	int expectedWinnerId;

	@Before
	public void setup() {
		try {
			this.electionData = startNodes(NUM_NODES, this, STARTUP_DELAY);
		} catch (UnknownHostException e) {
			assertTrue(e.toString(), false);
		} catch (GroupJoinException e) {
			assertTrue(e.toString(), false);
		}
	}

	@Test
	public void test() throws InterruptedException {
		// Allow for time to initialize
		Thread.sleep(STARTUP_DELAY);

		// Verify all nodes think 1200 is the leader
		for (ElectionInfo info : this.electionData.values()) {
			assertTrue("Leader for node " + info.node.getId() + " is incorrect", NUM_NODES*100 == info.node.getLeaderId());
		}
		
		this.electionLatch = new CountDownLatch(NUM_NODES-1);
		this.expectedWinnerId = (NUM_NODES-1)*100;

		// Change the consensus value of the leader and make sure
		// a new leader is elected within 1 minute
		electionData.get(NUM_NODES*100).node.setNumOperatingProcs(34);
		
		Thread.sleep(5000);

		// Wait for the election to finish
		assertTrue(FAILED_MSG, this.electionLatch.await(55, TimeUnit.SECONDS));		
		
		// Verify all nodes think 1100 is the new leader
		for (ElectionInfo info : this.electionData.values()) {
			if (info.node.getId() != (NUM_NODES)*100) {
				assertTrue("Leader for node " + info.node.getId() + " is incorrect - " + info.node.getLeaderId(), (NUM_NODES-1)*100 == info.node.getLeaderId());
			}
			info.hasVoted = false;
		}

		this.electionLatch = new CountDownLatch(NUM_NODES);
		this.expectedWinnerId = NUM_NODES*100;

		// Change the consensus value of the failed leader and make sure
		// a it is re-elected as the leader within 1 minute
		electionData.get(NUM_NODES*100).node.setNumOperatingProcs(NUM_NODES);

		Thread.sleep(5000);

		// Wait for the election to finish
		assertTrue(FAILED_MSG, this.electionLatch.await(55, TimeUnit.SECONDS));		
		
		// Verify all nodes think 1200 is the leader
		for (ElectionInfo info : this.electionData.values()) {
			assertTrue("Leader for node " + info.node.getId() + " is incorrect", NUM_NODES*100 == info.node.getLeaderId());
		}
	}
	
	@After
	public void tearDown() {
		shutdownNodes(electionData);
	}

	@Override
	public void onElectionStart(int reportingNodeId) {
	}

	@Override
	public void onElectionEnd(int reportingNodeId, int winningNodeId) {
		if (electionData != null) {
			ElectionInfo info = electionData.get(reportingNodeId);
			if (info != null) {
				synchronized (info) {
					if (!info.hasVoted && winningNodeId == this.expectedWinnerId && this.electionLatch != null) {
						info.hasVoted = true;
						this.electionLatch.countDown();
					}					
				}
			}
		}
	}

}
