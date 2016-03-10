package shared;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;

public class Packet
{
	private PacketHeader		header;
	private static final int	DATA_SIZE	= 300;

	private short[] data = new short[DATA_SIZE];

	public Packet(ByteBuffer buff)
	{
		this.header = new PacketHeader(buff);
		for (int i = 0; i < data.length; i++)
			data[i] = buff.getShort();
	}

	public Packet()
	{
		this.header = new PacketHeader();
		for (int i = 0; i < data.length; i++)
			data[i] = (short) i;
	}

	public void exportTo(ByteBuffer buff)
	{
		this.header.exportTo(buff);
		for (int i = 0; i < data.length; i++)
			buff.putShort(data[i]);
	}

	public static int getSize()
	{
		int size = 0;
		// Get the header data's size
		size += PacketHeader.getSize();
		// Get the data's size (shorts are two bytes...)
		size += DATA_SIZE * 2;
		return size;
	}

	public Instant getTimestamp()
	{
		return this.header.getTime();
	}

	@Override
	public String toString()
	{
		return this.header + " :: " + Arrays.toString(data);
	}

	public short[] getData()
	{
		return this.data;
	}

}
