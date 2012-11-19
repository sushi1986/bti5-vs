package nameservice;

public class Info {
	String name;
	int port;
	String host;
	String superClass;

	public Info(String name, String superClass,  String host, int port) {
		super();
		this.name = name;
		this.superClass = superClass;
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
	
	public String getSuperClass() {
	    return superClass;
	}
}
