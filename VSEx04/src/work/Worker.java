package work;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import threads.Message;

public class Worker extends Thread {

	private static final long FRAME_LENGTH = 1000;
	private static final long SLOT_LENGTH = 50;
	private static final int NUMBER_OF_SLOTS = (int) (FRAME_LENGTH / SLOT_LENGTH);

	private String self;

	private TimeSlot[] current;
	private TimeSlot[] future;

	private BlockingQueue<Message> incoming;
	private BlockingQueue<Message> outgoing;

	private int currentSlot;

	public Worker(BlockingQueue<Message> incoming,
			BlockingQueue<Message> outgoing, String self) {
		currentSlot = 0; // falls wir der erste sind, dann geben wir die zeit
							// vor
		this.self = self;
		this.incoming = incoming;
		this.outgoing = outgoing;
		current = new TimeSlot[NUMBER_OF_SLOTS];
		future = new TimeSlot[NUMBER_OF_SLOTS];
	}

	public void incCurrentSlot() {
		this.currentSlot = (currentSlot + 1) % NUMBER_OF_SLOTS;
	}

	@Override
	public void run() {
		Message msg = null;
		boolean synced = false;
		boolean silence = true;
		long startingTime = System.currentTimeMillis();
		// hier mit anderen auf zeit synchen
		while (!synced && !isInterrupted()) {
			try {
				msg = incoming.poll(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.out.println("[!!!] poll interrupted in during sync.");
			}
			if (msg != null) {

				silence = false;
				System.out.println("[WORKER] "+msg.toString());
				if (msg.getSender().equals(self)) {

				} else {

					TimeSlot tmp = new TimeSlot();//
					tmp.setTeam(msg.getSender());
					tmp.setSlot(msg.getNextSlot()); // damit wir beim naechsten
													// mal wissen wo wir denn
													// sind
					// long eta =
					// wenn sender schon einmal gesendet hat, dann kennen wir
					// den actualSlot
					TimeSlot found = lookForTimeSlotWithTeam(msg);

					// tmp.setEta(eta)

					if (found == null/* suche fehlgesschlagen */) {
						current[msg.getNextSlot()] = tmp;
					} else {
						// dann haben wir das Wissen um uns zu orientieren.
						synced = true;

						currentSlot = found.getSlot();

						long eta = (NUMBER_OF_SLOTS - found.getSlot() + msg
								.getNextSlot()) * SLOT_LENGTH;
						tmp.setEta(eta);

						for (int i = 0; i < tmp.getSlot(); i++) {
							future[i] = current[i];
						}

						future[msg.getNextSlot()] = tmp;
						// TODO EVtl fehler wegen kopieren von index...

					}
				}
			} else {

			}

			if (silence
					&& ((System.currentTimeMillis() - startingTime) >= 2000)) { // 2x
																				// Frame-time
				synced = true;
				// TODO eigenen Slot eintragen
				
				System.out.println("I'm alone....");
			}
		}
		System.out.println("Worker synchronized, now ...");
		
		long beginOfNextSlot = msg.getOurTimestamp() + 50;
		
		boolean receivedMessage = false;
		boolean sentMessage = false;
		// normaler run loop
		while (!isInterrupted()) {
			try {
				msg = incoming.poll(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			/*
			 * do we always receive the message in the right time slot?
			 */
			if (System.currentTimeMillis() >= beginOfNextSlot) {
				incCurrentSlot();
				beginOfNextSlot += 50;
				receivedMessage = false;
				sentMessage = false;
			}
			if (msg != null) {
				System.out.println(msg.toString());
			} else {
				// System.out.println("msg!");
			}
			if(!sentMessage && current[currentSlot] != null && current[currentSlot].getTeam().equals(self)) {
				byte nextSlot = 0;
				// TODO calculate next slot
				outgoing.add(new Message(new byte[]{0}, self, nextSlot, System.currentTimeMillis()));
				sentMessage = true;
			}
			if(sentMessage && receivedMessage) {
				// TODO resolve conflict
			}
		}
	}

	private TimeSlot lookForTimeSlotWithTeam(Message msg) {
		TimeSlot old = null;
		for (int i = 0; i < current.length; i++) {
			if (current[i] != null
					&& current[i].getTeam().equals(msg.getSender())) {
				// gefunden
				old = current[i];
				break;
			}
		}
		return old;
	}
}
