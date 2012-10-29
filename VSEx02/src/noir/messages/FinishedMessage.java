package noir.messages;

import java.io.Serializable;

public class FinishedMessage implements Serializable{
	private static final long serialVersionUID = -5636711540813993175L;
	
	public FinishedMessage() {
		
	}

	@Override
	public String toString() {
		return "[<FinishedMessage>]";
	}

}
