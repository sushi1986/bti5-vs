package threads;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import work.TimeHandler;

/**
 * VS Lab4
 * @author Phillip Gesien, Raphael Hiesgen
 */

public class Message {

    private byte[] data;
    private String sender;
    private byte nextSlot;
    private long timeStamp;

    private long ourTimestamp;

    public Message(byte[] data, String sender, byte nextSlot, long timeStamp) {
        super();
        this.data = data;
        this.sender = sender;
        this.nextSlot = nextSlot;
        this.timeStamp = timeStamp;
        this.ourTimestamp = TimeHandler.generateTimeStamp();
    }

    private Message(byte[] data, String sender, byte nextSlot, byte[] longValue) {
        this(data, sender, nextSlot);

        long tmp = 0;
        for (int i = 0; i < longValue.length; i++) {
            tmp <<= 8;
            tmp |= (longValue[i] & 0xFF);
        }
        timeStamp = tmp;
    }

    public Message(byte[] data, byte nextSlot, long longValue) {
        this(Arrays.copyOfRange(data, 10, 24), new String(Arrays.copyOfRange(data, 0, 10)), nextSlot, longValue);
    }

    private Message(byte[] data, String sender, byte nextSlot) {
        this(data, sender, nextSlot, 0);
    }

    public Message(byte[] input) {
        this(Arrays.copyOfRange(input, 10, 24), new String(Arrays.copyOfRange(input, 0, 10)), input[24], Arrays
                .copyOfRange(input, 25, 33));
    }

    public byte[] getBytes() {

        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(ByteOrder.BIG_ENDIAN);

        byte[] glub = String.format("%-10s", sender).getBytes();
        for (int i = sender.length(); i < glub.length; i++) {
            glub[i] = 0;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(33);
        DataOutputStream dis = new DataOutputStream(baos);

        if (data.length != 14) {
            byte[] tmp = new byte[14];
            int i;
            for (i = 0; i < data.length; i++) {
                if (i >= data.length || i >= tmp.length) {
                    break;
                }
                else {
                    tmp[i] = data[i];
                }
            }
            if (++i < tmp.length) {
                for (; i < tmp.length; i++) {
                    tmp[i] = 0;
                }
            }
            data = tmp;
        }

        byte[] ba = new byte[8];
        for (int i = 0; i < ba.length; i++) {
            long x = (0xFF00000000000000L >> i * 8);
            long t = (timeStamp & x);
            ba[i] = (byte) (t >> (64 - (i + 1) * 8));// 56);

        }

        try {
            dis.write(glub);
            dis.write(data);
            dis.writeByte(nextSlot);
            dis.write(ba);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public byte[] getData() {
        return data;
    }

    public String getSender() {
        return sender;
    }

    public byte getNextSlot() {
        return nextSlot;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getOurTimestamp() {
        return ourTimestamp;
    }

    @Override
    public String toString() {
        return "Message [data=" + Arrays.toString(data) + ", sender=" + sender + ", nextSlot=" + nextSlot
                + ", timeStamp=" + timeStamp + ", ourTimestamp=" + ourTimestamp + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + nextSlot;
        result = prime * result + (int) (ourTimestamp ^ (ourTimestamp >>> 32));
        result = prime * result + ((sender == null) ? 0 : sender.hashCode());
        result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Message other = (Message) obj;
        if (!Arrays.equals(data, other.data)) return false;
        if (nextSlot != other.nextSlot) return false;
        if (ourTimestamp != other.ourTimestamp) return false;
        if (sender == null) {
            if (other.sender != null) return false;
        }
        else if (!sender.equals(other.sender)) return false;
        if (timeStamp != other.timeStamp) return false;
        return true;
    }
}
