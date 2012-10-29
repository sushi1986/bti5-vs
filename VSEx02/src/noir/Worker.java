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
import java.util.concurrent.atomic.AtomicInteger;

import noir.messages.CalculateMessage;
import noir.messages.FinishedMessage;
import noir.messages.PrimeMessage;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;

public class Worker extends UntypedActor {
	
	/* THIS MUST BE CHANGED IN MASTER ACCORDINGLY */
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
	private BigInteger current;
	private ActorRef master;
	private int actorId;
	private long timeNeeded;
	
	public Worker() {
		actorId = idGenerator.addAndGet(1);
		getContext().setId(String.valueOf(actorId));
		System.out.println("[N] Worker created: " + actorId);
		primes = new TreeSet<BigInteger>();
		calculated = new TreeSet<BigInteger>();
		master = null;
		timeNeeded = 0;
	}
	
	public static BigInteger rho(BigInteger N, BigInteger a) {
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
		} while (p.compareTo(BI_ONE) == 0);
		if(p == N) {
			return null;
		}
		else { 
			return p;
		}
	}
	
	private void work(BigInteger N) {
		if(DEBUG) System.out.println("[N] ("+actorId+") work with " + N);
		BigInteger underTest = N;
		for(int cnt = 10; cnt > 0; cnt--) {
			BigInteger factor = rho(underTest, a);
			if(factor == null) {
				if(underTest.isProbablePrime(10)) {
					PrimeMessage pMessage = new PrimeMessage(underTest);
					broadcastWorkers(pMessage);
					master.tell(pMessage);
					if (!primes.contains(underTest)) {
						System.out.println("[N] ("+actorId+") Unknown prime: " + underTest);
						primes.add(underTest);
						calculated.add(N);
						while (current.mod(underTest).equals(BI_ZERO)) {
							current = current.divide(underTest);
						}
						if(DEBUG) System.out.println("[N] ("+actorId+") New problem: " + current);
						if(current.equals(BI_ONE)) {
							broadcastWorkers(new FinishedMessage());
						}
					}
					return;
				}
				else {
					// calculate new a?
					underTest = N;
				}
			}
			else {
				underTest = factor;
			}
		}
		System.out.println("[N] ("+actorId+") Could not find something, perhaps someone else did.\nI AM DONE!");
	}
	
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
			current = N;
//			do { 
//				a = new BigInteger(N.bitLength(), new Random());
//			} while( a.compareTo(BI_ZERO) == 0 || a.compareTo(new BigInteger("-2")) == 0);
			a = new BigInteger(String.valueOf(actorId));
			work(current);
			timeNeeded += (new Date().getTime() - time);
		}
		else if (message instanceof PrimeMessage) {
			long time = new Date().getTime();
			if(DEBUG) System.out.println("[N] ("+actorId+") Received Prime Message: " + message.toString());
			PrimeMessage pMessage = (PrimeMessage) message;
			BigInteger p = pMessage.getPrime();
			if (!primes.contains(p)) {
				System.out.println("[N] ("+actorId+") Unknown prime: " + p);
				this.primes.add(p);
				while (current.mod(p).equals(BI_ZERO)) {
					current = current.divide(p);
				}
				if(DEBUG) System.out.println("[N] ("+actorId+") New problem: " + current);
				if(current.equals(BI_ONE)) {
					broadcastWorkers(new FinishedMessage());
				}
				else {
					if(!calculated.contains(current)) {
						work(current);
					}
				}
			}
			if(!current.equals(BI_ONE)){
				if(!calculated.contains(current)) {
					work(current);
				}
			}
			timeNeeded += (new Date().getTime() - time);
		}
		else if (message instanceof FinishedMessage) {
			long time = new Date().getTime();
			if(!finished) {
				if(DEBUG) System.out.println("[N] ("+actorId+") Received Finished Message: " + message.toString());
				finished = true;
				timeNeeded += (new Date().getTime() - time);
				master.tell(new FinishedMessage(timeNeeded));
			}
			getContext().tell(poisonPill());
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
