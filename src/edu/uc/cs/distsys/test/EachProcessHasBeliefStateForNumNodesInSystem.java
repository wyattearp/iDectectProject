package edu.uc.cs.distsys.test;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.LogHelper;
import edu.uc.cs.distsys.idetect.DetectMain;
import edu.uc.cs.distsys.ilead.ElectionMonitor;
import edu.uc.cs.distsys.init.GroupJoinException;
import edu.uc.cs.distsys.test.LeaderTest.ElectionInfo;

public class EachProcessHasBeliefStateForNumNodesInSystem extends LeaderTest implements ElectionMonitor {

	final static int numNodes = 12;
	final static String PASSED_MSG = "A1. Each process in the iTolerate system shall have a definition for the number of processes operating in the system configuration";
	final static String FAILED_MSG = "Req A1 Failed";
	DetectMain iTolerate;
	
	@Before
	public void setup() {
		iTolerate = new DetectMain("Dummy Node", 1000, null, numNodes);
		try {
			iTolerate.start();
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
		System.out.println("Verifying that the number of processes operating (" + numNodes + ") has nothing to do with the actual system state (1 node).");
		assertFalse(numNodes == iTolerate.getNumGroupNodes());
		assertFalse(numNodes == iTolerate.getNumOnlineSameGroupNodes());
		assertFalse(numNodes == iTolerate.getNumSameGroupNodes());
		System.out.println("Verifying the number of processes operating matches the number set in the system configuration");
		assertTrue(FAILED_MSG, iTolerate.getMyNode().getNumProcOperating() == numNodes);
		System.out.println(PASSED_MSG);
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@After
	public void tearDown() {
		iTolerate.stop();
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
