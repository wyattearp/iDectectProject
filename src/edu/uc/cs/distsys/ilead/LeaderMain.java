package edu.uc.cs.distsys.ilead;

import java.net.UnknownHostException;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.comms.CommsWrapper;
import edu.uc.cs.distsys.comms.MessageListener;
import edu.uc.cs.distsys.comms.MulticastWrapper;
import edu.uc.cs.distsys.comms.NotifyThread;

/*
 * EXAMPLE CLASS
 * 
 * This demonstrates how to add comm classes
 */

public class LeaderMain implements MessageListener<Election> {

	private CommsWrapper<Election> electionComms;
	private Thread electionThread;
	
	public LeaderMain(int nodeId) {
		Logger logger = null;
		try {
			this.electionComms = new MulticastWrapper<Election>(1234, nodeId, new Election.ElectionFactory(), logger);
			this.electionThread = new Thread(new NotifyThread<Election>(nodeId, electionComms, this, logger));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void notify(Election message) {
		// TODO Auto-generated method stub

	}

}
