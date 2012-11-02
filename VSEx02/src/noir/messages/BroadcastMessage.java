package noir.messages;

import java.io.Serializable;

public class BroadcastMessage implements Serializable{
	private static final long serialVersionUID = 747361484179700087L;
	private Serializable message;

	public BroadcastMessage(Serializable message) {
		this.message = message;
	}

	public Serializable getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "[BroadcastMessage message=" + message + "]";
	}
	
}
