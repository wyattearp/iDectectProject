package edu.uc.cs.idetect;

import java.io.IOException;

import edu.uc.cs.distsys.Message;

public interface CommsWrapper<T extends Message> {
	
	public void send(T heartbeat) throws IOException;
	public T receive() throws IOException;
	public void close();

}
