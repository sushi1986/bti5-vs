package noir.messages;

import java.io.Serializable;
import java.math.BigInteger;

public class ContinueMessage implements Serializable{
	private static final long serialVersionUID = -3362726529809312802L;
	private int id;
	private BigInteger x;
	private BigInteger y;
	private BigInteger N;
	
	public ContinueMessage(final int id, final BigInteger x, final BigInteger y, final BigInteger N) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.N = N;
	}
	
	public int getId() {
		return id;
	}

	public BigInteger getX() {
		return x;
	}

	public BigInteger getY() {
		return y;
	}

	public BigInteger getN() {
		return N;
	}

	@Override
	public String toString() {
		return "[ContinueMessage id=" + id + ", x=" + x + ", y=" + y + ", N=" + N + "]";
	}
}
