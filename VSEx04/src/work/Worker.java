package work;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import threads.Message;

public class Worker extends Thread {

	private static final long FRAME_LENGTH = 1000;
	private static final long SLOT_LENGTH = 50;
	private static final int NUMBER_OF_SLOTS = (int) (FRAME_LENGTH / SLOT_LENGTH);
	private static final boolean RANDOM = true;

	private String self;

	private TimeSlot[] current;
	private TimeSlot[] future;
	private long[] diffrences;

	private BlockingQueue<Message> incoming;
	private BlockingQueue<Message> outgoing;

	private int currentSlot;

	public Worker(BlockingQueue<Message> incoming,
			BlockingQueue<Message> outgoing, String self) {
		currentSlot = 0;
		byte[] glub = String.format("%-10s", self).getBytes();
		for (int i = self.length(); i < glub.length; i++) {
			glub[i] = 0;
		}
		this.self = new String(glub);
		this.incoming = incoming;
		this.outgoing = outgoing;
		current = new TimeSlot[NUMBER_OF_SLOTS];
		future = new TimeSlot[NUMBER_OF_SLOTS];
		diffrences = new long[NUMBER_OF_SLOTS];
	}

	private boolean insertMessageIntoFuture(Message msg) {
		byte nextSlot = msg.getNextSlot();
		if (future[nextSlot] == null) {
			System.out.printf("[WORKER][main][%2d] %s\n", currentSlot,
					msg.toString());
			TimeSlot tmp = new TimeSlot();
			tmp.setTeam(msg.getSender());
			future[nextSlot] = tmp;
			return true;
		} else {
			if (current[currentSlot] == null) {
				System.out
						.printf("[WORKER][main][%2d] Slot not available (t%s -> s%d), conflic with (t%s - s%d) [current slot null]\n",
								currentSlot, future[nextSlot].getTeam(),
								future[nextSlot].getSlot(), msg.getSender(),
								msg.getNextSlot());
				return false;
			} else {
				if (current[currentSlot].getTeam().equals(msg.getSender())) {
					System.out.printf(
							"[WORKER][main][%2d][Overwriting team: %s] %s\n",
							currentSlot, future[nextSlot].getTeam(),
							msg.toString());
					TimeSlot tmp = new TimeSlot();
					tmp.setTeam(msg.getSender());
					future[nextSlot] = tmp;
					return true;
				} else {
					System.out
							.printf("[WORKER][main][%2d] Slot not available (t%s -> s%d), conflic with (t%s - s%d) [rightful owner already saved this slot]\n",
									currentSlot, future[nextSlot].getTeam(),
									future[nextSlot].getSlot(),
									msg.getSender(), msg.getNextSlot());
					return false;
				}
			}
		}
	}

	private void insertTimeSlotIntoCurrent(byte slot, String team) {
		TimeSlot tmp = new TimeSlot();
		tmp.setSlot(slot);
		tmp.setTeam(team);
		current[slot] = tmp;
	}

	private byte findFreeSlotFromIndexIn(int start, TimeSlot[] slots,
			boolean random) {
		if (!random) {
			for (byte i = (byte) start; i < slots.length; i++) {
				if (slots[i] == null) {
					return i;
				}
			}
			return -1;
		} else {
			List<Byte> tmp = new ArrayList<Byte>();
			for (byte i = (byte) start; i < slots.length; i++) {
				if (slots[i] == null) {
					tmp.add(i);
				}
			}
			if (tmp.isEmpty()) {
				return -1;
			} else {
				return tmp.get(new Random().nextInt(tmp.size()));
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

	@Override
	public void run() {

		Message msg = null;
		boolean synced = false;
		boolean silence = true;
		long startingTime = TimeHandler.generateTimeStamp();

		boolean isFirst = false;

		while (!synced && !isInterrupted()) {
			try {
				msg = incoming.poll(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.out
						.println("[Worker][!!!] poll interrupted in during sync.");
			}
			if (msg != null) {
				System.out.printf("[Worker][sync][%2d] %s\n", currentSlot,
						msg.toString());

				silence = false;

				TimeSlot tmp = new TimeSlot();
				tmp.setTeam(msg.getSender());
				tmp.setSlot(msg.getNextSlot());

				TimeSlot found = lookForTimeSlotWithTeam(msg);
				if (found == null) {
					current[msg.getNextSlot()] = tmp;
				} else {
					synced = true;
					currentSlot = found.getSlot();
					for (int i = 0; i < tmp.getSlot(); i++) {
						future[i] = current[i];
					}
					future[msg.getNextSlot()] = tmp;
				}
			}
			if (silence
					&& ((TimeHandler.generateTimeStamp() - startingTime) >= 2000)) {
				synced = true;
				isFirst = true;
			}
		}

		long beginOfNextSlot = 0;
		beginOfNextSlot = (TimeHandler.generateTimeStamp() / 1000) * 1000 + 1000;
		try {
			Thread.sleep(beginOfNextSlot-TimeHandler.generateTimeStamp());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isFirst) {
			System.out.println("[Worker] I'm alone....");
		} else {
			System.out.println("[Worker] Worker synchronized, now ...");
		}

		boolean sending = false;
		boolean sentMessage = false;
		// boolean receivedMessage = false;
		List<Message> receivedMessages = new ArrayList<Message>();
		while (!isInterrupted()) {
			try {
				msg = incoming.poll(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (TimeHandler.generateTimeStamp() >= beginOfNextSlot) {
				if (receivedMessages.size() > 1) {
					for (Message m : receivedMessages) {
						if (m.getSender().equals(self)) {
							sending = false;
						}
					}
					System.out.printf("[Worker][main][%2d] Collision!\n",
							currentSlot);
				} else if (receivedMessages.size() == 1) {
					insertMessageIntoFuture(receivedMessages.get(0));
				}
				receivedMessages.clear();
				currentSlot = (currentSlot + 1) % NUMBER_OF_SLOTS;
				if (currentSlot == 0) {
					current = future;
					future = new TimeSlot[NUMBER_OF_SLOTS];
					int cnt = 0;
					int average = 0;
					for (int i = 0; i < diffrences.length; i++) {
						if (diffrences[i] != 0) {
							average += diffrences[i];
							cnt++;
							diffrences[i] = 0;
						}
					}
					if (cnt > 0) {
						// beginOfNextSlot -= (average / cnt);
						TimeHandler.adjustTime(-(average / cnt));
					}
					sentMessage = false;
				}
				beginOfNextSlot += 50;
				// receivedMessage = false;
				if (!sending) {
					byte nextSlot = findFreeSlotFromIndexIn(currentSlot + 1,
							current, RANDOM);
					if (nextSlot >= 0) {
						insertTimeSlotIntoCurrent(nextSlot, self);
						sending = true;
					}
				}
			}

			TimeSlot now = current[currentSlot];
			if (msg == null) { // no message received, maybe i need to send
				if (now != null) {
					if (now.getTeam().equals(self)) { // i have to send
						if (!sentMessage
								&& TimeHandler.generateTimeStamp() >= (beginOfNextSlot - 30)) {
							byte nextSlot = findFreeSlotFromIndexIn(0, future,
									RANDOM);
							if (nextSlot == -1) {
								sending = false;
								System.err
										.println("[Worker] No free slot available.");
							} else {
								Message tmp = null;
								BufferedInputStream bis = new BufferedInputStream(
										System.in);
								byte[] input = new byte[24];

								try {
									if (bis.read(input) != 24) {
										System.out
												.println("Fehler mit der DatenQuelle");
									} else {
										tmp = new Message(input, nextSlot,
												TimeHandler.generateTimeStamp());
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
								if (tmp != null) {
									outgoing.add(tmp);
									sentMessage = true;
									// receivedMessages.add(tmp);
								} else {
									sending = false;
								}
								// if (tmp != null) {
								// if (insertMessageIntoFuture(tmp)) {
								// outgoing.add(tmp);
								// sentMessage = true;
								// } else {
								// sending = false;
								// System.out
								// .println("[Worker] Liar. That slot is not free ...");
								// }
								// }
							}
						}
					}
				}
			} else { // received message
				receivedMessages.add(msg);
				if (msg.getSender().equals(self)) {
					diffrences[currentSlot] = msg.getOurTimestamp()
							- msg.getTimeStamp();
				}
				// if (msg.getSender().equals(self)) { // received own message
				// // who cares?
				// } else { // received message from other team
				// if (now == null) { // they are sending the first time
				// insertMessageIntoFuture(msg);
				// } else { // expected someone to send something
				// if (msg.getSender().equals(now.getTeam())) {
				// insertMessageIntoFuture(msg);
				// } else {
				// insertMessageIntoFuture(msg);
				// // someone send in the wrong slot
				// }
				// }
				// diffrences[currentSlot] = msg.getOurTimestamp()
				// - msg.getTimeStamp();
				// }
			}
		}
		System.out.println("[Worker] Bye!");
	}
}
