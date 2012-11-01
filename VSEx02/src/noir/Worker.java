package noir;

import static akka.actor.Actors.remote;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import noir.messages.PrimeMessage;
import noir.messages.AlreadyCalculatedMessage;
import noir.messages.CalculateMessage;
import noir.messages.FactorMessage;
import noir.messages.FinishedMessage;
import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;

public class Worker extends UntypedActor {

	final static String MASTER_SERVER = "localhost";
	final static String WORKER_SERVER = "localhost";
	final static int MASTER_PORT = 2553;
	final static int WORKER_PORT = 2552;
	
	final static boolean DEBUG = false;
	
	private static AtomicInteger idGenerator = new AtomicInteger();
	private BigInteger N;
	private ActorRef master;
	private ActorRef self;
	private int actorId;
	private long timeNeeded; 
	
	private WorkerThread thread;
	
	public Worker() {
		actorId = idGenerator.addAndGet(1);
		getContext().setId(String.valueOf(actorId));
		System.out.println("[N] ("+actorId+") Worker created");
		self = getContext();
	}

	
	public void broadcastWorkers(final Serializable message) {
		for(ActorRef actor : Actors.registry().actors()) {
			if(!actor.equals(master) && !actor.equals(self)) {
				actor.tell(message);
			}
		}
	}
	
	public void sendMaster(final Serializable message) {
		master.tell(message);
	}
	
	public void sendSelf(final Serializable message) {
		getContext().tell(message);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof CalculateMessage) {
			long time = new Date().getTime();
			if(DEBUG) System.out.println("[N] ("+actorId+") Received Calculate Message: " + message.toString());
			master = getContext().getSender().get();
			N = ((CalculateMessage) message).getN();
			if(N.isProbablePrime(10)) {
				getContext().tell(new FinishedMessage(0, 0));
				master.tell(new PrimeMessage(N));
			}
			else {
				thread = new WorkerThread(this, actorId);
				thread.start();
				thread.addWork(N);
			}
			timeNeeded += (new Date().getTime() - time);
		}
		else if (message instanceof AlreadyCalculatedMessage) {
			long time = new Date().getTime();
			if(DEBUG) System.out.println("[N] ("+actorId+") Received Already Calculate Message: " + message.toString());
			AlreadyCalculatedMessage acMessage = (AlreadyCalculatedMessage) message;
			thread.addSolved(acMessage.getFactor());
			timeNeeded += (new Date().getTime() - time);
		}
		else if (message instanceof FactorMessage) {
			long time = new Date().getTime();
			if(DEBUG) System.out.println("[N] ("+actorId+") Received FactorMassage: " + message.toString());
			FactorMessage fMessage = (FactorMessage) message;
			thread.addWork(fMessage.getFactor());
			timeNeeded += (new Date().getTime() - time);
		}
		else if (message instanceof FinishedMessage) {
			long time = new Date().getTime();
			if(DEBUG) System.out.println("[N] ("+actorId+") Received FinishMassage - Not yet implemented.");
			FinishedMessage fMessage = (FinishedMessage) message;
			if(thread != null && thread.isAlive()) {
//				thread.kill();
				thread.interrupt();
			}
			timeNeeded += (new Date().getTime() - time);
			master.tell(new FinishedMessage(fMessage.getTime()+timeNeeded, fMessage.getIterations()));
		}
		else {
			throw new IllegalArgumentException("[N] ("+actorId+") Unknown message [" + message + "]");
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("[N] Worker");
		remote().start(WORKER_SERVER, WORKER_PORT);
	}
	
}
