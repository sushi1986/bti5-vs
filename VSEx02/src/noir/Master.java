package noir;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import noir.messages.CalculateMessage;
import noir.messages.FinishedMessage;
import noir.messages.PrimeMessage;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

public class Master extends UntypedActor {
	
	/* THIS MUST BE CHANGED IN WORKER ACCORDINGLY */
	final static String MASTER_SERVER = "localhost";
	final static String WORKER_SERVER = "localhost";
	final static int MASTER_PORT = 2553;
	final static int WORKER_PORT = 2552;
	
	final static String PRIME = "1000602106143806596478722974273666950903906112131794745457338659266842446985022076792112309173975243506969710503";
	
	final int NUMBER_OF_WORKERS = 7;
	
	final static boolean DEBUG = true;
	
	List<ActorRef> workers;
	SortedSet<BigInteger> results; 
	int finishedWorkers;
	long time;
	
	public Master() {
		workers = new ArrayList<ActorRef>();
		results = new TreeSet<BigInteger>();
		finishedWorkers = 0;
		time = 0;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof CalculateMessage) {
			time = new Date().getTime();
			if(DEBUG) System.out.println("[N] (Master) Received CalculateMessage." + message.toString());
			CalculateMessage cMessage = (CalculateMessage) message;
			ActorRef me = getContext();
			for(int i = 0; i < NUMBER_OF_WORKERS; ++i) {
				ActorRef worker = remote().actorFor(Worker.class.getName(), WORKER_SERVER, WORKER_PORT);
				worker.tell(cMessage, me);
				workers.add(worker);
			}
		}
		else if (message instanceof PrimeMessage) {
			if(DEBUG) System.out.println("[N] Master Received PrimeMessage." + message.toString());
			PrimeMessage pMessage= (PrimeMessage) message;
			results.add(pMessage.getPrime());
		}
		else if (message instanceof FinishedMessage) {
			if(DEBUG) System.out.println("[N] Master Received FinishMessage." + message.toString());
			FinishedMessage fMessage = (FinishedMessage) message;
			System.out.println("[N] Time needed by some worker: " + fMessage.getTime());
			++finishedWorkers;
			if(finishedWorkers >= NUMBER_OF_WORKERS) {
				System.out.println("[N] Prime factors found:");
				for(BigInteger bi : results) {
					System.out.println("[N] > " + bi);
				}
				System.out.println("[N] (Master) Calculation took: " + (new Date().getTime() - time) + ".");
				getContext().tell(poisonPill());
			}
		}
		else {
			throw new IllegalArgumentException("[N] (Master) Unknown message [" + message + "]");
		}
	}
	
	public static void main(String[] args) {
		System.out.println("[N] Master");
		RemoteServerModule remoteServer = remote().start(MASTER_SERVER, MASTER_PORT);
		ActorRef master = remote().actorFor(Master.class.getName(), MASTER_SERVER, MASTER_PORT);
		CalculateMessage calc = new CalculateMessage(PRIME);
		master.tell(calc);
	}

}
