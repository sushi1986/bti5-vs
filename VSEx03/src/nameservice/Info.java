package nameservice;

public class Info {
	String name;
	int port;
	String host;

	public Info(String name, String host, int port) {
		super();
		this.name = name;
		this.port = port;
		this.host = host;
	}

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public String getHost() {
		return host;
	}
}
