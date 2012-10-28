package noir;

import static akka.actor.Actors.poisonPill;
import static akka.actor.Actors.remote;

import java.math.BigInteger;

import noir.messages.*;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.remoteinterface.RemoteServerModule;

public class Master extends UntypedActor {

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof CalculateMessage) {
			System.out.println("[N] Received CalculateMessage. - Not Implemented!");
			CalculateMessage cMessage = (CalculateMessage) message;
		}
		else if (message instanceof ResultMessage) {
			System.out.println("[N] Received ResultMessage. - Not Implemented!");
			ResultMessage rMessage = (ResultMessage) message;
		}
		else {
			throw new IllegalArgumentException("[N] Unknown message [" + message + "]");
		}
	}
	
	public static void main(String[] args) {
		System.out.println("[N] Master");
		RemoteServerModule remoteServer = remote().start("localhost", 2553);
		ActorRef master = remote().actorFor(Master.class.getName(), "localhost", 2553);
		CalculateMessage calc = new CalculateMessage("45");
		master.tell(calc);
	}

}
