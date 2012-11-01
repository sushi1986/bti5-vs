package noir.messages;

import java.io.Serializable;
import java.math.BigInteger;

public class AlreadyCalculatedMessage implements Serializable{
	private static final long serialVersionUID = -1274255035154538402L;
	private BigInteger factor;
	
	public AlreadyCalculatedMessage(final BigInteger factor) {
		this.factor = factor;
	}

	public BigInteger getFactor() {
		return factor;
	}

	@Override
	public String toString() {
		return "[AlreadyCalculatedMessage factor=" + factor + "]";
	}
}
