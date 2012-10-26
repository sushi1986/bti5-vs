package de.haw.inet.vs.lab2;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

import java.util.concurrent.atomic.AtomicInteger;

public class Worker extends UntypedActor {

	private static AtomicInteger idGenerator = new AtomicInteger();

	public Worker() {
		// Wichtig: Wenn die ID nicht gesetzt wird, wird immer dieselbe In- //
		// stanz des Aktors r alle Remote-Aufrufe eines Clients verwendet!
		int actorId = idGenerator.addAndGet(1); // get next free actor ID
		getContext().setId(String.valueOf(actorId));
		System.out.println("Aktor wurde erstellt: " + actorId);
	}

	private int calculate(int a, int b) {
		return a + b;
	}

	// message handler
	public void onReceive(Object message) {
		if (message instanceof CalculateMessage) {
			// Beim ersten Aufruf wird der Sender ermittel
			CalculateMessage calculateMessage = (CalculateMessage) message;
			int result = calculate(calculateMessage.getA(),
					calculateMessage.getB());
			// Mit einer Ergebnis-Nachricht antworten
			// replyUnsafe() wirft eine Exception bei ungltigem Absender
			//getContext().replyUnsafe(new ResultMessage(result));
			getContext().reply(new ResultMessage(result));
			// Durch getContext().tell([Nachricht]) kann der Aktor
			// sich selbst eine Nachricht schicken. In diesem Fall schickt
			// sich der Aktor eine "poisonPill". Empfngt ein Aktor diese, //
			// beendet er sich und postStop() wird aufgerufen.
			getContext().tell(poisonPill());
		} else {
			throw new IllegalArgumentException("Unknown message [" + message
					+ "]");
		}
	}

	@Override
	public void postStop() {
		System.out.println("Aktor wurde beendet: " + getContext().getId());
		super.postStop();
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Worker");
		remote().start("localhost", 2552);
	}
}
