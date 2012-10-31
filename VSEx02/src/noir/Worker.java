package noir;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import noir.messages.CalculateMessage;
import noir.messages.FactorMessage;
import noir.messages.FinishedMessage;
import noir.messages.PrimeMessage;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;

public class Worker extends UntypedActor {

	final static String MASTER_SERVER = "localhost";
	final static String WORKER_SERVER = "localhost";
	final static int MASTER_PORT = 2553;
	final static int WORKER_PORT = 2552;
	
	final static boolean DEBUG = false;
	
	private static AtomicInteger idGenerator = new AtomicInteger();
	private boolean finished;
	private final static BigInteger BI_ZERO = new BigInteger("0");
	private final static BigInteger BI_ONE = new BigInteger("1");
	private SortedSet<BigInteger> primes;
	private Set<BigInteger> calculated;
	private BigInteger N;
	private BigInteger a;
	private ActorRef master;
	private int actorId;
	private long timeNeeded;
	private long iterationsNeeded;
//	private BigInteger condition; 
	
	public Worker() {
		actorId = idGenerator.addAndGet(1);
		getContext().setId(String.valueOf(actorId));
		System.out.println("[N] ("+actorId+") Worker created");
		primes = new TreeSet<BigInteger>();
		calculated = new TreeSet<BigInteger>();
		master = null;
		timeNeeded = 0;
		iterationsNeeded = 0;
	}
	
//	public BigInteger rho(BigInteger N, BigInteger a) throws TimeoutException {
//		do {
//			BigInteger x;
//			do {
//				x = new BigInteger(N.bitLength(), new Random());
//			} while(x.compareTo(N) < 0);
//			BigInteger y = x;
//			BigInteger p = BI_ONE;
//			int counter = 0;
//			do {
//				x = x.pow(2).add(a).mod(N);
//				y = y.pow(2).add(a).mod(N);
//				y = y.pow(2).add(a).mod(N);
//				BigInteger d = y.subtract(x).mod(N);
//				p = d.gcd(N);
//				++counter;
//				++iterationsNeeded;
//			} while (p.compareTo(BI_ONE) == 0 && counter <= 1000000);
//			if(counter >= 1000000) {
//				counter = 0;
//				do { 
//					a = new BigInteger(N.bitLength(), new Random());
//				} while( a.compareTo(BI_ZERO) == 0 || a.compareTo(new BigInteger("-2")) == 0);
//				System.err.println("[N] ("+actorId+") [!!!] Restart rho with new randomness!");
//			} else {
//				if(p == N) {
//					return null;
//				}
//				else { 
//					return p;
//				}
//			}
//		} while(true);
//	}
	
	public BigInteger rho(BigInteger N, BigInteger a) throws TimeoutException {
		BigInteger x;
		do {
			x = new BigInteger(N.bitLength(), new Random());
		} while(x.compareTo(N) < 0);
		BigInteger y = x;
		BigInteger p = BI_ONE;
		do {
			x = x.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			BigInteger d = y.subtract(x).mod(N);
			p = d.gcd(N);
			++iterationsNeeded;
		} while (p.compareTo(BI_ONE) == 0);
		if(p == N) {
			return null;
		}
		else { 
			return p;
		}
	}
	
//	public BigInteger rho(BigInteger N, BigInteger a) throws TimeoutException {
//		BigInteger x;
//		do {
//			x = new BigInteger(N.bitLength(), new Random());
//		} while(x.compareTo(N) < 0);
//		BigInteger y = x;
//		BigInteger p = BI_ONE;
//		do {
//			x = x.pow(2).add(a).mod(N);
//			y = y.pow(2).add(a).mod(N);
//			y = y.pow(2).add(a).mod(N);
//			BigInteger d = y.subtract(x).mod(N);
//			p = d.gcd(N);
//			++iterationsNeeded;
//		} while (p.compareTo(BI_ONE) == 0);
//		if(p == N) {
//			return null;
//		}
//		else { 
//			return p;
//		}
//	}
	
	private boolean work(BigInteger N) throws TimeoutException {
		if(DEBUG) System.out.println("[N] ("+actorId+") work with " + N);
		BigInteger factor = rho(N, a);
		if(factor == null) {
			if(N.isProbablePrime(10)) {
				if (!primes.contains(N)) {
					master.tell(new PrimeMessage(N));
					primes.add(N);
				}
			}
			else {
				if(DEBUG) System.out.println("[N] ("+actorId+") work brodcasting factor: " + N);
				broadcastWorkers(new FactorMessage(N));
			}
		}
		else if(factor.isProbablePrime(10)) {
			master.tell(new PrimeMessage(factor));
			BigInteger rest = N.divide(factor);
			if(rest.isProbablePrime(10)) {
				master.tell(new PrimeMessage(rest));
				return true;
			}
			calculated.add(N);
			if(DEBUG) System.out.println("[N] ("+actorId+") work brodcasting rest: " + rest);
			broadcastWorkers(new FactorMessage(rest));
		}
		else {
			calculated.add(N);
			BigInteger rest = N.divide(factor);
			if(DEBUG) System.out.println("[N] ("+actorId+") work brodcasting everything: " + rest);
			broadcastWorkers(new FactorMessage(factor));
			broadcastWorkers(new FactorMessage(rest));
		}
		return false;
	}
	
//	private void work(BigInteger N) throws TimeoutException {
//		if(DEBUG) System.out.println("[N] ("+actorId+") work with " + N);
//		BigInteger factor = rho(N, a);
//		if(factor == null) {
//			if(N.isProbablePrime(10)) {
//				if (!primes.contains(N)) {
//					master.tell(new PrimeMessage(N));
//					broadcastWorkers(new PrimeMessage(N));
////					primes.add(N);
//				}
//			}
//			else {
//				if(DEBUG) System.out.println("[N] ("+actorId+") work brodcasting factor: " + N);
//				broadcastWorkers(new FactorMessage(N));
//			}
//		}
//		else {
//			calculated.add(N);
//			BigInteger rest = N.divide(factor);
//			if(DEBUG) System.out.println("[N] ("+actorId+") work brodcasting everything: " + rest);
//			broadcastWorkers(new FactorMessage(factor));
//			broadcastWorkers(new FactorMessage(rest));
//		}
//	}
	
	private void broadcastWorkers(final Serializable message) {
		ActorRef self = getContext();
		for(ActorRef actor : Actors.registry().actors()) {
			if(!actor.equals(master)) {
				actor.tell(message, self);
			}
		}
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof CalculateMessage) {
			long time = new Date().getTime();
			if(DEBUG) System.out.println("[N] ("+actorId+") Received Calculate Message: " + message.toString());
			master = getContext().getSender().get();
			CalculateMessage cMessage = (CalculateMessage) message;
			finished = false;
			N = cMessage.getN();
//			condition = N;
			do { 
				a = new BigInteger(N.bitLength(), new Random());
			} while( a.compareTo(BI_ZERO) == 0 || a.compareTo(new BigInteger("-2")) == 0);
			if (work(N)) {
				timeNeeded += (new Date().getTime() - time);
				master.tell(new FinishedMessage(timeNeeded, iterationsNeeded));
				getContext().tell(poisonPill());
				finished = true;
			}
//			work(N);
			timeNeeded += (new Date().getTime() - time);
		}
		else if (message instanceof FactorMessage) {
			if(DEBUG) System.out.println("[N] ("+actorId+") Received FactorMassage: " + message.toString());
			long time = new Date().getTime();
			if(!finished) {
				FactorMessage fMessage = (FactorMessage) message;
				if(!calculated.contains(fMessage.getFactor())) {
					if (work(fMessage.getFactor())) {
						timeNeeded += (new Date().getTime() - time);
						master.tell(new FinishedMessage(timeNeeded, iterationsNeeded));
						finished = true;
						getContext().tell(poisonPill());
					}
//					work(fMessage.getFactor());
				}
			}
			timeNeeded += (new Date().getTime() - time);
		}
//		else if(message instanceof PrimeMessage) {
//			long time = new Date().getTime();
//			PrimeMessage pMessage = (PrimeMessage) message;
//			BigInteger prime = pMessage.getPrime();
//			if(!primes.contains(prime)) {
//				primes.add(prime);
//				while(condition.mod(prime).compareTo(BI_ZERO) == 0) {
//					condition = condition.divide(prime);
//				}
//				if(condition.compareTo(BI_ONE) == 0) {
//					timeNeeded += (new Date().getTime() - time);
//					master.tell(new FinishedMessage(timeNeeded, iterationsNeeded));
//					finished = true;
//					getContext().tell(poisonPill());
//				}
//			}
//			timeNeeded += (new Date().getTime() - time);
//		}
		else if (message instanceof FinishedMessage) {
			if(DEBUG) System.out.println("[N] ("+actorId+") Received FinishMassage - Not yet implemented.");
		}
		else {
			throw new IllegalArgumentException("[N] ("+actorId+") Unknown message [" + message + "]");
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("[N] Worker");
		remote().start(WORKER_SERVER, WORKER_PORT);
	}
	
}
