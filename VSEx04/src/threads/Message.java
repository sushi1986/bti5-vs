package threads;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.Arrays;

import javax.security.auth.callback.LanguageCallback;

public class Message {

	private byte[] data;
	private String sender;
	private byte nextSlot;
	private long timeStamp;

	private long ourTimestamp;
    private static long offset = 0;
    public static void adjustTime(long offset) {
		Message.offset += offset;
	}
    public static long generateTimeStamp(){
    	return System.currentTimeMillis() + offset;
    }
    
	public Message(byte[] data, String sender, byte nextSlot, long timeStamp) {
		super();
		this.data = data;
		this.sender = sender;
		this.nextSlot = nextSlot;
		this.timeStamp = timeStamp;
		this.ourTimestamp = generateTimeStamp();
	}

	private Message(byte[] data, String sender, byte nextSlot, byte[] longValue) {
		this(data, sender, nextSlot);
		
		
		
		long tmp = 0;
		for (int i = 0; i < longValue.length; i++) {
			tmp <<= 8;
			tmp |=  (longValue[i] & 0xFF);
		}
		timeStamp = tmp;
	}

	private Message(byte[] data, String sender, byte nextSlot) {
		this(data, sender, nextSlot, 0);
	}

	public Message(byte[] input) {
		this(Arrays.copyOfRange(input, 10, 24), new String(Arrays.copyOfRange(
				input, 0, 10)), input[24], Arrays.copyOfRange(input, 25, 33));
	}

	public byte[] getBytes() {

		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.order(ByteOrder.BIG_ENDIAN);

		String glub = String.format("%-10s", sender);

		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(33);
		DataOutputStream dis = new DataOutputStream(baos);
		

		if (data.length != 14) {
			byte[] tmp = new byte[14];
			int i;
			for (i = 0; i < data.length; i++) {
				if (i >= data.length || i >= tmp.length) {
					break;
				} else {
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
		
		bb.putLong(timeStamp);

		try {
			dis.writeBytes(glub);
			dis.write(data);
			dis.writeByte(nextSlot);
			dis.write(bb.array());
		} catch (IOException e) {
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
		return "Message [data=" + Arrays.toString(data) + ", sender=" + sender
				+ ", nextSlot=" + nextSlot + ", timeStamp=" + timeStamp
				+ ", ourTimestamp=" + ourTimestamp + "]";
	}
}
