package noir;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;

import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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

	static String PRIME = "1000602106143806596478722974273666950903906112131794745457338659266842446985022076792112309173975243506969710503";
	// "1000602106143806596478722974273666950903906112131794745457338659266842446985022076792112309173975243506969710503";
																																				// //"1137047281562824484226171575219374004320812483047";
	static boolean HAS_GUI = false;

	final int NUMBER_OF_LOCAL_WORKERS = 8;

	final static boolean DEBUG = true;

	Map<Long, WorkerData> workerData;
	SortedSet<BigInteger> results;
	int unfinishedWorkers;

	long time;
	long timeNeeded;

	public Master() {
		workerData = new TreeMap<Long, WorkerData>();
		results = new TreeSet<BigInteger>();
		unfinishedWorkers = NUMBER_OF_LOCAL_WORKERS;
		time = 0;
		timeNeeded = 0;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		// time = new Date().getTime();
		if (message instanceof CalculateMessage) {
			time = new Date().getTime();
			System.out.println("[N] (Master) Received CalculateMessage."
					+ message.toString());
			ActorRef me = getContext();
			Actors.registry().register(me);
			CalculateMessage cMessage = (CalculateMessage) message;
			for (int i = 0; i < NUMBER_OF_LOCAL_WORKERS; ++i) {
				ActorRef worker = remote().actorFor(Worker.class.getName(),
						"localhost", WORKER_PORT);
				worker.tell(cMessage, me);
			}
			workerData = new TreeMap<Long, WorkerData>();
			results = new TreeSet<BigInteger>();
			unfinishedWorkers = NUMBER_OF_LOCAL_WORKERS;
			timeNeeded = 0;
		} else if (message instanceof PrimeMessage) {
			System.out.println("[N] Master Received PrimeMessage."
					+ message.toString());
			PrimeMessage pMessage = (PrimeMessage) message;
			results.add(pMessage.getPrime());

			if (HAS_GUI) {
				GUI.gui.getTextArea().setText("");
				for (BigInteger s : results) {
					addTextToView(s.toString());
				}
			}
		} else if (message instanceof FinishedMessage) {
			System.out.println("[N] Master Received FinishMessage."
					+ message.toString());
			FinishedMessage fMessage = (FinishedMessage) message;
			workerData.put(fMessage.getId(), new WorkerData(fMessage.getTime(),
					fMessage.getIterations()));
			// System.out.println("[N] Worker finished in " + fMessage.getTime()
			// + " with " + fMessage.getIterations() + " iterations");
			unfinishedWorkers--;
			if (unfinishedWorkers == 0) {
				System.out.println("[N] ########## RESULTS ##########");
				addTextToView("[N] ########## RESULTS ##########");

				timeNeeded += new Date().getTime() - time;

				System.out.println("[N] " + results.size()
						+ " primefactors for " + PRIME + " found:");
				addTextToView(results.size() + " primefactors for "
						+ PRIME + " found:");

				for (Iterator<BigInteger> itr = results.iterator(); itr
						.hasNext();) {
					BigInteger prime = (BigInteger) itr.next();
					
					System.out.println("[N] > " + prime);
					addTextToView(" > " + prime);
				
				}
				
				System.out.println("[N] Master took " + timeNeeded + " msec");
				addTextToView("Master took " + timeNeeded + " msec");
				
				
				for (Long key : workerData.keySet()) {
					
				

						WorkerData tmp = workerData.get(key);
						
						System.out.println("[N] Worker " + key + " took "
								+ tmp.getTime() + " msec and "
								+ tmp.getIterations() + " iterations");
						
						addTextToView("Worker " + key + " took "
								+ tmp.getTime() + " msec and "
								+ tmp.getIterations() + " iterations");
					
				}
				getContext().tell(poisonPill());
				System.out.println("[N] ############ END ############");
				addTextToView("############ END ############");
			}
		} else {
			throw new IllegalArgumentException("[N] (Master) Unknown message ["
					+ message + "]");
		}
		// timeNeeded += new Date().getTime() - time;
	}

	private void addTextToView(String s) {
		if(HAS_GUI) {
			GUI.gui.getTextArea().setText(
					GUI.gui.getTextArea().getText() + "\n" + s);
		}
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			PRIME = args[0];
			HAS_GUI = true;
		}
		System.out.println("[N] Master");
		RemoteServerModule remoteServer = remote().start(MASTER_SERVER,
				MASTER_PORT);
		ActorRef master = remote().actorFor(Master.class.getName(),
				MASTER_SERVER, MASTER_PORT);
		CalculateMessage calc = new CalculateMessage(PRIME);
		master.tell(calc);
	}

}
