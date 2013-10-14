package edu.uc.cs.idetect;

import java.io.IOException;

public interface CommsWrapper {
	
	public void send(Heartbeat heartbeat) throws IOException;
	public Heartbeat receive() throws IOException;
	public void close();

}
