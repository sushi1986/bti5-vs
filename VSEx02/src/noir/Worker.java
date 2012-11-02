package noir;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;

import java.math.BigInteger;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import noir.messages.BroadcastMessage;
import noir.messages.CalculateMessage;
import noir.messages.ContinueMessage;
import noir.messages.FactorMessage;
import noir.messages.FinishedMessage;
import noir.messages.PrimeMessage;
import noir.messages.SolvedMessage;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.japi.Procedure;

public class Worker extends UntypedActor {

	private static final int CERTAINTY = 20;
	private static final int CHECK_COUNTER = 1000000;

	private static final BigInteger BI_ONE = new BigInteger("1");
	private static final BigInteger BI_ZERO = new BigInteger("0");
	private static final BigInteger BI_N_ONE = new BigInteger("-1");

	final static String MASTER_SERVER = "localhost";
	final static String WORKER_SERVER = "localhost";
	final static int MASTER_PORT = 2553;
	final static int WORKER_PORT = 2552;

	final static boolean DEBUG = false;

	private static AtomicInteger idGenerator = new AtomicInteger();
	private ActorRef master;
	private int actorId;

	private Set<BigInteger> solved;
	private BigInteger N;
	private BigInteger x;
	private BigInteger y;
	private BigInteger a;
	
	private boolean finished;

	public Worker() {
		actorId = idGenerator.addAndGet(1);
		getContext().setId(String.valueOf(actorId));
		System.out.println("[N] (" + actorId + ") Worker created");

		solved = new TreeSet<BigInteger>();

		N = null;
		x = null;
		y = null;
//		a = null;
		a = new BigInteger(String.valueOf(actorId));

		master = null;
		finished = false;
	
	}
	
	private void trySuicide() {
		System.out.println("[N] ("+actorId+") Is there any work left?");
		become(new Procedure<Object>() {
			@Override
			public void apply(final Object message) {
				if(!finished) {
					if (message instanceof FinishedMessage) {
						System.out.println("[N] ("+actorId+") DONE!!1!");
						if(master != null)
							master.tell(new FinishedMessage(actorId, 0),getContext());
						finished = true;
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
			}
		}, false);
		getContext().tell(new FinishedMessage(0, 0));
	}

	private BigInteger rho() throws Exception{
		BigInteger p = null;
		int counter = 0;
		do {
			x = x.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			BigInteger d = y.subtract(x).mod(N);
			p = d.gcd(N);
			++counter;
		} while (p.compareTo(BI_ONE) == 0 && counter < CHECK_COUNTER);
		if (counter >= CHECK_COUNTER) {
			getContext().tell(new ContinueMessage(x, y, N));
			become(new Procedure<Object>() {
				@Override
				public void apply(final Object message) {
					System.out.println("[N] IN rho -> become ...");
					if (message instanceof ContinueMessage) {
						System.out.println("[N] IN rho -> become ... received continue message");
						ContinueMessage cMessage = (ContinueMessage) message;
						N = cMessage.getN();
						x = cMessage.getX();
						y = cMessage.getY();
						unbecome();
						// rho(); // todo: handle rho result
						work();
					} else if (message instanceof SolvedMessage) {
						System.out
								.println("[N] IN rho -> become ... received solved message");
						SolvedMessage sMessage = (SolvedMessage) message;
						solved.add(sMessage.getFactor());
						if (N.compareTo(sMessage.getFactor()) == 0) {
							unbecome();
						}
					} else {
						System.out
								.println("[N] IN rho -> become ... appending message");
						getContext().tell(message);
					}
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
			if(a == null) {
				System.out.println("### BEFORE RHO ###\nN: "+N+"\nx: "+x+"\ny: "+y+"\na: "+a+"\nactorid: " + actorId);
			}

			BigInteger factor = null;
			try {
				factor = rho();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("### AFTER RHO ###\nN: "+N+"\nx: "+x+"\ny: "+y+"\na: "+a+"\nactorid: " + actorId);
				System.exit(0);
			}
			System.out.println("[N] Rho factor is: " + factor);
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
					master.tell(new BroadcastMessage(new FactorMessage(N)));
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
						master.tell(new BroadcastMessage(new FactorMessage(
								factor)));
					}
					if (rest.isProbablePrime(CERTAINTY)) {
						restIsPrime = true;
						master.tell(new PrimeMessage(rest));
					} else {
						master.tell(new BroadcastMessage(
								new FactorMessage(rest)));
					}
					if (factorIsPrime && restIsPrime) {
						terminate = true;
					}
				}
			}
			if (hasSolved) {
				solved.add(N);
				master.tell(new BroadcastMessage(new SolvedMessage(N)));
			}
		} else {
			terminate=true;
		}
		if (terminate) {
			trySuicide();
//			System.out.println("[N] ("+actorId+") Is there any work left?");
//			become(new Procedure<Object>() {
//				@Override
//				public void apply(final Object message) {
//					if (message instanceof FinishedMessage) {
//						System.out.println("[N] ("+actorId+") DONE!!1!");
//						
//						master.tell(new FinishedMessage(actorId, 0),getContext());
//						finished = true;
//						getContext().tell(poisonPill());
//					} else {
//						if(!finished) {
//							unbecome();
//							try {
//								Worker.this.onReceive(message);
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//						else {
//							System.out.println("[N] ("+actorId+") Skipping message");
//						}
//					}
//				}
//			}, false);
//			getContext().tell(new FinishedMessage(0, 0));
		}
	}

	@Override
	public void onReceive(final Object message) throws Exception {
		if(actorId > 2 ){//TODO
			getContext().tell(poisonPill());
			return;
		}
		
		if (message instanceof CalculateMessage) {
			ActorRef me = getContext();
			CalculateMessage cMessage = (CalculateMessage) message;
			N = cMessage.getN();
			master = getContext().getSender().get();
//			do {
//				a = new BigInteger(N.bitLength(),new Random());
//			} while (a.compareTo(BI_ZERO) == 0
//					|| a.compareTo(new BigInteger("-2")) == 0);
			me.tell(new FactorMessage(N));
		} else if (message instanceof FactorMessage) {
			// boolean hasSolved = false;
			// boolean terminate = false;
			FactorMessage fMessage = (FactorMessage) message;
			System.out.println("[N][OnRcv][fMsg] Message: " + fMessage);
			N = fMessage.getFactor();
			do {
				x = new BigInteger(N.bitLength(), new Random());
			} while (x.compareTo(N) < 0);
			y = x;
			work();
			
		} else if (message instanceof SolvedMessage) {
			SolvedMessage sMessage = (SolvedMessage) message;
			solved.add(sMessage.getFactor());
			trySuicide();
		} else if (message instanceof FinishedMessage) {
			// continue
			trySuicide();
		} else if (message instanceof ContinueMessage) {
			// continue
			trySuicide();
		} else {
			throw new IllegalArgumentException("[N] (" + actorId
					+ ") Unknown message [" + message + "]");
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("[N] Worker");
		remote().start(WORKER_SERVER, WORKER_PORT);
	}

}
