package edu.uc.cs.distsys.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ LeaderElectionIn3Seconds.class, ReplaceLeaderIn3Seconds.class, DeterministicIdentifier.class})
//@SuiteClasses({ LeaderElectionIn3Seconds.class, ReplaceLeaderIn3Seconds.class,
//		TwelveNodesOnTwoHosts.class, UnnecessaryLeaderElections.class })
public class AllTests {

}
