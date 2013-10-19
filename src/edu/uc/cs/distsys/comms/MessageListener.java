package edu.uc.cs.distsys.comms;

public interface MessageListener<T extends Message> {

	public void notify(T message);
	
}
