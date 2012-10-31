package rh.messages;

import java.io.Serializable;

public class FinishedMessage implements Serializable{
	private static final long serialVersionUID = -5636711540813993175L;
	private long time;
	
	public FinishedMessage() {
		this.time = -1;
	}
	
	public FinishedMessage(long time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "[FinishedMessage time=" + time + "]";
	}

	public long getTime() {
		return time;
	}

}
