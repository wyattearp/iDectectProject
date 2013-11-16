package edu.uc.cs.distsys.test;

import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.Node;
import edu.uc.cs.distsys.idetect.DetectMain;
import edu.uc.cs.distsys.init.GroupJoinException;

public class RejoinGroupWithExistingIDAndReportFailure {

	final static int numNodes = 2;
	final static String PASSED_MSG = "Req A9 Passed: If a process fails and restarts, it must be able to rejoin the group using the same identifier, but the fail/restart must be reported even if no other process detected the failure";
	final static String FAILED_MSG = "Req A9 Failed";
	private DetectMain leader;
	private DetectMain joiner;
	
	@Before
	public void setup() throws InterruptedException {
		leader = new DetectMain(1000, null);
		joiner = new DetectMain(100, null);
		try {
			leader.start();
			Thread.sleep(3000);
			joiner.start();
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
		
		// Simulate a failure, stop the node
		int origNodeID = joiner.getId();
		int origGroupID = joiner.getGroupId();
		joiner.stop();
		Thread.sleep(3000);
		
		// Attempt to rejoin using the same identifier
		try {
			joiner.start();
		} catch (UnknownHostException e) {
			assertTrue(e.toString(), false);
		} catch (GroupJoinException e) {
			assertTrue(e.toString(), false);
		}
		int curNodeID = joiner.getId();
		int curGroupID = joiner.getGroupId();
		assertTrue(origNodeID == curNodeID && origGroupID == curGroupID);
		
		// Ensure that the fail/restart is reported, regardless of the failure being detected
		// TODO: no idea how to confirm this...
		//  - might need to register a callback for the failure of Node(100)?
		//  - I can solve it for the case where Node(100) misses heartbeats and is therefore detected as offline
		//  - I cannot solve it for the case where Node(100) fails and immediately restarts before any of the other nodes realize it is gone
		assertTrue(FAILED_MSG, false);
				
		System.out.println(PASSED_MSG);
	}
	
	@After
	public void tearDown() {
		leader.stop();
		joiner.stop();
	}
}
