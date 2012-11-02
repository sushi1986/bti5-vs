package noir;

import static akka.actor.Actors.remote;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.eaio.uuid.UUID;

import noir.messages.BroadcastMessage;
import noir.messages.CalculateMessage;
import noir.messages.FinishedMessage;
import noir.messages.PrimeMessage;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

public class Master extends UntypedActor {

	/* THIS MUST BE CHANGED IN WORKER ACCORDINGLY */
	final static String MASTER_SERVER = "localhost";
	final static String WORKER_SERVER = "localhost";
	final static int MASTER_PORT = 2553;
	final static int WORKER_PORT = 2552;

	final static String PRIME = "1137047281562824484226171575219374004320812483047";

	final int NUMBER_OF_WORKERS = 2;

	final static boolean DEBUG = true;

	Map<UUID, ActorRef> map;
	Set<ActorRef> workers;
	SortedSet<BigInteger> results;
	int finishedWorkers;
	long time;

	public Master() {
		map = new TreeMap<UUID, ActorRef>();
		workers = new TreeSet<ActorRef>();
		results = new TreeSet<BigInteger>();
		finishedWorkers = 0;
		time = 0;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof CalculateMessage) {
			System.out.println("[N] (Master) Received CalculateMessage."
					+ message.toString());
			ActorRef me = getContext();
			CalculateMessage cMessage = (CalculateMessage) message;
			for (int i = 0; i < NUMBER_OF_WORKERS; ++i) {
				ActorRef worker = remote().actorFor(Worker.class.getName(),
						"localhost", WORKER_PORT);
				worker.tell(cMessage, me);
				workers.add(worker);
				map.put(worker.getUuid(), worker);
//				System.out.println("[N] CREATE WORKER UUID: " + worker.getUuid());
//				System.out.println("[N] CREATE WORKER ID  : " + worker.getId());
//				System.out.println("[N] CREATE WORKER HASH: " + worker.hashCode());
			}
//			for (int i = 0; i < 2; ++i) {
//				ActorRef worker = remote().actorFor(Worker.class.getName(),
//						"141.22.95.19", WORKER_PORT);
//				worker.tell(cMessage, me);
//				workers.add(worker);
//				map.put(worker.getUuid(), worker);
//			}
		} else if (message instanceof BroadcastMessage) {
			BroadcastMessage bMessage = (BroadcastMessage) message;
			ActorRef sender = getContext().getSender().get();
//			System.out.println("[N] RECEIVED bMESSAGE FROM UUID: " + sender.getUuid());
//			System.out.println("[N] RECEIVED bMESSAGE FROM ID  : " + sender.getId());
//			System.out.println("[N] RECEIVED bMESSAGE FROM HASH: " + sender.hashCode());
			for (ActorRef worker : workers) {
				if(worker.compareTo(sender) != 0) worker.tell(bMessage.getMessage());
				else System.out.println("Skipping sender ...");
			}
		} else if (message instanceof PrimeMessage) {
			System.out.println("[N] Master Received PrimeMessage."
					+ message.toString());
			PrimeMessage pMessage = (PrimeMessage) message;
			results.add(pMessage.getPrime());
		} else if (message instanceof FinishedMessage) {
			System.out.println("[N] Master Received FinishMessage."
					+ message.toString());
			FinishedMessage fMessage = (FinishedMessage) message;
			System.out.println("[N] Master removeing worker with id: " + fMessage.getTime());
			ActorRef sender = getContext().getSender().get();
			if(map.containsKey(sender.getUuid()))
				workers.remove(map.get(sender.getUuid()));
		} else {
			throw new IllegalArgumentException("[N] (Master) Unknown message ["
					+ message + "]");
		}
	}

	public static void main(String[] args) {
		System.out.println("[N] Master");
		RemoteServerModule remoteServer = remote().start(MASTER_SERVER,
				MASTER_PORT);
		ActorRef master = remote().actorFor(Master.class.getName(),
				MASTER_SERVER, MASTER_PORT);
		CalculateMessage calc = new CalculateMessage(PRIME);
		master.tell(calc);
	}

}
