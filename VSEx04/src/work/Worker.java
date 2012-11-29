package work;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import threads.Message;

public class Worker extends Thread {

	private static final int NUMBER_OF_SLOTS = 20;
	private static final long FRAME_LENGTH = 1000;
	private static final long SLOT_LENGTH = 50;
	
	private TimeSlot[] current;
	private TimeSlot[] future;
	
	private BlockingQueue<Message> incoming;
	private BlockingQueue<Message> outgoing;
	
	public Worker(BlockingQueue<Message> incoming, BlockingQueue<Message> outgoing) {
		this.incoming = incoming;
		this.incoming = outgoing;
		current = new TimeSlot[NUMBER_OF_SLOTS];
		future = new TimeSlot[NUMBER_OF_SLOTS];
	}
	
	@Override
	public void run() {
		Message msg = null;
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
