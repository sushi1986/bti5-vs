package noir;

import static akka.actor.Actors.remote;

import java.math.BigInteger;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import noir.messages.BroadcastMessage;
import noir.messages.CalculateMessage;
import noir.messages.FinishedMessage;
import noir.messages.PrimeMessage;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

public class Master extends UntypedActor {
	
	/* THIS MUST BE CHANGED IN WORKER ACCORDINGLY */
	final static String MASTER_SERVER = "localhost";//"141.22.90.60";
	final static String WORKER_SERVER = "localhost";//"141.22.85.85";
	final static int MASTER_PORT = 2553;
	final static int WORKER_PORT = 2552;
	
	final static String PRIME = "45";
	
	final int NUMBER_OF_WORKERS = 2;
	
	final static boolean DEBUG = true;
	
	Set<ActorRef> workers;
	SortedSet<BigInteger> results; 
	int finishedWorkers;
	long time;
	
	public Master() {
		workers = new TreeSet<ActorRef>();
		results = new TreeSet<BigInteger>();
		finishedWorkers = 0;
		time = 0;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof CalculateMessage) {
			if(DEBUG) System.out.println("[N] (Master) Received CalculateMessage." + message.toString());
			ActorRef me = getContext();
			CalculateMessage cMessage = (CalculateMessage) message;
			for(int i = 0; i < NUMBER_OF_WORKERS; ++i) {
				ActorRef worker = remote().actorFor(Worker.class.getName(), "localhost", WORKER_PORT);
				worker.tell(cMessage, me);
				workers.add(worker);
			}
		}
		else if (message instanceof BroadcastMessage) {
			BroadcastMessage bMessage = (BroadcastMessage) message;
			for(ActorRef worker : workers) {
				worker.tell(bMessage.getMessage());
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
