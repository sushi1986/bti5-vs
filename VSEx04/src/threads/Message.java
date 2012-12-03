package threads;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
		this.ourTimestamp = System.currentTimeMillis();
	}

	private Message(byte[] data, String sender, byte nextSlot, byte[] longValue) {
		this(data, sender, nextSlot);
		long tmp = 0;
		for (int i = 0; i < 4; i++) {
			tmp <<= 8;
			tmp ^= (long) longValue[i] & 0xFF;
		}
		timeStamp = tmp;
	}

	private Message(byte[] data, String sender, byte nextSlot) {
		this(data, sender, nextSlot, 0);
	}

	public Message(byte[] input) {
		this(Arrays.copyOfRange(input, 10, 23), new String(Arrays.copyOfRange(
				input, 0, 9)), input[24], Arrays.copyOfRange(input, 25, 32));

	}

	public byte[] getBytes() {

		ByteArrayOutputStream baos = new ByteArrayOutputStream(33);
		DataOutputStream dis = new DataOutputStream(baos);
		String glub = String.format("%-5s", sender);

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
			if (i < tmp.length) {
				for (; i < tmp.length; i++) {
					tmp[i] = 0;
				}
			}
			data = tmp;
		}

		try {
			dis.writeBytes(glub);
			dis.write(data);
			dis.writeByte(nextSlot);
			dis.writeLong(timeStamp);
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
