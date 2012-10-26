import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


public class Primefactorization {
	static BigInteger cnt = new BigInteger("0");
	
	public static void main(String[] args) {
		ArrayList<BigInteger> results = new ArrayList<BigInteger>();
		BigInteger start =  new BigInteger("1137047281562824484226171575219374004320812483047");
		BigInteger N = start;
		BigInteger a;
//		do { 
//			a = new BigInteger(N.bitLength(), new Random());
//		} while( a.compareTo(new BigInteger("0")) == 0 || a.compareTo(new BigInteger("-2")) == 0);
		a = new BigInteger("1");
//		System.out.println("Primefactor of " + N + " with a = " + a + ":");
		BigInteger save;
		for(int i = 0; i < 10; i++) {
			save = rho(N, a);
//			System.out.println("Result "+i+": " + save);
			if( save == null) {
				results.add(N);
				System.out.println("[!!!] rho returned: " + save);
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
		System.out.println("Iterations: " + cnt);
		for (int i = 0; i < results.size(); i++) {
			System.out.println("[R] "+results.get(i));
		}
	}
	
	private static BigInteger rho(BigInteger N, BigInteger a) {
		BigInteger x;
		do {
			x = new BigInteger(N.bitLength(), new Random());
		} while( x.compareTo(N) < 0);
//		System.out.println("[RHO] x = "+x);
		BigInteger y = x;
		BigInteger p = new BigInteger("1");
		do {
			cnt = cnt.add(new BigInteger("1"));
			x = x.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			BigInteger d = y.subtract(x).mod(N);
//			System.out.println("[RHO][" + cnt + "] x = " + x + ", y = " + y + ", d = " + d);
			p = d.gcd(N);
		} while (p.compareTo(new BigInteger("1")) == 0);
		
		if(p == N)
			return null;
		else 
			return p;
	}
}
