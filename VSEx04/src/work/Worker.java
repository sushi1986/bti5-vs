package work;

import java.security.acl.LastOwnerException;
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

	TimeSlot pastSlot = null;
	TimeSlot futureSlot = null;

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
			pastSlot = current[NUMBER_OF_SLOTS - 1];
			current = future;
			futureSlot = current[1];
			future = new TimeSlot[NUMBER_OF_SLOTS];
		} else {
			pastSlot = current[currentSlot - 1];
			futureSlot = (currentSlot == NUMBER_OF_SLOTS - 1) ? future[0]
					: current[currentSlot + 1];
		}
	}

	private boolean insertMessageIntoSlot(Message msg, TimeSlot[] into) {
		if (into[msg.getNextSlot()] != null) {
			System.out.println("[WORKER]" + "Slot is already in use: "
					+ into[msg.getNextSlot()].toString());
			return false;
		} else {
			System.out.println("[WORKER]" + "Slot is: " + currentSlot + ": "
					+ msg.toString());
			TimeSlot tmp = new TimeSlot();
			tmp.setTeam(msg.getSender());
			into[msg.getNextSlot()] = tmp;
			return true;
		}
	}

	private boolean insertMessageIntoSlot(Message msg, TimeSlot[] into, long eta) {
		if (!insertMessageIntoSlot(msg, into)) {
			return false;
		}
		into[msg.getNextSlot()].setEta(eta);
		return true;
	}

	private void insertTimeSlot(TimeSlot[] into, byte slot, long eta,
			String team) {
		TimeSlot tmp = new TimeSlot();
		tmp.setEta(eta);
		tmp.setSlot(slot);
		tmp.setTeam(team);
		into[slot] = tmp;
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
					byte nextSlot = findFreeSlotFrom(currentSlot + 1, current);
					if (nextSlot >= 0) {
						insertTimeSlot(future, nextSlot, (nextSlot
								* SLOT_LENGTH + (beginOfNextSlot - 50)), self);
						sending = true;
					}
				}
			}

			// expecting noboady specific
			if (current[currentSlot] == null) {
				if (msg == null) {
					// continue
				} else {
					// TODO: expected last slot?
					if (pastSlot != null
							&& msg.getSender().startsWith(pastSlot.getTeam())) {
						long difference = msg.getOurTimestamp()
								- pastSlot.getEta();
						beginOfNextSlot += (difference / 2);
					} else if (futureSlot != null
							&& msg.getSender().startsWith(futureSlot.getTeam())) {
						long difference = futureSlot.getEta()
								- msg.getOurTimestamp();
						beginOfNextSlot -= (difference / 2);
					} else {
						long eta = (FRAME_LENGTH - currentSlot
								* SLOT_LENGTH + msg.getNextSlot() * SLOT_LENGTH
								+ beginOfNextSlot - 50);
						insertMessageIntoSlot(msg, future, eta);
					}
				}
			}

			// expecting self / send something
			else if (current[currentSlot].getTeam().startsWith(self)) {
				if (msg == null) {
					// TODO: did someone else already send something?
					if (sentMessage == false) { // send msg ...
						byte nextSlot = findFreeSlotFrom(0, future);
						if (nextSlot == -1) {
							sending = false;
							System.err
									.println("[Worker] No free slot available.");
						} else {
							Message tmp = new Message(new byte[] { 0 }, self,
									nextSlot, System.currentTimeMillis());
							long eta = (FRAME_LENGTH - currentSlot
									* SLOT_LENGTH + nextSlot * SLOT_LENGTH
									+ beginOfNextSlot - 50);
							if (insertMessageIntoSlot(tmp, future, eta)) {
								outgoing.add(tmp);
								sentMessage = true;
							} else {
								System.out
										.println("[Worker] Liar. That slot is not free ...");
							}
						}
					}
				} else {
					if (msg.getSender().startsWith(self)) {
						// expected
					} else { // other
								// TODO: did i already send something
						// sync to stuff
					}
				}
			}

			// expecting specific team
			else { // other
				if (msg == null) {

				} else {
					if (msg.getSender().startsWith(self)) {
						// conflict
					} else { // other
						long eta = (FRAME_LENGTH - currentSlot
								* SLOT_LENGTH + msg.getNextSlot() * SLOT_LENGTH
								+ beginOfNextSlot - 50);
						insertMessageIntoSlot(msg, future, eta);
						// adhust timing
					}
				}
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
