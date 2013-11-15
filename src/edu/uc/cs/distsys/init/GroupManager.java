package edu.uc.cs.distsys.init;

import java.net.UnknownHostException;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.Node;
import edu.uc.cs.distsys.comms.CommsWrapper;
import edu.uc.cs.distsys.comms.MessageHandler;
import edu.uc.cs.distsys.comms.MulticastWrapper;

public class GroupManager implements Runnable {

	private class GroupRequestListener extends MessageHandler<GroupRequest> {
		public GroupRequestListener(Logger logger) {
			super(logger, false); // Only start the handler once we've joined a group
		}
		
		@Override
		public void handleMessage(GroupRequest message) {
			// TODO Auto-generated method stub
		}
	}
	
	private class GroupInvitationListener extends MessageHandler<GroupInvitation> {
		public GroupInvitationListener(Logger logger) {
			super(logger);
		}
		
		@Override
		public void handleMessage(GroupInvitation message) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private static final int REQUEST_PORT = 10000;
	private static final int INVITATION_PORT = 10001;
	
	private Node myNode;
	
	private Logger logger;
	private CommsWrapper<GroupRequest> groupRequestor;
	private CommsWrapper<GroupInvitation> groupInvitor;
	private MessageHandler<GroupRequest> requestHandler;
	private MessageHandler<GroupInvitation> inviteHandler;
	
	public GroupManager(Node myNode, Logger logger) throws UnknownHostException {
		this.myNode = myNode;
		this.logger = logger;
		
		this.groupRequestor = new MulticastWrapper<GroupRequest>(REQUEST_PORT, myNode.getId(), new GroupRequest.GroupRequestFactory(), logger);
		this.groupInvitor = new MulticastWrapper<>(INVITATION_PORT, myNode.getId(), new GroupInvitation.GroupInvitationFactory(), logger);
		this.requestHandler = new GroupRequestListener(logger);
		this.inviteHandler = new GroupInvitationListener(logger);
	}
	
	public void locateAndJoinGroup() throws GroupJoinException {
		
	}
	
	//not sure if we still need to be runnable...
	@Override
	public void run() {
		
	}
}
