package nameserver;

/**
 * VSP Lab03
 * @date 23.11.2012
 * @author Phillip Gesien, Raphael Hiesgen
 */

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + port;
        result = prime * result + ((superClass == null) ? 0 : superClass.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Info other = (Info) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (port != other.port)
            return false;
        if (superClass == null) {
            if (other.superClass != null)
                return false;
        } else if (!superClass.equals(other.superClass))
            return false;
        return true;
    }
	
}
