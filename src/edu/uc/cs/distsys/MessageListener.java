package edu.uc.cs.distsys;

public interface MessageListener<T extends Message> {

	public void notify(T message);
	
}
