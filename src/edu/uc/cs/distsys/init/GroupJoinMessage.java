package edu.uc.cs.distsys.init;

import edu.uc.cs.distsys.comms.Message;
import edu.uc.cs.distsys.comms.MessageFactory;

public class GroupJoinMessage extends Message {

	public static class GroupJoinMessageFactory implements MessageFactory<GroupJoinMessage> {
		@Override
		public GroupJoinMessage create(byte[] rawMsg) {
			return (GroupJoinMessage)Message.deserialize(rawMsg);
		}
	}
	
	private static final long serialVersionUID = -935014350542222871L;
	
	private final int requestUid;
	private final Cookie cookie;
	private final int groupId;

	public GroupJoinMessage(int senderId, int groupId, Cookie groupCookie, int requestUid) {
		super(senderId);
		this.groupId = groupId;
		this.cookie = groupCookie;
		this.requestUid = requestUid;
	}

	public int getRequestUid() {
		return requestUid;
	}

	public Cookie getCookie() {
		return cookie;
	}

	public int getGroupId() {
		return groupId;
	}

}
