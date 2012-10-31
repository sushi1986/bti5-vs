package de.haw.inet.vs.lab2;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;

import java.math.BigInteger;
import java.util.Date;

import phillip.CalcMessage;
import phillip.WorkerSendingEverything;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

public class Master extends UntypedActor {
	long time;
	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof CalculateMessage) {
			CalculateMessage calculate = (CalculateMessage) message;
			// Worker auf dem Remote-Host erstellen
			ActorRef worker = remote().actorFor(Worker.class.getName(),
					"localhost", 2552);
			// getContext() gibt eine Referenz auf diesen Aktor zurück
			ActorRef me = getContext();
			// tell verschickt eine Message an einen Aktor. Zusätzlich kann
			// man eine Referenz auf einen anderen Aktor übergeben
			worker.tell(calculate, me);
		} else if (message instanceof ResultMessage) {
			System.out.println((new Date().getTime()-time) + "<- gesamt time: "+time);
			System.out.println(((ResultMessage) message).getResult());
			getContext().tell(poisonPill());
		} else if (message instanceof phillip.CalcMessage) {
			CalcMessage msg = (CalcMessage) message;
			System.out.println("got messsage: N: " + msg.getN());

			// ActorRef worker =
			// remote().actorFor(WorkerSendingEverything.class.getName(),
			// "localhost", 2552);
			ActorRef me = getContext();
			// tell verschickt eine Message an einen Aktor. Zusätzlich kann
			// man eine Referenz auf einen anderen Aktor übergeben
			time = new Date().getTime();
			worker.tell(new CalcMessage(((CalcMessage) message).getN()), me);
			worker2.tell(new CalcMessage(((CalcMessage) message).getN()), me);
			worker3.tell(new CalcMessage(((CalcMessage) message).getN()), me);
			System.out.println("Started timer. Sent all");
		} else {
			throw new IllegalArgumentException("Unknown message [" + message
					+ "]");
		}
	}

	static ActorRef worker;
	static ActorRef worker2;
	static ActorRef worker3;

	public static void main(String[] args) {

		System.out.println("Master");
		// Der "Client" muss auch als Remote-Aktor gestartet werden um
		// später Nachrichten vom Server empfangen zu können.
		RemoteServerModule remoteSupport = remote().start("localhost", 2553);
		ActorRef client = remote().actorFor(Master.class.getName(),
				"localhost", 2553);
		CalcMessage calculate = new CalcMessage(new BigInteger("1000602106143806596478722974273666950903906112131794745457338659266842446985022076792112309173975243506969710503"));
		client.tell(calculate);
		worker = remote().actorFor(WorkerSendingEverything.class.getName(),
				"localhost", 2552);		
		worker2 = remote().actorFor(WorkerSendingEverything.class.getName(),
						"localhost", 2552);
		worker3 = remote().actorFor(WorkerSendingEverything.class.getName(),
				"localhost", 2552);

	}
}
