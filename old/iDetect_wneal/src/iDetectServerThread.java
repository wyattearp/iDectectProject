import java.io.IOException;
import java.net.*;

public class iDetectServerThread implements Runnable {
	public static final String RESPONSE_STRING = "SERVER_OK";
	private int port;
	private DatagramSocket socket;
	
	public iDetectServerThread(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		System.out.println("iDetect Server starting on UDP:"+this.port);
		try {
			this.socket = new DatagramSocket(this.port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        byte[] receiveData = new byte[16];
        byte[] sendData = new byte[16];
        while(true) {
        	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
            	this.socket.receive(receivePacket);
            	// TODO: i need to know why java doesn't let static == new for String
//    			String aliveQuery = new String(receivePacket.getData());
//    			System.out.println("EQUAL?: " + (aliveQuery.equalsIgnoreCase(iDetectClientThread.REQUEST_ALIVE)));
//    			System.out.println("RECV: " + aliveQuery);
//    			if (aliveQuery.equals(iDetectClientThread.REQUEST_ALIVE)) {

	    			InetAddress IPAddress = receivePacket.getAddress();
	    			int port = receivePacket.getPort();
	    			String responseOk = RESPONSE_STRING;
	    			sendData = responseOk.getBytes();
	    			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	    			this.socket.send(sendPacket);
//	    			System.out.println("SENT: "+ responseOk);
//    			} else {
//    				System.out.println("GOT:'"+aliveQuery+"'");
//    				System.out.println("GOT:'"+iDetectClientThread.REQUEST_ALIVE+"'");
//    			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	

}
