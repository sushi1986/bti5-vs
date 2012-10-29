package noir;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import noir.messages.CalculateMessage;
import noir.messages.FinishedMessage;
import noir.messages.MasterMessage;
import noir.messages.PrimeMessage;
import noir.messages.ResultMessage;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

public class Master extends UntypedActor {
	
	/* THIS MUST BE CHANGED IN WORKER ACCORDINGLY */
	final static String MASTER_SERVER = "localhost";
	final static String WORKER_SERVER = "localhost";
	final static int MASTER_PORT = 2553;
	final static int WORKER_PORT = 2552;
	
	final int NUMBER_OF_WORKERS = 3;
	
	List<ActorRef> workers;
	SortedSet<BigInteger> results; 
	int finishedWorkers;
	
	public Master() {
		workers = new ArrayList<ActorRef>();
		results = new TreeSet<BigInteger>();
		finishedWorkers = 0;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof CalculateMessage) {
			System.out.println("[N] (Master) Received CalculateMessage.");
			CalculateMessage cMessage = (CalculateMessage) message;
			ActorRef me = getContext();
			for(int i = 0; i < NUMBER_OF_WORKERS; ++i) {
				ActorRef worker = remote().actorFor(Worker.class.getName(), WORKER_SERVER, WORKER_PORT);
				worker.tell(cMessage, me);
				workers.add(worker);
			}
		}
		else if (message instanceof PrimeMessage) {
			System.out.println("[N] Master Received PrimeMessage.");
			PrimeMessage pMessage= (PrimeMessage) message;
			results.add(pMessage.getPrime());
		}
		else if (message instanceof ResultMessage) {
			System.out.println("[N] (Master) Received ResultMessage.");
		}
		else if (message instanceof FinishedMessage) {
			System.out.println("[N] Master Received FinishMessage.");
			FinishedMessage fMessage = (FinishedMessage) message;
			++finishedWorkers;
			if(finishedWorkers >= NUMBER_OF_WORKERS) {
				System.out.println("[N] Prime factors found:");
				for(BigInteger bi : results) {
					System.out.println("[N] > " + bi);
				}
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
		CalculateMessage calc = new CalculateMessage("9398726230209357241");
		master.tell(calc);
	}

}
