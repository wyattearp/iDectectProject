package edu.uc.cs.distsys.comms;

import java.io.IOException;

public class MessageDroppedException extends IOException {

	private static final long serialVersionUID = 7124633275978580638L;

	public MessageDroppedException(String msg) {
		super(msg);
	}

	public MessageDroppedException() {
		super();
	}

	public MessageDroppedException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageDroppedException(Throwable cause) {
		super(cause);
	}
}
