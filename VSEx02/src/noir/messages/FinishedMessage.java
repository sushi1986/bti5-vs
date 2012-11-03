package noir.messages;

import java.io.Serializable;

public class FinishedMessage implements Serializable{
	private static final long serialVersionUID = -5636711540813993175L;
	private long id;
	private long time;
	private long iterations;
	
	public FinishedMessage(long id, long time, long iterations) {
		this.id = id;
		this.time = time;
		this.iterations = iterations;
	}

	public long getId() {
		return id;
	}
	
	public long getTime() {
		return time;
	}

	public long getIterations() {
		return iterations;
	}

	@Override
	public String toString() {
		return "[FinishedMessage id=" + id + ", time=" + time + ", iterations="
				+ iterations + "]";
	}
}
