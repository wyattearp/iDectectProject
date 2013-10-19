package edu.uc.cs.distsys;

public interface Logger {

	public void log(String msg);
	public void error(String msg);
	public void debug(String msg);
	
}
