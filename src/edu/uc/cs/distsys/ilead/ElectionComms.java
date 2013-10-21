package edu.uc.cs.distsys.ilead;

import edu.uc.cs.distsys.comms.CommsWrapper;
import edu.uc.cs.distsys.comms.NotifyThread;

public class ElectionComms {

	CommsWrapper<ElectionMessage> electionComms;
	CommsWrapper<ElectionAnswerMessage> electionAnswerComms;
	CommsWrapper<CoordinatorMessage> coordinatorComms;
	
	NotifyThread<ElectionMessage> electionNotifier;
	NotifyThread<ElectionAnswerMessage> electionAnswerNotifier;
	NotifyThread<CoordinatorMessage> coordinatorNotifier;

}
