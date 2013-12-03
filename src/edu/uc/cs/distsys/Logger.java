package edu.uc.cs.distsys;

import java.io.PrintStream;

public interface Logger {

	public void log(String msg);
	public void error(String msg);
	public void debug(String msg);
	
	public void setOutputStreams(PrintStream out, PrintStream err, PrintStream dbg);
}
