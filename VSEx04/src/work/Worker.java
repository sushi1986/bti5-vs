package work;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import threads.Message;

public class Worker extends Thread {

	private static final int NUMBER_OF_SLOTS = 20;
	private static final long FRAME_LENGTH = 1000;
	private static final long SLOT_LENGTH = 50;
	
	private String self;
	
	private TimeSlot[] current;
	private TimeSlot[] future;
	
	private BlockingQueue<Message> incoming;
	private BlockingQueue<Message> outgoing;
	
	public Worker(BlockingQueue<Message> incoming, BlockingQueue<Message> outgoing, String self) {
		this.self = self;
		this.incoming = incoming;
		this.incoming = outgoing;
		current = new TimeSlot[NUMBER_OF_SLOTS];
		future = new TimeSlot[NUMBER_OF_SLOTS];
		for(int i = 0; i < NUMBER_OF_SLOTS; i++) {
			current[i] = new TimeSlot();
			future[i] = new TimeSlot();
		}
	}
	
	@Override
	public void run() {
		Message msg = null;
		boolean synced = false;
		while(!synced && !isInterrupted()) {
			try {
				msg = incoming.poll(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.out.println("[!!!] poll interrupted in during sync.");
			}
			if(msg != null) {
				System.out.println(msg.toString());
				if(msg.getSender().equals(self)) {
					
				}
			} else {
				
			}
		}
		while (!isInterrupted()) {
			try {
				msg = incoming.poll(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(msg != null) {
				System.out.println(msg.toString());
			} else {
//				System.out.println("msg!");
			}
		
			
		}
	}
}
