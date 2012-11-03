package noir;

public class WorkerData {
	private long time;
	private long iterations;
	
	public WorkerData(long time, long iterations) {
		this.time = time;
		this.iterations = iterations;
	}

	public long getTime() {
		return time;
	}

	public long getIterations() {
		return iterations;
	}

	@Override
	public String toString() {
		return "[WorkerData time=" + time + ", iterations=" + iterations + "]";
	}
}
