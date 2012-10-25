package de.haw.inet.vs.lab2;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

public class Master extends UntypedActor {
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
			System.out.println(((ResultMessage) message).getResult());
			getContext().tell(poisonPill());
		} else {
			throw new IllegalArgumentException("Unknown message [" + message
					+ "]");
		}
	}

	public static void main(String[] args) {
		System.out.println("Master");
		// Der "Client" muss auch als Remote-Aktor gestartet werden um
		// später Nachrichten vom Server empfangen zu können.
		RemoteServerModule remoteSupport = remote().start("localhost", 2553);
		ActorRef client = remote().actorFor(Master.class.getName(),
				"localhost", 2553);
		CalculateMessage calculate = new CalculateMessage(10, 10);
		client.tell(calculate);
	}
}