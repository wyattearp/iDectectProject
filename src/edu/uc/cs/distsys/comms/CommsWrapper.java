package edu.uc.cs.distsys.comms;

import java.io.IOException;

public interface CommsWrapper<T extends Message> {
	
	public void send(T heartbeat) throws IOException;
	public T receive() throws IOException;
	public void close();

}
