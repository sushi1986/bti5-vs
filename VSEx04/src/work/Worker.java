package work;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;

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
		currentSlot = 0;
		this.self = self;
		this.incoming = incoming;
		this.outgoing = outgoing;
		current = new TimeSlot[NUMBER_OF_SLOTS];
		future = new TimeSlot[NUMBER_OF_SLOTS];
	}

	public void incCurrentSlot() {
		this.currentSlot = (currentSlot + 1) % NUMBER_OF_SLOTS;
		if (currentSlot == 0) {
			current = future;
			future = new TimeSlot[NUMBER_OF_SLOTS];
		}
	}

	@Override
	public void run() {

		Message msg = null;
		boolean synced = false;
		boolean silence = true;
		long startingTime = System.currentTimeMillis();

		while (!synced && !isInterrupted()) {
			try {
				msg = incoming.poll(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.out.println("[!!!] poll interrupted in during sync.");
			}
			if (msg != null) {

				silence = false;
				System.out.println("[WORKER][sync]" + "Slot is: " + currentSlot
						+ "; " + msg.toString());

				TimeSlot tmp = new TimeSlot();
				tmp.setTeam(msg.getSender());
				tmp.setSlot(msg.getNextSlot());
				
				TimeSlot found = lookForTimeSlotWithTeam(msg);
				if (found == null) {
					current[msg.getNextSlot()] = tmp;
				} else {
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

			if (silence
					&& ((System.currentTimeMillis() - startingTime) >= 2000)) {
				synced = true;
				// TODO eigenen Slot eintragen
				System.out.println("I'm alone....");
				// TODO be the first to send
			}
		}
		System.out.println("Worker synchronized, now ...");

		long beginOfNextSlot = msg.getOurTimestamp() + 50;

		boolean sending = false;
		boolean sentMessage = false;
		boolean receivedMessage = false;
		while (!isInterrupted()) {
			try {
				msg = incoming.poll(10, TimeUnit.MILLISECONDS);
				if (msg != null && msg.getSender().startsWith(self)) {
					System.out.println("[WORKER][OWN_MESSAGE] " + currentSlot
							+ " -> " + msg.toString());
					msg = null;
				}
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
				if (!sending) {
					// find send slot
					byte slot = findFreeSlotFrom(currentSlot + 1, current);
					if (slot >= 0) {
						TimeSlot tmp = new TimeSlot();
						tmp.setEta(slot * SLOT_LENGTH + (beginOfNextSlot - 50));
						tmp.setSlot(slot);
						tmp.setTeam(self);
						current[slot] = tmp;
						sending = true;
					}
				}
			}
			if (msg != null) {
				System.out.println("[WORKER]" + "Slot is: " + currentSlot
						+ "; " + msg.toString());
				future[msg.getNextSlot()] = new TimeSlot();
				future[msg.getNextSlot()].setTeam(msg.getSender());
			} else {
				// System.out.println("msg!");
			}
			if (!sentMessage && current[currentSlot] != null
					&& current[currentSlot].getTeam().startsWith(self)) {
				byte nextSlot = findFreeSlotFrom(0, future);
				if (nextSlot == -1) {
					System.err.println("NO FREE SLOT AVAILABLE");
					sending = false;
				} else {
					outgoing.add(new Message(new byte[] { 0 }, self, nextSlot,
							System.currentTimeMillis()));
					sentMessage = true;
					future[nextSlot] = new TimeSlot();
					future[nextSlot].setEta(FRAME_LENGTH - currentSlot
							* SLOT_LENGTH + nextSlot * SLOT_LENGTH
							+ beginOfNextSlot - 50);
					future[nextSlot].setSlot(nextSlot);
					future[nextSlot].setTeam(self);
				}
			}
			if (sentMessage && receivedMessage) {
				// TODO resolve conflict
			}
		}
	}

	private byte findFreeSlotFrom(int start, TimeSlot[] slots) {
		for (byte i = 0; i < slots.length; i++) {
			if (slots[i] == null) {
				return i;
			}
		}
		return -1;
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
