
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


public class Primefactorization {
	
	public static void main(String[] args) {
		ArrayList<BigInteger> results = new ArrayList<BigInteger>();
		BigInteger start =  new BigInteger("1137047281562824484226171575219374004320812483047");
		BigInteger N = start;
		BigInteger a;
		do {
			a = new BigInteger(N.bitLength(), new Random());
		} while( a.compareTo(new BigInteger("0")) == 0 || a.compareTo(new BigInteger("-2")) == 0);
		System.out.println("Primefactor of " + N + "\n> with a = " + a + ":");
		BigInteger save;
		for(int i = 0; i < 10; i++) {
			save = rho(N, a);
			if( save == null) {
				if(N != null) {
					results.add(N);
				}
				break;
			}
			if(save.isProbablePrime(10)) {
				results.add(save);
				BigInteger pro = new BigInteger("1");
				for (Iterator iterator = results.iterator(); iterator.hasNext();) {
					pro = pro.multiply((BigInteger) iterator.next());
					
				}
				N = start.divide(pro);
			}
			else {
				N = save;
			}
		}
		System.out.println(rho(N, a));
		for (int i = 0; i < results.size(); i++) {
			System.out.println("[R] "+results.get(i));
		}
	}
	
	public static BigInteger rho(BigInteger N, BigInteger a) {
		BigInteger x;
		do {
			x = new BigInteger(N.bitLength(), new Random());
		} while( x.compareTo(N) < 0);
		BigInteger y = x;
		BigInteger p = new BigInteger("1");
		int cnt = 0;
		do {
			x = x.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			BigInteger d = y.subtract(x).mod(N);
			p = d.gcd(N);
			++cnt;
		} while (p.compareTo(new BigInteger("1")) == 0);
		
		if(p == N)
			return null;
		else 
			return p;
	}
}
