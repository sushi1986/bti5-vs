package phillip;



import java.math.BigInteger;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import akka.actor.UntypedActor;

public class WorkerSendingEverything extends UntypedActor {

	Set<BigInteger> primeSet;
	Set<BigInteger> calcSet;
	
	private static AtomicInteger idGenerator = new AtomicInteger();

	public WorkerSendingEverything() {
		// Wichtig: Wenn die ID nicht gesetzt wird, wird immer dieselbe In- //
		// stanz des Aktors r alle Remote-Aufrufe eines Clients verwendet!
		int actorId = idGenerator.addAndGet(1); // get next free actor ID
		getContext().setId(String.valueOf(actorId));
		System.out.println("Aktor wurde erstellt: " + actorId);
		a = new BigInteger(String.valueOf(actorId));
		primeSet = new TreeSet<BigInteger>();
		calcSet = new TreeSet<BigInteger>();
	}

	private BigInteger cnt = new BigInteger("0");
	private BigInteger a;
	private long time = 0;

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof CalcMessage) {
			Date past = new Date();
			
			// Beim ersten Aufruf wird der Sender ermittel
			CalcMessage calcMessage = (CalcMessage) message;
			// int result =
			// calculate(calculateMessage.getA(),calculateMessage.getB());
			if( !( calcSet.contains(calcMessage.getN()) ) || !( primeSet.contains(calcMessage.getN()) ) ){
				BigInteger resu = rho(calcMessage.getN(), a);

				if(resu == null){
					//evtl is N eine Primzahl
					if (calcMessage.getN().isProbablePrime(10)) {
					
						// TODO Terminiere und schicke dem Master alle daten
						
					}
				}
				
				
				if (resu.isProbablePrime(10)) {
					primeSet.add(resu);
				} else {
					getContext().reply(new CalcMessage(resu)); //TODO send to all
				}

				BigInteger other = calcMessage.getN().divide(resu);
				if (other.isProbablePrime(10)) {
					primeSet.add(other);
				} else {
					getContext().reply(new CalcMessage(other));//TODO send to all
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

}
