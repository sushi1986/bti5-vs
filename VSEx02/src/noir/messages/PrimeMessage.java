package noir.messages;

import java.io.Serializable;
import java.math.BigInteger;

public class PrimeMessage implements Serializable {
	private static final long serialVersionUID = -4900071350085175939L;
	private BigInteger prime;
	
	public PrimeMessage(BigInteger prime) {
		this.prime = prime;
	}
	
	public PrimeMessage(String prime) {
		this.prime = new BigInteger(prime);
	}
	
	public BigInteger getPrime() {
		return prime;
	}
	
}
