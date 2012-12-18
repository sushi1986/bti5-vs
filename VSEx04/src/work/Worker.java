package work;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import threads.Message;

/**
 * VS Lab4
 * @author Phillip Gesien, Raphael Hiesgen
 */

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

    public Worker(BlockingQueue<Message> incoming, BlockingQueue<Message> outgoing, String self) {
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
            System.out.printf("[WORKER][main][%2d] %s\n", currentSlot, msg.toString());
            TimeSlot tmp = new TimeSlot();
            tmp.setTeam(msg.getSender());
            future[nextSlot] = tmp;
            return true;
        }
        else {
            System.out.printf("[WORKER][main][%2d] Slot already in use! (%s)\n", currentSlot, msg.toString());
            return false;
        }
    }

    private void insertTimeSlotIntoCurrent(byte slot, String team) {
        TimeSlot tmp = new TimeSlot();
        tmp.setSlot(slot);
        tmp.setTeam(team);
        current[slot] = tmp;
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
            if (silence && ((TimeHandler.generateTimeStamp() - startingTime) >= 2000)) {
                synced = true;
                isFirst = true;
            }
        }

        long beginOfNextSlot = (TimeHandler.generateTimeStamp() / 1000) * 1000 + 1000;
        try {
            Thread.sleep(beginOfNextSlot - TimeHandler.generateTimeStamp());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (isFirst) {
            System.out.println("[Worker] I'm alone....");
        }
        else {
            System.out.println("[Worker] Worker synchronized, now ...");
        }
        currentSlot = 0;
        beginOfNextSlot += 50;

        boolean sending = false;
        boolean sentMessage = false;
        List<Message> receivedMessages = new ArrayList<Message>();
        while (!isInterrupted()) {
            try {
                msg = incoming.poll(10, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (TimeHandler.generateTimeStamp() >= beginOfNextSlot) {
                if (receivedMessages.size() > 1) {
                    if (sentMessage) {
                        sending = false;
                    }
                    System.out.printf("[Worker][main][%2d] Collision!\n", currentSlot);
                }
                else if (receivedMessages.size() == 1) {
                    if (!insertMessageIntoFuture(receivedMessages.get(0))
                            && receivedMessages.get(0).getSender().equals(self)) {
                        sending = false;
                    }
                }
                receivedMessages.clear();
                currentSlot = (currentSlot + 1) % NUMBER_OF_SLOTS;
                if (currentSlot == 0) {
                    current = future;
                    future = new TimeSlot[NUMBER_OF_SLOTS];
                    int cnt = 1;
                    int average = 0;
                    for (int i = 0; i < diffrences.length; i++) {
                        if (diffrences[i] != 0) {
                            average += diffrences[i];
                            cnt++;
                            diffrences[i] = 0;
                        }
                    }
                    if (cnt > 1) {
                        TimeHandler.adjustTime(-(average / cnt));
                    }
                    if (!sending) {
                        byte nextSlot = findFreeSlotFromIndexIn(currentSlot, current, RANDOM);
                        if (nextSlot >= 0) {
                            insertTimeSlotIntoCurrent(nextSlot, self);
                            sending = true;
                        } else {
                        	System.out.println("COULDNT FIND A FREE SLOT!!!!!!!");
                        }
                    }
                }
                beginOfNextSlot += 50;
                sentMessage = false;
            }

            TimeSlot now = current[currentSlot];
            if (msg == null) { // no message received, maybe i need to send
                if (now != null) {
                    if (now.getTeam().equals(self)) { // i have to send
                        if (sending && !sentMessage && TimeHandler.generateTimeStamp() >= (beginOfNextSlot - 30)) {
                            byte nextSlot = findFreeSlotFromIndexIn(0, future, RANDOM);
                            if (nextSlot == -1) {
                                sending = false;
                                System.err.println("[Worker] No free slot available.");
                            }
                            else {
                                Message tmp = null;
                                BufferedInputStream bis = new BufferedInputStream(System.in);
                                byte[] input = new byte[24];

                                try {
                                    if (bis.read(input) != 24) {
                                        System.out.println("Fehler mit der DatenQuelle");
                                    }
                                    else {
                                        tmp = new Message(input, nextSlot, System.currentTimeMillis());
                                    }
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (tmp != null) {
                                    outgoing.add(tmp);
                                    sentMessage = true;
                                }
                                else {
                                    sending = false;
                                }
                            }
                        }
                    }
                }
            }
            else { // received message
                receivedMessages.add(msg);
                if (!msg.getSender().equals(self)) {
                    diffrences[currentSlot] = msg.getOurTimestamp() - msg.getTimeStamp();
                }
            }
        }
        System.out.println("[Worker] Bye!");
    }
}
