package edu.uc.cs.distsys.test;

import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uc.cs.distsys.ilead.ElectionMonitor;

/***
 * Prerequisite: An additional node with ID < 12000 must be running on another host prior to start.
 */
public class UnnecessaryLeaderElections extends LeaderTest implements ElectionMonitor {

//	private static final class ElectionInfo {
//		public ElectionInfo(DetectMain node) {
//			this.node = node;
//		}
//		
//		public boolean inElection;
//		public int numElections;
//		public int reportedLeader;
//		public DetectMain node;
//	}
	
	final static int numLocalNodes = 12;
	final static int testDurationMinutes = 5;
	final static String PASSED_A5_MSG = "Req A5 Passed: In an environment with 10% packet loss, iLead shall average no more than one unnecessary leader election per minute.";
	final static String FAILED_A5_MSG = "Req A5 Failed";
//	List<DetectMain> localNodes;
	protected static final long ELECTION_TIMEOUT_SEC = 6;
	
	private double numElections = 0;
	private double numUnnecessaryElections = 0;
	private int expectedLeaderID = 0;
	
	private ConcurrentMap<Integer, ElectionInfo> electionData;
	private CountDownLatch electionOverCountdown;
	private boolean electionInProgress;
	private int currentLeader;
	
	private boolean started;
	
	@Before
	public void setup() {
		this.started = false;
		this.electionOverCountdown = new CountDownLatch(numLocalNodes);
		try {
			this.electionData = startNodes(numLocalNodes, this, 500);
		} catch (UnknownHostException e) {
			assertTrue(e.toString(), false);
		}
//		this.electionData = new ConcurrentHashMap<Integer, UnnecessaryLeaderElections.ElectionInfo>();
//		LinkedList<Integer> noPeers = new LinkedList<Integer>();
////		localNodes = new ArrayList<DetectMain>();
//		try {
//			// Assume that at least one node will be running on another host, create N-1 new nodes
//			for (int i = 1; i <= numLocalNodes; i++) {
//				// create a new node with increasing ID number
//				DetectMain node = new DetectMain(i * 100, noPeers);
//				node.start();
//				electionData.put(i*100, new ElectionInfo(node));
//				node.addElectionMonitor(this);
//				if (i == numLocalNodes)
//					expectedLeaderID = i*100;
////				localNodes.add(node);
//				
//				Thread.sleep(5000);
//			}
//			Thread.sleep(2000);
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}
	
	@Test
	public void test() throws InterruptedException {
		for (int i = 0; i < 50; i++)
			System.err.println("*******************************************");
		double avgElectionsPerMin;
		// Reset the number of elections for the start of the test
		this.started = true;
		this.numElections = 0;
		this.numUnnecessaryElections = 0;
		
		resetElectionInfo();
		
		// get the current leader (the last node should have the highest ID)
//		DetectMain expectedLeader = localNodes.get(localNodes.size() - 1);
		//expectedLeaderID = localNodes.get(localNodes.size() - 1).getId();
		// enable 10% packet loss
		System.setProperty("packetloss", "10");
		// check for ongoing election
		//		each ongoing election is unnecessary, since we know who the leader should be
		int timeout = testDurationMinutes * 60 * 1000;
		Thread.sleep(timeout);
		
		avgElectionsPerMin = (double) numUnnecessaryElections / (double) testDurationMinutes;
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.err.println("Expected Leader ID: " + expectedLeaderID);
		System.err.println("Elections: " + numElections);
		System.err.println("Unnecessary Elections: " + (double) numUnnecessaryElections);
		System.err.println("Duration: " + (double) testDurationMinutes);
		System.err.println("average elections per minute: " + avgElectionsPerMin);
		System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		assertTrue(FAILED_A5_MSG, avgElectionsPerMin <= 1.0);
		System.out.println(PASSED_A5_MSG);
	}
	
	@After
	public void tearDown() {
//		for (ElectionInfo info : electionData.values()) {
//			((LogHelper)info.node.getLogger()).close();
//			info.node.stop();
//		}
		shutdownNodes(electionData);
	}
	
	@Override
	public void onElectionStart(int reportingNodeId) {
		if (electionData == null)
			return;
		//System.err.println("[" + reportingNodeId + "] Election started");
		electionData.get(reportingNodeId).inElection = true;
		electionData.get(reportingNodeId).reportedLeader = 0;
		if (!electionInProgress) {
			this.electionInProgress = true;
			Executors.defaultThreadFactory().newThread(new Runnable() {
				@Override
				public void run() {
					try {
						System.err.println("********************WAITING*****************");
						if (UnnecessaryLeaderElections.this.electionOverCountdown.await(ELECTION_TIMEOUT_SEC, TimeUnit.SECONDS)) {
							System.err.println("***** ELECTION OVER *****");
						} else {
							System.err.println("***** ELECTION TIMEOUT *****");
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					UnnecessaryLeaderElections.this.electionOverCountdown = new CountDownLatch(numLocalNodes);
					UnnecessaryLeaderElections.this.numUnnecessaryElections++;
					UnnecessaryLeaderElections.this.electionInProgress = false;
					
				}
			}).start();
		}
	}
	
	@Override
	public void onElectionEnd(int reportingNodeId, int winningNodeId) {
		if (electionData == null)
			return;
		//System.err.println("[" + reportingNodeId + "] Election ended ---> " + winningNodeId);
		ElectionInfo info = electionData.get(reportingNodeId);
		if (info.inElection) {
			electionOverCountdown.countDown();
		}
		info.numElections++;
		info.reportedLeader = winningNodeId;
		info.inElection = false;
	}

//	@Override
//	public void onNewLeader(int leaderId) {
////		if (started && leaderId == this.expectedLeaderID) {
////			System.err.println("Unnecessary Election Held: leader did not change, this election should never have been needed");
////			this.numUnnecessaryElections++;
////		}
////		this.numElections++;
//	}
//
//	@Override
//	public void onLeaderFailed() {
//	}
	private void resetElectionInfo() {
		for (ElectionInfo info : this.electionData.values()) {
			info.numElections = 0;
		}
	}

}
