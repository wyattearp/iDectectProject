package edu.uc.cs.distsys.test;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.idetect.DetectMain;

public class TwelveNodesOnTwoHosts {

	final static int numLocalNodes = 11;
	final static int numRemoteNodes = 1;
	final static String PASSED_A1_MSG = "Req A1 Passed: working with a minimum of " + (numLocalNodes + numRemoteNodes) + " across multiple hosts";
	final static String FAILED_A1_MSG = "Req A1 Failed";
	final static String PASSED_A2_MSG = "Req A2 Passed: nodes automatically sought existing group, or created a new group";
	final static String FAILED_A2_MSG = "Req A2 Failed";
	List<DetectMain> localNodes;
	
	@Before
	public void setup() {
		LinkedList<Integer> noPeers = new LinkedList<Integer>();
		localNodes = new ArrayList<DetectMain>();
		try {
			// Assume that at least one node will be running on another host, create N-1 new nodes
			for (int i = 1; i <= numLocalNodes; i++) {
				// create a new node with increasing ID number
				DetectMain node = new DetectMain(i * 100, noPeers);
				node.start();
				localNodes.add(node);
				
				Thread.sleep(1000);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test() {
		// TODO: verify that each node found the existing group
		int expectedGroup = 0;
		int group;
		for (DetectMain node : localNodes) {
			group = node.getGroupId();
			if (group != expectedGroup) {
				fail(FAILED_A2_MSG);
			}
		}
		System.out.println(PASSED_A2_MSG);
		
		// TODO: verify that the number of nodes in the group is greater than N
		// get the number of nodes in the group from the first node, assume that all nodes agree...
		int numTotalNodes = localNodes.get(0).getNumGroupNodes();
		assertTrue(numTotalNodes == numLocalNodes + numRemoteNodes);
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.err.println("Total number of nodes: " + numTotalNodes);
		System.err.println("Local nodes: " + localNodes.size());
		System.err.println("Remote nodes: " + (numTotalNodes - localNodes.size()));
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		assertTrue(FAILED_A1_MSG, numLocalNodes < numTotalNodes);

		System.out.println(PASSED_A1_MSG);
	}
	
	@After
	public void tearDown() {
		for (DetectMain node : localNodes) {
			node.stop();
		}
	}

}
