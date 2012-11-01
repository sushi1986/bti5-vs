package noir;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import noir.messages.FinishedMessage;
import noir.messages.AlreadyCalculatedMessage;
import noir.messages.FactorMessage;
import noir.messages.PrimeMessage;

public class WorkerThread extends Thread {

	private static final int CERTAINTY = 10;
	
	private static final boolean DEBUG = false;
	
	private static final BigInteger BI_ONE = new BigInteger("1");
	private static final BigInteger BI_ZERO = new BigInteger("0");
	private static final BigInteger BI_N_ONE = new BigInteger("-1");
	
	private long iterationsNeeded;
	private long timeNeeded;
	
	private int id;
	
	private BigInteger a;
	
	private BigInteger problem;
	
	private boolean running;
	private boolean solving;
	
	private Worker actor;
	
	private LinkedBlockingQueue<BigInteger> queue;
	
	private Set<BigInteger> primes;
	private Set<BigInteger> solved;
	
	private Lock primes_mtx;
	private Lock solved_mtx;
	
	private Condition primes_cond;
	private Condition solved_cond;
	
	public WorkerThread(Worker actor, int id) {
		this.actor = actor;
		this.id = id;
		this.a = new BigInteger(String.valueOf(id));
		
		running = true;
		solving = false;
		
		iterationsNeeded = 0;
		timeNeeded = 0;
		
		queue = new LinkedBlockingQueue<BigInteger>();
		
		primes = new TreeSet<BigInteger>();
		solved = new TreeSet<BigInteger>();
		
		primes_mtx = new ReentrantLock();
		solved_mtx = new ReentrantLock();
		
		primes_cond = primes_mtx.newCondition();
		solved_cond = solved_mtx.newCondition();
		
		
	}
	
	@Override
	public void run() {
		System.out.println("[N] (" + id + ") thread running ...");
		while(running) {
			BigInteger current = null;
			solved_mtx.lock();
			while(current == null || solved.contains(current)) {
				solved_mtx.unlock();
				current = queue.poll();
				solved_mtx.lock();
			} 
			solved_mtx.unlock();
			if(DEBUG) System.out.println("[N] (" + id + ") WORKING ON: " + current);
			BigInteger factor = rho(current, a);
			if(factor == null) {
				if(current.isProbablePrime(CERTAINTY)) {
					actor.sendMaster(new PrimeMessage(current));
					System.out.println("[N] (" + id + ") >>> " + current);
					solved_mtx.lock();
					solved.add(current);
					solved_mtx.unlock();
					actor.broadcastWorkers(new AlreadyCalculatedMessage(current));
					continue;
				}
			}
			else if (factor.compareTo(BI_N_ONE) == 0) {
				continue;
			}
			else {
				BigInteger rest = current.divide(factor);
				actor.broadcastWorkers(new FactorMessage(factor));
				actor.broadcastWorkers(new FactorMessage(rest));
				actor.broadcastWorkers(new AlreadyCalculatedMessage(current));
				queue.offer(factor);
				queue.offer(rest);
				solved_mtx.lock();
				solved.add(current);
				solved_mtx.unlock();
			}
			if(queue.isEmpty()) {
				System.out.println("[N] (" + id + ") Empyt queue, bye!");
				running = false;
			}
		}
		System.out.println("[N] (" + id + ") thread done!");
	}

	private BigInteger rho(BigInteger N, BigInteger a) {
		solved_mtx.lock();
		problem = N;
		solving = true;
		solved_mtx.unlock();
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
		} while (p.compareTo(BI_ONE) == 0 && solving);
		if(!solving) {
			if(DEBUG) System.out.println("[N] (" + id + ") WAS INTERRUPT FACTORING " + N);
			return BI_N_ONE;
		}
		if(p == N) {
			return null;
		}
		else { 
			return p;
		}
	}
	
	public void addWork(BigInteger factor) {
		solved_mtx.lock();
		if(!solved.contains(factor)) {
			queue.offer(factor);
		}
		solved_mtx.unlock();
	}
	
	public void addSolved(BigInteger factor) {
		solved_mtx.lock();
		if(factor.compareTo(problem) == 0) {
			if(DEBUG) System.out.println("[N] (" + id + ") SHOULD BE INTERRUPT");
			solving = false;
		}
		solved.add(factor);
		solved_mtx.unlock();
	}
}
