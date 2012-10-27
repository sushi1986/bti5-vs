package phillip;

import java.io.Serializable;
import java.math.BigInteger;

public class CalcMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5473710640782279269L;
	private BigInteger N;

	public CalcMessage(BigInteger N) {
		
		this.N = N;
	}


	public BigInteger getN() {
		return N;
	}
}