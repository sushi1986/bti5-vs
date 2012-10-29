package noir.messages;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

public class ResultMessage implements Serializable {
	private static final long serialVersionUID = -6678706018831793018L;
	private BigInteger[] results;
	
	public ResultMessage(final BigInteger[] results) {
		this.results = results;
	}
	
	public BigInteger[] getResults() {
		return results;
	}

	@Override
	public String toString() {
		return "[<ResultMessage> results=" + Arrays.toString(results) + "]";
	}
	
	
}
