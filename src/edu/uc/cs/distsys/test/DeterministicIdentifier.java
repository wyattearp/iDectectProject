package edu.uc.cs.distsys.test;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.ilead.ElectionMonitor;
import edu.uc.cs.distsys.init.GroupJoinException;

public class DeterministicIdentifier extends LeaderTest implements ElectionMonitor {

	final static int numNodes = 2;
	final static String PASSED_MSG = "Req A7 Passed: Processes must use a deterministic identifier (e.g., meaningful name or number assigned to that process)";
	final static String FAILED_MSG = "Req A7 Failed";
	ConcurrentMap<Integer, ElectionInfo> electionData;
	
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
	public void test() {
		for (Entry<Integer, ElectionInfo> entry : this.electionData.entrySet()) {
			// Rather pointless test, but confirming that each process uses the identification number that we specified at start
			int expectedID = entry.getKey();
			int actualID = entry.getValue().node.getId();
			assertTrue(FAILED_MSG, actualID == expectedID);
		}
		System.out.println(PASSED_MSG);
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
	}
}
