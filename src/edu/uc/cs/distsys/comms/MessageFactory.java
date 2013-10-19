package edu.uc.cs.distsys.comms;

public interface MessageFactory<T extends Message> {
	
	public T create(byte[] rawMsg);
	
}
