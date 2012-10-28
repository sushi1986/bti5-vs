package noir;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;

import akka.actor.UntypedActor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import noir.messages.CalculateMessage;
import noir.messages.ContinueMessage;
import noir.messages.FinishedMessage;
import noir.messages.PrimeMessage;
import noir.messages.ResultMessage;

public class Worker extends UntypedActor{
	
	// ... some other method for network stuff
	private static AtomicInteger idGenerator = new AtomicInteger();
	private final static BigInteger BI_ZERO = new BigInteger("0");
	private final static BigInteger BI_ONE = new BigInteger("1");
	private SortedSet<BigInteger> primes;
	private BigInteger N;
	private BigInteger a;
	private BigInteger current;
	private boolean finished;
	
	public Worker() {
		int actorId = idGenerator.addAndGet(1);
		getContext().setId(String.valueOf(actorId));
		System.out.println("[N] Worker created: " + actorId);
		primes = new TreeSet<BigInteger>();
	}
	
	public static BigInteger rho(BigInteger N, BigInteger a) {
		BigInteger x;
		do {
			x = new BigInteger(N.bitLength(), new Random());
		} while( x.compareTo(N) < 0);
		BigInteger y = x;
		BigInteger p = BI_ONE;
		do {
			x = x.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			BigInteger d = y.subtract(x).mod(N);
			p = d.gcd(N);
		} while (p.compareTo(BI_ONE) == 0);
		if(p == N) {
			return null;
		}
		else { 
			return p;
		}
	}
	
	private void work(BigInteger root) {
		BigInteger result = calculateFactor(N);
		if(result == null) {
			finished = true;
			// tell everyone
			System.out.println("[N] Done.");
			for (BigInteger bi : primes) {
				System.out.println("[N] > " + bi);
			}
		}
		else {
			getContext().tell(new ContinueMessage());
			current = result;
			// broadcast prime
		}
	}
	
	private BigInteger calculateFactor(BigInteger root) {
		BigInteger start = calculateSmallestKnown(root);
		for(;;) {
			BigInteger result = rho(start, a);
			if(result == null) {
				if (start.isProbablePrime(10)) {
					primes.add(start);
					return null;
				}
				else {
					System.out.println("[N] This still has problems");
					System.exit(-1);
				}
			}
			else {
				return result;
			}
		}
	}
	
	private BigInteger calculateSmallestKnown(BigInteger root) {
		if(primes.isEmpty()) {
			return root;
		}
		else {
			for (Iterator<BigInteger> itr = primes.iterator(); itr.hasNext();) {
				BigInteger divisor = (BigInteger) itr.next();
				while(root.mod(divisor).compareTo(BI_ZERO) == 0) {
					root = root.divide(divisor);
				}
			}
			return root;
		}
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof CalculateMessage) {
			CalculateMessage cMessage = (CalculateMessage) message;
			finished = false;
			this.N = cMessage.getN();
			do { 
				a = new BigInteger(N.bitLength(), new Random());
			} while( a.compareTo(BI_ZERO) == 0 || a.compareTo(new BigInteger("-2")) == 0);
			work(N);
		}
		else if (message instanceof PrimeMessage) {
			
		}
		else if(message instanceof ContinueMessage) {
			if(!finished) {
				work(current);
			}
		}
		else if (message instanceof ResultMessage) {
			
		}
		else if (message instanceof FinishedMessage) {
			
		}
		
	}

}
