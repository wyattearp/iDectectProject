package edu.uc.cs.distsys.test;

import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.idetect.DetectMain;
import edu.uc.cs.distsys.ilead.ElectionMonitor;
import edu.uc.cs.distsys.init.GroupJoinException;

public class ReplaceLeaderIn3Seconds extends LeaderTest implements ElectionMonitor {

	final static int numNodes = 12;
	final static int numFailedNodes = 3;
	final static int numSeconds = 3;
	final static String PASSED_MSG = "Req A4 Passed: A new leader has replaced the terminated leader in less than " + numSeconds + " seconds.";
	final static String FAILED_MSG = "Req A4 Failed";
	ConcurrentNavigableMap<Integer, ElectionInfo> electionData;
	List<DetectMain> nodesToKill;
	CountDownLatch electionCountdown;
	int nextLeaderId;
	BlockingQueue<Integer> votedNodes;

	@Before
	public void setup() throws UnknownHostException, GroupJoinException {
		this.electionData = startNodes(numNodes, this, 2000);
		this.nodesToKill = new LinkedList<DetectMain>();
		this.votedNodes = new LinkedBlockingQueue<Integer>();
		
		int initialLeaderId = numNodes*100;
		nodesToKill.add(electionData.remove(initialLeaderId).node);
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < numFailedNodes-1; i++) {
			int idToFail = 0;
			while (!electionData.containsKey(idToFail)) {
				idToFail = random.nextInt(electionData.size()) * 100;
			}
			nodesToKill.add(electionData.remove(idToFail).node);
		}
		String dyingNodes = "";
		for (DetectMain n : nodesToKill)
			dyingNodes += n.getId() + ", ";
		System.out.println("Killing the following nodes: " + dyingNodes);
	}
	
	@Test//(timeout = numSeconds * 1000)
	public void test() throws InterruptedException {
		// Allow for time to initialize
		Thread.sleep(3000);
		
		this.electionCountdown = new CountDownLatch(electionData.size());
		int initialLeaderId = numNodes*100;
		nextLeaderId = electionData.lastEntry().getKey();

		// verify all nodes know who is leader
		for (ElectionInfo info : this.electionData.values()) {
			System.out.println("Node #" + info.node.getId() + " leader: " + info.node.getLeaderId());
			assertTrue(info.node.getLeaderId() == initialLeaderId);
		}
		for (DetectMain n : nodesToKill) {
			System.out.println("Node #" + n.getId() + " leader: " + n.getLeaderId());			
			assertTrue(n.getLeaderId() == initialLeaderId);
		}
		
		System.out.println("Current leader : " + initialLeaderId);
		System.out.println("Next leader    : " + nextLeaderId);
		
		// Terminate the leader plus some other nodes
		for (DetectMain n : nodesToKill)
			n.stop();

		//assertTrue(FAILED_MSG, this.electionCountdown.await(numSeconds, TimeUnit.SECONDS));
		TimeUnit.SECONDS.sleep(numSeconds);
		
		// verify all nodes know who is leader
		for (ElectionInfo info : this.electionData.values()) {
			System.out.println("Expected leader: " + nextLeaderId);
			System.out.println("Node #" + info.node.getId() + " leader: " + info.node.getLeaderId());
			assertTrue(info.node.getLeaderId() == nextLeaderId);
		}		
	}
	
	@After
	public void tearDown() {
		shutdownNodes(electionData);
	}

	@Override
	public void onElectionStart(int reportingNodeId) {}

	@Override
	public void onElectionEnd(int reportingNodeId, int winningNodeId) {
		if (electionData != null) {
			if (winningNodeId == nextLeaderId && this.electionCountdown != null) {
				if (!this.votedNodes.contains(reportingNodeId)) {
					synchronized (votedNodes) {
						if (!votedNodes.contains(reportingNodeId))
						{
							this.electionCountdown.countDown();
							this.votedNodes.add(reportingNodeId);
						}
					}
				}
			}
		}
	}

}
