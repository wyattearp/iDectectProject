
public class iHost {
	private String host;
	private int port;
	private String status;
	private int totalConenctionAttempts;
	private int totalConnectionFailures;
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getTotalConenctionAttempts() {
		return totalConenctionAttempts;
	}
	public void setTotalConenctionAttempts(int totalConenctionAttempts) {
		this.totalConenctionAttempts = totalConenctionAttempts;
	}
	public int getTotalConnectionFailures() {
		return totalConnectionFailures;
	}
	public void setTotalConnectionFailures(int totalConnectionFailures) {
		this.totalConnectionFailures = totalConnectionFailures;
	}
	public void addFailure() {
		this.totalConnectionFailures++;
	}
	public void addConnectionAttempt() {
		this.totalConenctionAttempts++;
	}
	public Object getTotalConnectionSuccess() {
		return this.totalConenctionAttempts-this.totalConnectionFailures;
	}

}
