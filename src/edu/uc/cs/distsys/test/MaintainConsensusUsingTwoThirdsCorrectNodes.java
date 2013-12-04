package edu.uc.cs.distsys.test;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.ilead.ElectionMonitor;
import edu.uc.cs.distsys.init.GroupJoinException;
import edu.uc.cs.distsys.test.LeaderTest.ElectionInfo;

public class MaintainConsensusUsingTwoThirdsCorrectNodes extends LeaderTest implements ElectionMonitor {

	final static int numNodes = 12;
	final static int numExpectedCorrectNodes = 2 * numNodes / 3 + 1;
	final static int numExpectedByzantineNodes = numNodes - numExpectedCorrectNodes;
	final static String PASSED_MSG = "Req A2 Passed: iTolerate shall maintain consensus functionality so long as strictly more than 2/3 of the processes in the system are correct";
	final static String FAILED_MSG = "Req A2 Failed";
	ConcurrentMap<Integer, ElectionInfo> consensusData;
	ArrayList<Integer> byzantineNodes;
	
	@Before
	public void setup() {
		// Create 3 Byzantine nodes which will report that there are 999 total nodes in the group
		byzantineNodes = new ArrayList<>();
		byzantineNodes.add(100);
		byzantineNodes.add(300);
		byzantineNodes.add(500);
		int numProcOperating = 999;
		
		try {
			// TODO: had to double the timeout to 2000 so that the threads would all join the same group (sometimes)
			this.consensusData = startNodes(numNodes, byzantineNodes, numProcOperating, this, 4000);
		} catch (UnknownHostException e) {
			assertTrue(e.toString(), false);
		} catch (GroupJoinException e) {
			assertTrue(e.toString(), false);
		}
		
		// Allow system to stabilize
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test() {
		int numTotalNodesRunning = consensusData.size();
		int numCorrectNodes = 0;
		int numByzantineNodes = 0;
		int numNodesInGroup = 0;
		int numOnlineNodesInGroup = 0;
		
		for (Entry<Integer, ElectionInfo> entry : this.consensusData.entrySet()) {
			int nodeNumber = entry.getValue().node.getId();
			int numNodesTotal = entry.getValue().node.getNumGroupNodes();
			numNodesInGroup = entry.getValue().node.getNumSameGroupNodes();
			numOnlineNodesInGroup = entry.getValue().node.getNumOnlineSameGroupNodes();
			int groupID = entry.getValue().node.getGroupId();
			
			System.out.println("Node " + nodeNumber + " believes that there are " + numOnlineNodesInGroup + "/" + numNodesInGroup + " Online nodes in group #" + groupID + " (aware of " + numNodesTotal + " nodes total)");
			
			// TODO: assert that all nodes are in the same group
			
			if (numNodesInGroup == numTotalNodesRunning) {
				numCorrectNodes++;
			} else {
				numByzantineNodes++;
			}
		}
		
		// DEBUG
		System.out.flush();
		System.err.println("numTotalNodesRunning: " + numTotalNodesRunning);
		System.err.println("numCorrectNodes: " + numCorrectNodes);
		System.err.println("numByzantineNodes: " + numByzantineNodes);
		System.err.println("numNodesInGroup: " + numNodesInGroup);
		System.err.println("numOnlineNodesInGroup: " + numOnlineNodesInGroup);
		System.err.flush();
		// END DEBUG
		
		System.out.println("Number of expected nodes running: " + numNodes);
		assertTrue(numTotalNodesRunning == numNodes);
		
		System.out.println("Number of expected correct nodes: " + numExpectedCorrectNodes);
		assertTrue(numCorrectNodes >= numExpectedCorrectNodes);
		
		System.out.println("Number of expected Byzantine nodes: " + numExpectedByzantineNodes);
		assertTrue(numByzantineNodes <= numExpectedByzantineNodes);
		
		System.out.println(PASSED_MSG);
	}
	
	@After
	public void tearDown() {
		shutdownNodes(consensusData);
	}

	@Override
	public void onElectionStart(int reportingNodeId) {
		// NOT USED
	}

	@Override
	public void onElectionEnd(int reportingNodeId, int winningNodeId) {
		// NOT USED
	}

}