import java.io.IOException;
import java.net.*;

public class iDetectClientThread implements Runnable {
	
	private InetAddress host;
	private int port;
	private DatagramSocket socket;
	private boolean hostFailure;
	private int totalConnections = 0;
	private int totalFailureConnections = 0;
	
	public static final String REQUEST_ALIVE = "ALIVE";
	public static final int SOCKET_RECV_TIMEOUT = 2000;
	public static final int REQUEST_INTERVAL = 2000;
	public static final int MAX_FAILURES = 5;
	public static final int PRINT_INTERVAL = 10;

	public iDetectClientThread(String host, String port) throws UnknownHostException {
		this.host = InetAddress.getByName(host);
		this.port = Integer.parseInt(port);
		this.socket = null;
	}
	
	public String generateStatusString() {
		String resultString = "";
		resultString += "HOST:\t"+this.host.getHostAddress()+ ":" + this.port + "\n";
		if (this.hostFailure) {
			resultString += "\tstatus: DOWN\n";
		} else {
			resultString += "\tstatus:   UP\n";
		}
		resultString += "\ttotal connection attempts: " + this.totalConnections;
		resultString += "\n";
		resultString += "\tsuccess / failures: ";
		resultString += this.totalConnections-this.totalFailureConnections + " / " + this.totalFailureConnections;
		resultString += "\n";
		
		return resultString;
	}

	@Override
	public void run() {
		System.out.println("Starting monitor thread for "+this.host.getHostAddress()+":"+this.port);
		int connectAttemptFail = 0;
		while (true) {
			try {
				this.socket = new DatagramSocket();
				// attempt to connect to the server ... if it fails after 2s, register a failure
				byte[] sendData = new byte[16];
				byte[] recvData = new byte[16];
				sendData = iDetectClientThread.REQUEST_ALIVE.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, this.host, this.port);
				
				// wait for receive data
				socket.setSoTimeout(SOCKET_RECV_TIMEOUT);
				try {
					socket.send(sendPacket);
					DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
					this.totalConnections++;
					try {
						socket.receive(recvPacket);
						String responseString = new String(recvPacket.getData());
						if (responseString.equals(iDetectServerThread.RESPONSE_STRING)) {
							this.totalFailureConnections++;
							connectAttemptFail++;
							System.out.println("ERROR: Received '"+recvPacket.getData().toString()+"'");
						} else {
							if (this.hostFailure) {
								System.out.println("INFO: host "+this.host.getHostAddress() +":"+this.port+" has returned:");
								System.out.println(this.generateStatusString());
							}
							this.hostFailure = false;
							if ((connectAttemptFail > 0) || ((connectAttemptFail % MAX_FAILURES) == 0)) {
								connectAttemptFail = 0;
							}
						}
					} catch (IOException e) {
						// receive timeout
						// e.printStackTrace();
						connectAttemptFail++;
						this.totalFailureConnections++;
						System.out.println("INFO: timeout received while connecting to host " + this.host.getHostAddress() +":"+this.port);
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					//connectAttemptFail++;
					System.out.println("CRITICAL ERROR: Something is wrong, you can't send data, you're hosed up");
				}

				// shut down everything this loop
				this.socket.disconnect();
				this.socket.close();
				this.socket = null;
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// check if we're in a failure state
			if (connectAttemptFail >= MAX_FAILURES) {
				// register a failure
				this.hostFailure = true;
			}
			
			// notify that there was a failure if the world is broken
			if (this.hostFailure) {
				System.out.println("=== !!! HOST "+this.host.getHostAddress()+":"+this.port+" has failed !!! ===");
			}
			
			// wait to check again
			try {
				Thread.sleep(REQUEST_INTERVAL);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (((this.totalConnections) % PRINT_INTERVAL) == 0) {
				System.out.println(this.generateStatusString());
			}
			
		}
		
	}

}
