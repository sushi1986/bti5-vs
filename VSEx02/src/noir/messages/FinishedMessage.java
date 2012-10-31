package noir.messages;

import java.io.Serializable;

public class FinishedMessage implements Serializable{
	private static final long serialVersionUID = -5636711540813993175L;
	private long time;
	private long iterations;
	
	public FinishedMessage(long time, long iterations) {
		this.time = time;
		this.iterations = iterations;
	}

	@Override
	public String toString() {
		return "[FinishedMessage time=" + time + "]";
	}

	public long getTime() {
		return time;
	}

	public long getIterations() {
		return iterations;
	}

}
