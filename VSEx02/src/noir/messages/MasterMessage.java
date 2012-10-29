package noir.messages;

import java.io.Serializable;

import akka.actor.ActorRef;

public class MasterMessage implements Serializable {
	private static final long serialVersionUID = 334088183548128478L;
	private ActorRef master;
	
	public MasterMessage(final ActorRef master) {
		this.master = master;
	}
	
	public ActorRef getMaster() {
		return master;
	}

	@Override
	public String toString() {
		return "[<MasterMessage> master=" + master + "]";
	}

	
}
