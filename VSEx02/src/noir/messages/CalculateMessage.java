package noir.messages;

import java.io.Serializable;
import java.math.BigInteger;

public class CalculateMessage implements Serializable{
	private static final long serialVersionUID = -121976766272391832L;
	private BigInteger N;
	
	public CalculateMessage(BigInteger N) {
		this.N = N;
	}
	
	public CalculateMessage(String N) {
		this.N = new BigInteger(N);
	}
	
	public BigInteger getN() {
		return N;
	}
	
}
