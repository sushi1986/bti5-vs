import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import threads.Message;
import threads.ReceiveThread;
import threads.SendThread;
import work.Worker;

/**
 * VS Lab4
 * @author Phillip Gesien, Raphael Hiesgen
 */

public class Main {

    private static String readTeam() {
        BufferedInputStream bis = new BufferedInputStream(System.in);
        byte[] input = new byte[24];
        String resu = null;
        try {
            if (bis.read(input) != 24) {
                System.out.println("Fehler mit der DatenQuelle");
            }
            else {
                resu = new String(Arrays.copyOfRange(input, 0, 10));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return resu;
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java Main <GROUP> <PORT>");
            System.exit(0);
        }

        String team = readTeam();
        String group = args[0];
        int port = Integer.valueOf(args[1]);

        BlockingQueue<Message> rcvMsgs = new LinkedBlockingQueue<Message>();
        BlockingQueue<Message> sndMsgs = new LinkedBlockingQueue<Message>();

        final SendThread s = new SendThread(sndMsgs, group, port);
        ReceiveThread r = new ReceiveThread(rcvMsgs, group, port);
        Thread t = new Worker(r.getReceivedMsgs(), s.getMsgsToSend(), team);

        s.start();
        r.start();
        t.start();

        try {
            s.join();
            r.join();
            t.join();
        }
        catch (Exception exc) {
            System.out.println("blub");
        }
    }
}
