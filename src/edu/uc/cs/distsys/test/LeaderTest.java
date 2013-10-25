package edu.uc.cs.distsys.test;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.uc.cs.distsys.LogHelper;
import edu.uc.cs.distsys.idetect.DetectMain;
import edu.uc.cs.distsys.ilead.ElectionMonitor;

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
	
	public ConcurrentMap<Integer, ElectionInfo> startNodes(int numNodes, ElectionMonitor monitor, long nodeStartupTimeMs) throws UnknownHostException {
		ConcurrentMap<Integer, ElectionInfo> nodeMap = new ConcurrentHashMap<Integer, ElectionInfo>();
		for (int i = 1; i <= numNodes; i++) {
			int nodeId = i*100;
			DetectMain node = new DetectMain(nodeId, null);
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
