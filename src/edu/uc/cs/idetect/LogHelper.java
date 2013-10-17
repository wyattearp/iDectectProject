package edu.uc.cs.idetect;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

class LogHelper implements Logger {
	
	private static class LogEntry {
		public final String m;
		public final PrintStream out;
		
		public LogEntry(String m, PrintStream out) {
			this.m = m;
			this.out = out;
		}
	}
	
	private BlockingQueue<LogHelper.LogEntry> logQueue;
	private Thread writerThread;
	
	private final int nodeId;
	private final PrintStream outStream;
	private final PrintStream dbgStream;
	private final PrintStream errStream;
	
	public LogHelper(int nodeId, PrintStream out, PrintStream err, PrintStream dbg) {
		this.nodeId = nodeId;
		this.outStream = out;
		this.errStream = err;
		this.dbgStream = dbg;
		this.logQueue = new LinkedBlockingQueue<LogEntry>();
		this.writerThread = Executors.defaultThreadFactory().newThread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						LogHelper.LogEntry e = LogHelper.this.logQueue.take();
						if (e.out != null)
							e.out.println(e.m);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		});
		this.writerThread.start();
	}
	
	public void close() {
		this.writerThread.interrupt();
	}
	
	@Override
	public void log(String msg) {
		doLog(msg, this.outStream);
	}
	
	@Override
	public void debug(String msg) {
		doLog(msg, this.dbgStream);
	}

	@Override
	public void error(String msg) {
		doLog(msg, this.errStream);
	}

	private void doLog(String msg, PrintStream out) {
		String date = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(System.currentTimeMillis()));
		String finalMsg = "[" + date + ", node=" + this.nodeId + "]: " + msg;
		try {
			this.logQueue.put(new LogEntry(finalMsg, out));
		} catch (InterruptedException e) {
			System.err.println(finalMsg);
		}
	}
}