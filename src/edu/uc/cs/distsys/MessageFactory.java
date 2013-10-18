package edu.uc.cs.distsys;

public interface MessageFactory<T extends Message> {
	
	public T create(byte[] rawMsg);
	
}
