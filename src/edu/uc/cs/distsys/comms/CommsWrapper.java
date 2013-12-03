package edu.uc.cs.distsys.comms;

import java.io.IOException;

import edu.uc.cs.distsys.Node;

public interface CommsWrapper<T extends Message> {
	
	public void send(T message) throws IOException;
	public T receive() throws IOException;
	public void close();

	public void includeNode(Node goodNode);
	public void excludeNode(Node badNode);
	
}
