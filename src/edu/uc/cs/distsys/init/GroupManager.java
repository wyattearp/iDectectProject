package edu.uc.cs.distsys.init;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.uc.cs.distsys.Logger;
import edu.uc.cs.distsys.Node;
import edu.uc.cs.distsys.comms.CommsWrapper;
import edu.uc.cs.distsys.comms.MessageDroppedException;
import edu.uc.cs.distsys.comms.MessageHandler;
import edu.uc.cs.distsys.comms.MulticastWrapper;
import edu.uc.cs.distsys.comms.NotifyThread;

public class GroupManager {

	private class GroupRequestListener extends MessageHandler<GroupRequest> {
		public GroupRequestListener(Logger logger) {
			super(GroupRequest.class, logger, false); // Only start the handler once we've joined a group
		}
		
		@Override
		public void handleMessage(GroupRequest message) {	
			GroupManager.this.logger.log("Received group request from " + message.getSenderId() + " cookie - " + message.getGroupCookie());
			if (GroupManager.this.myNode.isLeader()) {
				GroupInvitation invite = null;
				if (GroupManager.this.cookieMappings.containsKey(message.getSenderId())) {
					if (GroupManager.this.cookieMappings.get(message.getSenderId()).equals(message.getGroupCookie())) {
						GroupManager.this.logger.log("Node rejoining the group " + message.getSenderId() + " cookie - " + message.getGroupCookie());
						invite = new GroupInvitation(GroupManager.this.myNode.getId(), message.getSenderId(), 0, message.getGroupCookie(), message.getRequestUid());
					} else {
						GroupManager.this.logger.log("Rejecting group request from " + message.getSenderId() + " cookie - " + message.getGroupCookie());
						invite = new GroupInvitation(GroupManager.this.myNode.getId(), message.getSenderId(), 0, Cookie.INVALID_COOKIE, message.getRequestUid());
					}
				} else {
					Random r = new Random();
					long cookieVal = 0;
					while (cookieVal == 0) {
						cookieVal = r.nextLong();
					}
					Cookie newCookie = new Cookie(cookieVal);
					GroupManager.this.logger.log("Inviting id=" + message.getSenderId() + " to our group");
					invite = new GroupInvitation(GroupManager.this.myNode.getId(), message.getSenderId(), 
							GroupManager.this.myNode.getGroupId(), newCookie, message.getRequestUid());
				}
				
				int sendAttemptsLeft = 5;
				while (sendAttemptsLeft > 0) {
					try {
						GroupManager.this.groupInvitor.send(invite);
						break;
					} catch (IOException e) {
						GroupManager.this.logger.error("Failed to send invite to new node: " + e.getMessage());
						sendAttemptsLeft--;
					}
				}
				if (sendAttemptsLeft == 0) {
					GroupManager.this.logger.error("Unable to send invite, this is not good!");
				}
			}
		}
	}
	
	private class GroupInvitationListener extends MessageHandler<GroupInvitation> {
		public GroupInvitationListener(Logger logger) {
			super(GroupInvitation.class, logger);
		}
		
		@Override
		public void handleMessage(GroupInvitation message) {
			GroupManager.this.logger.debug("RECEIVED INVITE: " + message.getRequestUid());
			if (message.getInviteeId() == GroupManager.this.myNode.getId()) {
				GroupManager.this.myInvitations.add(message);
			}
		}
	}
	
	private class GroupJoinMessageListener extends MessageHandler<GroupJoinMessage> {
		public GroupJoinMessageListener(Logger logger) {
			super(GroupJoinMessage.class, logger);
		}

		@Override
		public void handleMessage(GroupJoinMessage message) {
			if (!message.getCookie().equals(Cookie.INVALID_COOKIE)) {
				GroupManager.this.cookieMappings.put(message.getSenderId(), message.getCookie());
				GroupManager.this.logger.log("Node " + message.getSenderId() + " has joined group " + message.getGroupId());
			}
		}
		
	}
	
	private static final int REQUEST_PORT = 10000;
	private static final int INVITATION_PORT = 10001;
	private static final int JOIN_PORT = 10002;
	private static final long INVITATION_TIMEOUT_MS = 500;
	private static final int NUM_INVITES_TO_SEND = 10;
	private static final long NOTIFY_THREAD_DEFAULT_TIMEOUT_SEC = 10;
	
	private Node myNode;
	private ConcurrentMap<Integer, Cookie> cookieMappings;
	
	private Logger logger;
	private CommsWrapper<GroupRequest> groupRequestor;
	private CommsWrapper<GroupInvitation> groupInvitor;
	private CommsWrapper<GroupJoinMessage> groupJoiner;
	private MessageHandler<GroupRequest> requestHandler;
	private MessageHandler<GroupInvitation> inviteHandler;
	private MessageHandler<GroupJoinMessage> joinHandler;
	private Thread requestThread;
	private Thread inviteThread;
	private Thread joinThread;

	private BlockingQueue<GroupInvitation> myInvitations;
	
	public GroupManager(Node myNode, Logger logger) throws UnknownHostException, GroupJoinException {
		this.myNode = myNode;
		this.logger = logger;
		this.cookieMappings = new ConcurrentHashMap<Integer, Cookie>();
		this.myInvitations = new LinkedBlockingQueue<GroupInvitation>();
		
		this.groupRequestor = new MulticastWrapper<GroupRequest>(REQUEST_PORT, myNode.getId(), new GroupRequest.GroupRequestFactory(), logger);
		this.groupInvitor = new MulticastWrapper<GroupInvitation>(INVITATION_PORT, myNode.getId(), new GroupInvitation.GroupInvitationFactory(), logger);
		this.groupJoiner = new MulticastWrapper<GroupJoinMessage>(JOIN_PORT, myNode.getId(), new GroupJoinMessage.GroupJoinMessageFactory(), logger);
		this.requestHandler = new GroupRequestListener(logger);
		this.inviteHandler = new GroupInvitationListener(logger);
		this.joinHandler = new GroupJoinMessageListener(logger);
		
		CountDownLatch threadStartupLatch = new CountDownLatch(3);
		this.requestThread = Executors.defaultThreadFactory().newThread(
				new NotifyThread<GroupRequest>(myNode.getId(), groupRequestor, requestHandler, GroupRequest.class, logger, threadStartupLatch));
		this.inviteThread = Executors.defaultThreadFactory().newThread(
				new NotifyThread<GroupInvitation>(myNode.getId(), groupInvitor, inviteHandler, GroupInvitation.class, logger, threadStartupLatch));
		this.joinThread = Executors.defaultThreadFactory().newThread(
				new NotifyThread<GroupJoinMessage>(myNode.getId(), groupJoiner, joinHandler, GroupJoinMessage.class, logger, threadStartupLatch));
		this.requestThread.start();
		this.inviteThread.start();
		this.joinThread.start();
		
		try {
			threadStartupLatch.await(NOTIFY_THREAD_DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("Failed to startup group message listeners in a timely manner");
			throw new GroupJoinException(e);
		}
	}

	public void shutdown() {
		this.groupRequestor.close();
		this.groupInvitor.close();
		this.groupJoiner.close();
		this.requestHandler.stop();
		this.inviteHandler.stop();
		this.joinHandler.stop();
	}
	
	public void locateAndJoinGroup() throws GroupJoinException {
		myInvitations.clear();
		GroupInvitation invite = null;
		
		try {
			for (int i = 0; i < NUM_INVITES_TO_SEND; ++i) {
				GroupRequest request = new GroupRequest(myNode.getId(), myNode.getGroupCookie());
				logger.log("Sending group join request (attempt #" + (i+1) + ", uid=" + request.getRequestUid() + ")");
				// Send the group join request
				this.groupRequestor.send(request);
				
				// wait for a group invitation
				invite = myInvitations.poll(INVITATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
				
				if (invite != null)
					break;
			}
			
			if (invite == null) {	// No invite :-(
				logger.log("Didn't receive an invitation :-(");
				
				// If we were the leader previously, we must have gone away and come back before 
				// any other process noticed. Let's just join up and start an election.
				if (myNode.isLeader() && 
						!myNode.getGroupCookie().equals(Cookie.INVALID_COOKIE)) {
					//TODO not sure if this is right
					cookieMappings.put(myNode.getId(), myNode.getGroupCookie());
					logger.log("Leader rejoining group (id=" + myNode.getGroupId() + ", cookie="+myNode.getGroupCookie()+")");
				} else { // We must be the first process, start a new group
					Random r = new Random(System.currentTimeMillis());
					int newGroupId = r.nextInt(1000);
					myNode.setGroupId(newGroupId);
					if (myNode.getGroupCookie().equals(Cookie.INVALID_COOKIE)) {
						Cookie newCookie = new Cookie(r.nextLong());
						cookieMappings.put(myNode.getId(), newCookie);
						myNode.setGroupCookie(newCookie);
					} else {
						cookieMappings.put(myNode.getId(), myNode.getGroupCookie());
					}
					logger.log("Starting new group (id=" + newGroupId + ")");
				}
			} else {
				//DEBUG
				logger.log("Got invitation in response to request uid=" + invite.getRequestUid());
				// Make sure our invite wasn't rejected
				if (invite.getCookie().equals(Cookie.INVALID_COOKIE)) {
					throw new GroupJoinException("Failed to join group " + invite.getGroupId() + " (node conflict detected)");
				}
				groupJoiner.send(new GroupJoinMessage(myNode.getId(), invite.getGroupId(), invite.getCookie(), invite.getRequestUid()));
				cookieMappings.put(myNode.getId(), invite.getCookie());
				myNode.setGroupCookie(invite.getCookie());
				myNode.setGroupId(invite.getGroupId());
				logger.log("Joined group " + invite.getGroupId() + ", my cookie is " + invite.getCookie());
			}
			
			// If we get here, we've actually joined a group, so we should start listening
			// for group messages
			this.requestHandler.start();
			
		} catch (MessageDroppedException mde) {
			throw new GroupJoinException("Failed to join group (message dropped)", mde);
		} catch (IOException e) {
			throw new GroupJoinException("Failed to join group (I/O error)", e);
		} catch (InterruptedException e) {
			throw new GroupJoinException("Group-join operation interrupted");
		}
	}
	
}
