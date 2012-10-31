package noir.messages;

import java.io.Serializable;
import java.math.BigInteger;

public class FactorMessage implements Serializable{
	private static final long serialVersionUID = 869164145910206453L;
	private BigInteger factor;
	
	public FactorMessage(BigInteger factor) {
		this.factor = factor;
	}

	public BigInteger getFactor() {
		return factor;
	}

	@Override
	public String toString() {
		return "[FactorMessage factor=" + factor + "]";
	}
}
