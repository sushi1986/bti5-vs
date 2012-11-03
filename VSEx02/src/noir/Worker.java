package noir;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import noir.messages.CalculateMessage;
import noir.messages.ContinueMessage;
import noir.messages.FactorMessage;
import noir.messages.FinishedMessage;
import noir.messages.PrimeMessage;
import noir.messages.SolvedMessage;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.japi.Procedure;
import akka.remoteinterface.RemoteSupport;

public class Worker extends UntypedActor {

	private static final int CERTAINTY = 20;
	private static final int CHECK_COUNTER = 500000;

	private static final BigInteger BI_ONE = new BigInteger("1");
	private static final BigInteger BI_ZERO = new BigInteger("0");
	private static final BigInteger BI_N_ONE = new BigInteger("-1");

	final static String MASTER_SERVER = "localhost";
	final static String WORKER_SERVER = "localhost";
	final static int MASTER_PORT = 2553;
	final static int WORKER_PORT = 2552;

	final static boolean DEBUG = false;

	private static AtomicInteger idGenerator = new AtomicInteger();
	private static AtomicInteger contIdGen = new AtomicInteger();
	private ActorRef master;
	private int actorId;

	private Set<BigInteger> solved;
	private BigInteger N;
	private BigInteger x;
	private BigInteger y;
	private BigInteger a;

	private boolean finished;
	private boolean isShuttingDown;
	
	private long time;
	private long timeNeeded;
	
	private long iterationsNeeded;
	
	private int nextContId;

	public Worker() {
		actorId = idGenerator.addAndGet(1);
		getContext().setId(String.valueOf(actorId));
		System.out.println("[N] (" + actorId + ") Worker created");

		solved = new TreeSet<BigInteger>();

		N = null;
		x = null;
		y = null;
		// a = null;
		a = new BigInteger(String.valueOf(actorId));

		master = null;
		finished = false;
		isShuttingDown = false;
		
		time = 0;
		timeNeeded = 0;
		iterationsNeeded = 0;
		
		nextContId = 0;
	}

	private BigInteger rho() {
		BigInteger p = null;
		int counter = 0;
		do {
			x = x.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			BigInteger d = y.subtract(x).mod(N);
			p = d.gcd(N);
			++counter;
			++iterationsNeeded;
		} while (p.compareTo(BI_ONE) == 0 && counter < CHECK_COUNTER);
		if (counter >= CHECK_COUNTER) {
			nextContId = contIdGen.incrementAndGet();
			getContext().tell(new ContinueMessage(nextContId, x, y, N));
			become(new Procedure<Object>() {
				@Override
				public void apply(final Object message) {
					time = new Date().getTime();
					if (message instanceof ContinueMessage) {
//						System.out.println("[N] (" + actorId + ") -> become ... received continue message");
						ContinueMessage cMessage = (ContinueMessage) message;
						if(cMessage.getId() == nextContId) {
							N = cMessage.getN();
							x = cMessage.getX();
							y = cMessage.getY();
							unbecome();
							work();
						}
					} else if (message instanceof SolvedMessage) {
//						System.out.println("[N] (" + actorId + ") -> become ... received solved message");
						SolvedMessage sMessage = (SolvedMessage) message;
						solved.add(sMessage.getFactor());
						if (N.compareTo(sMessage.getFactor()) == 0) {
							unbecome();
						}
					} else {
//						System.out.println("[N] (" + actorId + ") -> become ... appending message");
						getContext().tell(message);
					}
					timeNeeded += new Date().getTime() - time;
				}
			}, false);
			return BI_N_ONE;
		}
		if (p == N) {
			return null;
		} else {
			return p;
		}
	}

	private void work() {
		boolean hasSolved = false;
		boolean terminate = false;
		if (!solved.contains(N)) {
			BigInteger factor = rho();
			if (factor == null) {
				if (N.isProbablePrime(CERTAINTY)) {
					master.tell(new PrimeMessage(N));
					terminate = true;
					hasSolved = true;
				} else {
					do {
						x = new BigInteger(N.bitLength(), new Random());
					} while (x.compareTo(N) < 0);
					y = x;
					ActorRef me = getContext();
					me.tell(new FactorMessage(N));
					broadcast(new FactorMessage(N));
					// master.tell(new BroadcastMessage(new FactorMessage(N)),
					// me);
				}
			} else {
				if (factor.compareTo(BI_N_ONE) == 0) {
					// continue
				} else {
					hasSolved = true;
					BigInteger rest = N.divide(factor);
					boolean factorIsPrime = false;
					boolean restIsPrime = false;
					if (factor.isProbablePrime(CERTAINTY)) {
						factorIsPrime = true;
						master.tell(new PrimeMessage(factor));
					} else {
						ActorRef me = getContext();
						me.tell(new FactorMessage(factor));
						broadcast(new FactorMessage(factor));
						// master.tell(new BroadcastMessage(new FactorMessage(
						// factor)), me);
					}
					if (rest.isProbablePrime(CERTAINTY)) {
						restIsPrime = true;
						master.tell(new PrimeMessage(rest));
					} else {
						ActorRef me = getContext();
						me.tell(new FactorMessage(rest));
						broadcast(new FactorMessage(rest));
						// master.tell(new BroadcastMessage(
						// new FactorMessage(rest)), me);
					}
					if (factorIsPrime && restIsPrime) {
						terminate = true;
					}
				}
			}
			if (hasSolved) {
				solved.add(N);
				ActorRef me = getContext();
				broadcast(new SolvedMessage(N));
				// master.tell(new BroadcastMessage(new SolvedMessage(N)), me);
			}
		} else {
			terminate = true;
		}
		if (terminate) {
			if (!finished) {
				getContext().tell(new FinishedMessage(0, 0, 0));
				finished = true;
			}
		}
	}

	void broadcast(Serializable message) {
		ActorRef me = getContext();
		for(ActorRef actor : Actors.registry().actors()) {
			if(actor.compareTo(master) != 0) {
				actor.tell(message, me);
			}
		}
	}
	
	@Override
	public void onReceive(final Object message) throws Exception {
		time = new Date().getTime();
		if (message instanceof CalculateMessage) {
			ActorRef me = getContext();
			CalculateMessage cMessage = (CalculateMessage) message;
			N = cMessage.getN();
			master = getContext().getSender().get();
			// do {
			// a = new BigInteger(N.bitLength(),new Random());
			// } while (a.compareTo(BI_ZERO) == 0
			// || a.compareTo(new BigInteger("-2")) == 0);
			me.tell(new FactorMessage(N));
		} else if (message instanceof FactorMessage) {
			FactorMessage fMessage = (FactorMessage) message;
			N = fMessage.getFactor();
			do {
				x = new BigInteger(N.bitLength(), new Random());
			} while (x.compareTo(N) < 0);
			y = x;
			work();
		} else if (message instanceof SolvedMessage) {
			SolvedMessage sMessage = (SolvedMessage) message;
			solved.add(sMessage.getFactor());
		} else if (message instanceof FinishedMessage) {
			getContext().tell(message);
			become(new Procedure<Object>() {
				@Override
				public void apply(final Object message) {
					time = new Date().getTime();
					if(!isShuttingDown) {
						if (message instanceof FinishedMessage) {
							isShuttingDown = true;
							System.out.println("[N] (" + actorId + ") Worker shutting down.");
							timeNeeded += new Date().getTime() - time;
							master.tell(new FinishedMessage(actorId, timeNeeded, iterationsNeeded),getContext());
							getContext().tell(poisonPill());
						} else {
							unbecome();
							try {
								Worker.this.onReceive(message);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					timeNeeded += new Date().getTime() - time;
				}
			}, false);
		} else if (message instanceof ContinueMessage) {
			// continue
		} else {
			throw new IllegalArgumentException("[N] (" + actorId
					+ ") Unknown message [" + message + "]");
		}
		timeNeeded += new Date().getTime() - time;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("[N] Worker");
		RemoteSupport remoteServer = remote();
		remoteServer.start(WORKER_SERVER, WORKER_PORT);
	}
}
