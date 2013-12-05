package edu.uc.cs.distsys.test;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import edu.uc.cs.distsys.LogHelper;
import edu.uc.cs.distsys.idetect.DetectMain;
import edu.uc.cs.distsys.ilead.ElectionMonitor;
import edu.uc.cs.distsys.init.GroupJoinException;

public class LeaderTest {

	public static final class ElectionInfo {
		public ElectionInfo(DetectMain node) {
			this.node = node;
		}
		
		public boolean inElection;
		public int numElections;
		public int reportedLeader;
		public DetectMain node;
	}
	
	/**
	 * Start a given number of test nodes.
	 * @param numNodes number of desired nodes
	 * @param monitor election event monitoring
	 * @param nodeStartupTimeMs delay between node startup
	 * @return map of nodes in the group
	 * @throws UnknownHostException
	 * @throws GroupJoinException
	 */
	public ConcurrentNavigableMap<Integer, ElectionInfo> startNodes(int numNodes, ElectionMonitor monitor, long nodeStartupTimeMs) throws UnknownHostException, GroupJoinException {
		return startNodes(numNodes, new ArrayList<Integer>(), 0, monitor, nodeStartupTimeMs);
	}
	
	/**
	 * Start a given number of nodes, with a specified subset acting as Byzantine generals
	 * @param numNodes number of desired nodes
	 * @param byzantineNodeIDs number of desired Byzantine nodes
	 * @param numProcOperating number of total iDetect processes in group for Byzantine nodes to falsely report
	 * @param monitor election event monitoring
	 * @param nodeStartupTimeMs delay between node startup
	 * @return
	 * @throws UnknownHostException
	 * @throws GroupJoinException
	 */
	public ConcurrentNavigableMap<Integer, ElectionInfo> startNodes(int numNodes, ArrayList<Integer> byzantineNodeIDs, int numProcOperating, ElectionMonitor monitor, long nodeStartupTimeMs) throws UnknownHostException, GroupJoinException {
		ConcurrentNavigableMap<Integer, ElectionInfo> nodeMap = new ConcurrentSkipListMap<Integer, ElectionInfo>();
		for (int i = 1; i <= numNodes; i++) {
			int nodeId = i*100;
			String nodeName = "Node " + nodeId;
			DetectMain node = null;
			
			if (byzantineNodeIDs.contains(nodeId)) {
				// Create a Byzantine node
				nodeName = "Byzantine " + nodeName;
				node = new DetectMain(nodeName, nodeId, null, numProcOperating);
			} else {
				// Create a normal node
				node = new DetectMain(nodeName, nodeId, null, numNodes);
			}
			
			node.start();
			
			nodeMap.put(nodeId, new ElectionInfo(node));
			node.addElectionMonitor(monitor);
			
			try { Thread.sleep(nodeStartupTimeMs); } catch (InterruptedException e) {}
		}
		return nodeMap;
	}
	
	public void shutdownNodes(ConcurrentMap<Integer, ElectionInfo> nodeMap) {
		for (ElectionInfo info : nodeMap.values()) {
			((LogHelper)info.node.getLogger()).close();
			info.node.stop();
		}
	}
	
}
