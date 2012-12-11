package work;

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
    private static final boolean RANDOM = false;

    private String self;

    private TimeSlot[] current;
    private TimeSlot[] future;
    private long[] diffrences;

    private BlockingQueue<Message> incoming;
    private BlockingQueue<Message> outgoing;

    private int currentSlot;


    
    public Worker(BlockingQueue<Message> incoming, BlockingQueue<Message> outgoing, String self) {
        currentSlot = 0;
        this.self = self;
        this.incoming = incoming;
        this.outgoing = outgoing;
        current = new TimeSlot[NUMBER_OF_SLOTS];
        future = new TimeSlot[NUMBER_OF_SLOTS];
        diffrences = new long[NUMBER_OF_SLOTS];
    }

    private boolean insertMessageIntoSlot(Message msg, TimeSlot[] into) {
        if (into[msg.getNextSlot()] != null) {
            System.out.printf("[WORKER][main][%2d] Slot not available (t%s -> s%d), conflic with (t%s - s%d)\n", currentSlot, into[msg.getNextSlot()].getTeam(), into[msg.getNextSlot()].getSlot(), msg.getSender(), msg.getNextSlot());
            return false;
        }
        else {
            // System.out.println("[WORKER][main] Slot is '" + currentSlot +
            // "' > " + msg.toString());
            System.out.printf("[WORKER][main][%2d] %s\n", currentSlot, msg.toString());
            TimeSlot tmp = new TimeSlot();
            tmp.setTeam(msg.getSender());
            into[msg.getNextSlot()] = tmp;
            return true;
        }
    }

    private void insertTimeSlot(TimeSlot[] into, byte slot, long eta, String team) {
        TimeSlot tmp = new TimeSlot();
        tmp.setEta(eta);
        tmp.setSlot(slot);
        tmp.setTeam(team);
        into[slot] = tmp;
    }

    private byte findFreeSlotFromIndexIn(int start, TimeSlot[] slots, boolean random) {
        if (!random) {
            for (byte i = (byte) start; i < slots.length; i++) {
                if (slots[i] == null) {
                    return i;
                }
            }
            return -1;
        }
        else {
            List<Byte> tmp = new ArrayList<Byte>();
            for (byte i = (byte) start; i < slots.length; i++) {
                if (slots[i] == null) {
                    tmp.add(i);
                }
            }
            if (tmp.isEmpty()) {
                return -1;
            }
            else {
                return tmp.get(new Random().nextInt(tmp.size()));
            }
        }
    }

    private TimeSlot lookForTimeSlotWithTeam(Message msg) {
        TimeSlot old = null;
        for (int i = 0; i < current.length; i++) {
            if (current[i] != null && current[i].getTeam().equals(msg.getSender())) {
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
        long startingTime = Message.generateTimeStamp();

        boolean isFirst = false;

        while (!synced && !isInterrupted()) {
            try {
                msg = incoming.poll(10, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                System.out.println("[Worker][!!!] poll interrupted in during sync.");
            }
            if (msg != null) {
                System.out.printf("[Worker][sync][%2d] %s\n", currentSlot, msg.toString());
                
                silence = false;

                TimeSlot tmp = new TimeSlot();
                tmp.setTeam(msg.getSender());
                tmp.setSlot(msg.getNextSlot());

                TimeSlot found = lookForTimeSlotWithTeam(msg);
                if (found == null) {
                    current[msg.getNextSlot()] = tmp;
                }
                else {
                    synced = true;
                    currentSlot = found.getSlot();
                    for (int i = 0; i < tmp.getSlot(); i++) {
                        future[i] = current[i];
                    }
                    future[msg.getNextSlot()] = tmp;
                }
            }
            if (silence && ((Message.generateTimeStamp() - startingTime) >= 2000)) {
                synced = true;
                isFirst = true;
            }
        }

        long beginOfNextSlot = 0;

        if (isFirst) {
            beginOfNextSlot = Message.generateTimeStamp();
            System.out.println("[Worker] I'm alone....");
        }
        else {
            beginOfNextSlot = msg.getOurTimestamp() + 25;
            System.out.println("[Worker] Worker synchronized, now ...");
        }

        boolean sending = false;
        boolean sentMessage = false;
        // boolean receivedMessage = false;
        while (!isInterrupted()) {
            try {
                msg = incoming.poll(10, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (Message.generateTimeStamp() >= beginOfNextSlot) {
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
                        beginOfNextSlot -= (average / cnt);
                    	Message.adjustTime(-(average / cnt));
                    }
                }
                beginOfNextSlot += 50;
                // receivedMessage = false;
                sentMessage = false;
                if (!sending) {
                    byte nextSlot = findFreeSlotFromIndexIn(currentSlot + 1, current, RANDOM);
                    if (nextSlot >= 0) {
//                        insertTimeSlot(future, nextSlot, (nextSlot * SLOT_LENGTH + (beginOfNextSlot - 50)), self);
                        insertTimeSlot(current, nextSlot, (nextSlot * SLOT_LENGTH + (beginOfNextSlot - 50)), self);

                    	sending = true;
                    }
                }
            }

            TimeSlot now = current[currentSlot];
            if (msg == null) { // no message received, maybe i need to send
                if (now != null) {
                    if (now.getTeam().startsWith(self)) { // i have to send
                        if (!sentMessage && Message.generateTimeStamp() >= (beginOfNextSlot - 30)) {
                            byte nextSlot = findFreeSlotFromIndexIn(0, future, RANDOM);
                            if (nextSlot == -1) {
                                sending = false;
                                System.err.println("[Worker] No free slot available.");
                            }
                            else {
                                Message tmp = new Message(new byte[] { 0 }, self, nextSlot, Message.generateTimeStamp());
                                if (insertMessageIntoSlot(tmp, future)) {
                                    outgoing.add(tmp);
                                    sentMessage = true;
                                }
                                else {
                                    sending = false;
                                    System.out.println("[Worker] Liar. That slot is not free ...");
                                }
                            }
                        }
                    }
                }
            }
            else { // received message
                if (msg.getSender().startsWith(self)) { // received own message
                    // who cares?
                }
                else { // received message from other team
                    if (now == null) { // they are sending the first time
                        insertMessageIntoSlot(msg, future);
                    }
                    else { // expected someone to send something
                        if (msg.getSender().startsWith(now.getTeam())) {
                            insertMessageIntoSlot(msg, future);
                        }
                        else {
                            // someone send in the wrong slot
                        }
                    }
                    diffrences[currentSlot] = msg.getOurTimestamp() - msg.getTimeStamp();
                }
            }
        }
        System.out.println("[Worker] Bye!");
    }
}
