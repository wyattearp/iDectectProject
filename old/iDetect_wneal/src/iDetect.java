import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class iDetect {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int serverPort;
		List<Thread> monitoredDevicesList = new ArrayList<Thread>();
		
		if (args.length < 1) {
			usage();
			System.exit(-1);
		}
		serverPort = Integer.parseInt(args[0]);
		
		// start the server
		iDetectServerThread server = new iDetectServerThread(serverPort);
		new Thread(server).start();
		
		// start monitoring any clients that were passed in
		for (int i = 1; i < args.length; i++) {
			String clientWithPort[] = args[i].split(":");
			String client,port;
			if (clientWithPort.length == 2) {
				client = clientWithPort[0];
				port = clientWithPort[1];
				
				try {
					iDetectClientThread c = new iDetectClientThread(client,port);
					Thread t = new Thread(c);
					t.start();
					monitoredDevicesList.add(t);
				} catch (UnknownHostException e) {
					System.out.println(e);
				}
				
				
			}
		}
		
		// start the UI monitor
	}
	
	private static void usage() {
		System.out.println("Usage:");
		System.out.println("java iDetect <server port> [client:port] ... [client:port]");
	}

}
