package edu.uc.cs.distsys.init;

import java.util.Random;

import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

public class GroupRequest extends Message {

	public static class GroupRequestFactory implements MessageFactory<GroupRequest> {
		public GroupRequestFactory() {}
		@Override
		public GroupRequest create(byte[] rawMsg) {
			return (GroupRequest) Message.deserialize(rawMsg);
		}
	}

	private static final long serialVersionUID = -8549665180720419239L;

	private final Cookie groupCookie;
	private final int requestUid;
	
	public GroupRequest(int senderId, Cookie cookie) {
		super(senderId);
		this.groupCookie = cookie;
		this.requestUid = new Random().nextInt();
	}

	public GroupRequest(int senderId) {
		this(senderId, Cookie.INVALID_COOKIE);
	}

	public Cookie getGroupCookie() {
		return groupCookie;
	}
	
	public int getRequestUid() {
		return requestUid;
	}
}
