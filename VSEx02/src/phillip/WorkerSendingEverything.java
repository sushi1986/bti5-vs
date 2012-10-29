package phillip;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import de.haw.inet.vs.lab2.ResultMessage;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;

/**
 * TODO: Ich hab noch keine Ahnung wie ich terminierung feststellen soll...
 * 
 * @author phillipgesien
 * 
 */
public class WorkerSendingEverything extends UntypedActor {

	Set<BigInteger> primeSet;
	Set<BigInteger> calcSet;

	private static AtomicInteger idGenerator = new AtomicInteger();

	public WorkerSendingEverything() {
		// Wichtig: Wenn die ID nicht gesetzt wird, wird immer dieselbe In- //
		// stanz des Aktors r alle Remote-Aufrufe eines Clients verwendet!
		int actorId = idGenerator.addAndGet(1); // get next free actor ID
		getContext().setId(String.valueOf(actorId));
		System.out.println("Aktor sending all wurde erstellt: " + actorId);
		a = new BigInteger(String.valueOf(actorId));
		primeSet = new TreeSet<BigInteger>();
		calcSet = new TreeSet<BigInteger>();
	}

	private BigInteger cnt = new BigInteger("0");
	private BigInteger a;
	private long time = 0;

	private void broadcastWorkers(final Serializable message) {
		ActorRef self = getContext();
		for (ActorRef actor : Actors.registry().actors()) {
			if (!actor.equals(master)) {
				actor.tell(message, self);
			}
		}
	}

	private ActorRef master;

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof phillip.CalcMessage) {
			if (master == null) {
				master = getContext().getSender().get();
			}
			CalcMessage msg = (CalcMessage) message;
			System.out.println(a + "got messsage: N: " + msg.getN());
			Date past = new Date();

			// Beim ersten Aufruf wird der Sender ermittel
			CalcMessage calcMessage = (CalcMessage) message;
			// int result =
			// calculate(calculateMessage.getA(),calculateMessage.getB());
			if (!(calcSet.contains(calcMessage.getN()))
					&& !(primeSet.contains(calcMessage.getN()))) {
				calcSet.add(calcMessage.getN());
				BigInteger resu = rho(calcMessage.getN(), a);

				System.out.println(a + "Resu: " + resu + " N: " + msg.getN());

				if (resu == null) {
					System.out.println("Resu: " + resu);
					// evtl is N eine Primzahl
					if (calcMessage.getN().isProbablePrime(10)) {
						System.out.println(a + "PrimeSet " + primeSet);
						System.out.println(a + "CalcSet: " + calcSet);
						time += new Date().getTime() - past.getTime();
						System.out.println(a + "Time: " + time);
						// TODO Terminiere und schicke dem Master alle daten
						master.tell(new ResultMessage((int) time));
						getContext().tell(poisonPill());

					} else {
						broadcastWorkers(new CalcMessage(calcMessage.getN())); // TODO
																				// send
																				// to
																				// all
					}
					return;
				}

				boolean first = false;
				if (resu.isProbablePrime(10)) {
					System.out.println(a + "resu " + resu + " is a Prime");
					primeSet.add(resu);
					first = true;
				} else {
					// System.out.println("sent resu: "+resu);
					broadcastWorkers(new CalcMessage(resu)); // TODO send to all
				}

				BigInteger other = calcMessage.getN().divide(resu);
				if (other.isProbablePrime(10)) {

					System.out.println(a + "other " + other + " is a Prime");
					primeSet.add(other);
					if (first) {
						System.out.println(a + "primeSet: " + primeSet);
						System.out.println(a + "primeSet: " + calcSet);
						time += new Date().getTime() - past.getTime();
						System.out.println(a + "time: " + time);

						master.tell(new ResultMessage((int) time));
						getContext().tell(poisonPill());

					}
				} else {
					// System.out.println("sent other: "+other);
					broadcastWorkers(new CalcMessage(other));// TODO send to all
				}

			}

			time += new Date().getTime() - past.getTime();

		} else {
			throw new IllegalArgumentException("Unknown message [" + message
					+ "]");
		}
	}

	private BigInteger rho(BigInteger N, BigInteger a) {
		BigInteger x;
		do {
			x = new BigInteger(N.bitLength(), new Random());
		} while (x.compareTo(N) < 0);
		// System.out.println("[RHO] x = "+x);
		BigInteger y = x;
		BigInteger p = new BigInteger("1");
		do {
			cnt = cnt.add(new BigInteger("1"));
			x = x.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			y = y.pow(2).add(a).mod(N);
			BigInteger d = y.subtract(x).mod(N);
			// System.out.println("[RHO][" + cnt + "] x = " + x + ", y = " + y +
			// ", d = " + d);
			p = d.gcd(N);
		} while (p.compareTo(new BigInteger("1")) == 0);

		if (p == N)
			return null;
		else
			return p;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Worker - sending all");
		remote().start("localhost", 2552);
	}
}
