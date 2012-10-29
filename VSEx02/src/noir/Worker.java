package noir;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;
import static akka.actor.Actors.registry;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;

import java.io.Serializable;
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
import noir.messages.FinishedMessage;
import noir.messages.MasterMessage;
import noir.messages.PrimeMessage;
import noir.messages.ResultMessage;

public class Worker extends UntypedActor {
	
	/* THIS MUST BE CHANGED IN MASTER ACCORDINGLY */
	final static String MASTER_SERVER = "localhost";
	final static String WORKER_SERVER = "localhost";
	final static int MASTER_PORT = 2553;
	final static int WORKER_PORT = 2552;
	
	final static boolean DEBUG = true;
	
	private static AtomicInteger idGenerator = new AtomicInteger();
	private final static BigInteger BI_ZERO = new BigInteger("0");
	private final static BigInteger BI_ONE = new BigInteger("1");
	private SortedSet<BigInteger> primes;
	private BigInteger N;
	private BigInteger a;
	private BigInteger current;
	private boolean finished;
	private ActorRef master;
	
	public Worker() {
		int actorId = idGenerator.addAndGet(1);
		getContext().setId(String.valueOf(actorId));
		System.out.println("[N] Worker created: " + actorId);
		primes = new TreeSet<BigInteger>();
		master = null;
	}
	
	public static BigInteger rho(BigInteger N, BigInteger a) {
		BigInteger x;
//		Random rnd = new Random();
//		do {
//			x = new BigInteger(rnd.nextInt(N.bitLength()), rnd);
//		} while(x.compareTo(N) < 0);
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
		if(DEBUG) System.out.println("[N] work with " + N);
		BigInteger underTest = N;
		for(int cnt = 10; cnt > 0; cnt--) {
			BigInteger factor = rho(underTest, a);
			if(factor == null) {
				if(underTest.isProbablePrime(10)) {
					PrimeMessage pMessage = new PrimeMessage(underTest);
					broadcastWorkers(pMessage);
					master.tell(pMessage);
					if (!primes.contains(underTest)) {
						System.out.println("[N] Unknown prime: " + underTest);
						this.primes.add(underTest);
						while (current.mod(underTest).equals(BI_ZERO)) {
							current = current.divide(underTest);
						}
						if(DEBUG) System.out.println("[N] New problem: " + current);
						if(current.equals(BI_ONE)) {
//							finished = true;
							broadcastWorkers(new FinishedMessage());
						}
					}
					return;
				}
				else {
					/*
					 * Next try, maybe more luck ...
					 */
					underTest = N;
				}
			}
			else {
				underTest = factor;
			}
		}
//		System.out.println("[N] Could not finde all primes.\nPrimes found:");
//		for(BigInteger bi : primes) {
//			System.out.println("[N] > " + bi);
//		}
//		getContext().tell(poisonPill());
		System.out.println("Could not find something, perhaps someone else did.\nI AM DONE!");
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
			if(DEBUG) System.out.println("[N] Received Calculate Message: " + message.toString());
			master = getContext().getSender().get();
			CalculateMessage cMessage = (CalculateMessage) message;
			finished = false;
			N = cMessage.getN();
			current = N;
			do { 
				a = new BigInteger(N.bitLength(), new Random());
			} while( a.compareTo(BI_ZERO) == 0 || a.compareTo(new BigInteger("-2")) == 0);
			work(current);
		}
		else if (message instanceof PrimeMessage) {
			if(DEBUG) System.out.println("[N] Received Prime Message: " + message.toString());
			PrimeMessage pMessage = (PrimeMessage) message;
			BigInteger p = pMessage.getPrime();
			if (!primes.contains(p)) {
				System.out.println("[N] Unknown prime: " + p);
				this.primes.add(p);
				while (current.mod(p).equals(BI_ZERO)) {
					current = current.divide(p);
				}
				if(DEBUG) System.out.println("[N] New problem: " + current);
				if(current.equals(BI_ONE)) {
//					finished = true;
					broadcastWorkers(new FinishedMessage());
				}
				else {
					work(current);
				}
			}
			if(!current.equals(BI_ONE)){
				work(current);
			}
		}
		else if (message instanceof ResultMessage) {
			if(DEBUG) System.out.println("[N] Received Result Message: " + message.toString());
		}
		else if (message instanceof FinishedMessage) {
			if(!finished) {
				if(DEBUG) System.out.println("[N] Received Finished Message: " + message.toString());
				finished = true;
//				BigInteger[] results = new BigInteger[primes.size()];
//				int i = 0;
//				for (Iterator<BigInteger> itr = primes.iterator(); itr.hasNext();) {
//					BigInteger value = (BigInteger) itr.next();
//					results[i] = value;
//					i++;
//				}
//				master.tell(new ResultMessage(results));
				master.tell(new FinishedMessage());
			}
			getContext().tell(poisonPill());
		}
//		else if(message instanceof MasterMessage) {
//			if(DEBUG) System.out.println("[N] Received Master Message: " + message.toString());
//			MasterMessage mMessage = (MasterMessage) message;
//			this.master = mMessage.getMaster();
//		}
		else {
			throw new IllegalArgumentException("[N] Unknown message [" + message + "]");
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("[N] Worker");
		remote().start(WORKER_SERVER, WORKER_PORT);
	}
	
}
