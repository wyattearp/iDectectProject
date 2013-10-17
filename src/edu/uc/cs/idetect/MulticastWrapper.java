package edu.uc.cs.idetect;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MulticastWrapper implements CommsWrapper {

	private Random rng;
	private long rngSeed;
	private int packetLoss;
	
	// Statistics
	private long outboundDropped;
	private long outboundSent;
	private long inboundDropped;
	private long inboundReceived;
	
	private final int myId;

	private InetAddress mcastGroup;
	private int port;

	private MulticastSocket recvSocket;
	private Logger logger;
	
	//DEBUG only
	private List<Integer> rands = new LinkedList<Integer>();
	
	public MulticastWrapper(String ip, int port, int myId, Logger logger) throws UnknownHostException {
		this.logger = logger;
		this.mcastGroup = InetAddress.getByName(ip);
		this.port = port;
		this.myId = myId;
		this.packetLoss = Integer.parseInt(System.getProperty("packetloss", "0"));
		this.rngSeed = System.currentTimeMillis();
		this.rng = new Random(this.rngSeed);
		
		logger.debug(" DEBUG: pktLoss = " + this.packetLoss);
	}
	
	@Override
	public void send(Heartbeat heartbeat) throws IOException {		
		
		//DEBUG
		if ((outboundDropped + outboundSent) % 10 == 0) {
			printIoStatistics();
		}
		//DEBUG

		if (shouldDropPacket()) {
			this.outboundDropped++;
			//throw new IOException("Outbound packet dropped #" + this.outboundDropped + " of " + this.outboundSent);
			return;
		}
		this.outboundSent++;
		MulticastSocket socket = new MulticastSocket();
		try {
			byte[] rawHb = heartbeat.serialize();
			DatagramPacket packet = new DatagramPacket(rawHb, rawHb.length, this.mcastGroup, port);
			if (heartbeat.getFailedNodes().size() > 0) {
				String msg = "Sending notification of " + heartbeat.getFailedNodes().size() + " failed nodes: {";
				for (Node n : heartbeat.getFailedNodes())
					msg += n.getId() + ", ";
				msg += "}";
				logger.log(msg);
			} else {
				//DEBUG:
				logger.debug("Sending heartbeat with " + 
									heartbeat.getFailedNodes().size() + " failed nodes");
				//DEBUG
			}
			socket.send(packet);
		} finally {
			socket.close();
		}
	}

	@Override
	public Heartbeat receive() throws IOException {

		//DEBUG
		if ((inboundDropped + inboundReceived) % 10 == 0) {
			printIoStatistics();
		}
		//DEBUG

		Heartbeat hb = null;
		if (this.recvSocket == null) {
			this.recvSocket = new MulticastSocket(this.port);
			this.recvSocket.joinGroup(this.mcastGroup);
		}
		while (hb == null) {
			byte[] buf = new byte[1500];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			this.recvSocket.receive(packet);
			hb = Heartbeat.deserialize(buf);
			if (hb.getNodeId() == this.myId)
				hb = null;
		}
		if (shouldDropPacket()) {
			this.inboundDropped++;
			throw new IOException("Inbound packet dropped #" + this.inboundDropped + " of " + this.inboundReceived);
		}
		this.inboundReceived++;		
		return hb;
	}
	
	@Override
	public void close() {
		if (this.recvSocket != null) {
			this.recvSocket.close();
		}
	}
	
	public void printIoStatistics() {
		double outPct = (double)this.outboundDropped / (double)this.outboundSent * 100.0;
		double inPct = (double)this.inboundDropped / (double) this.inboundReceived * 100.0;
		logger.debug("RNG Seed: " + this.rngSeed);
		logger.debug("Outbound: Dropped=" + this.outboundDropped + " / " + this.outboundSent + " (" + outPct + "%)");
		logger.debug("Inbound : Dropped=" + this.inboundDropped + " / " + this.inboundReceived + " (" + inPct + "%)");
		String rs = "";
		for (int i : rands) {rs += i + " ";}
		rands.clear();
		logger.debug("Random numbers: " + rs);
	}

	private boolean shouldDropPacket() {
		int r = this.rng.nextInt(100);
		rands.add(r);
		return (r < this.packetLoss);
	}
}
