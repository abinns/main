package shared;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;

public class PacketHeader
{
	private static final int	properMagic	= ByteBuffer.wrap(new byte[]
												{ (byte) 0xa1, (byte) 0xb2, (byte) 0xc3, (byte) 0xd4 })
			.getInt();
	private Instant				time;

	public PacketHeader(ByteBuffer buff)
	{
		int magic = buff.getInt();
		if (magic != properMagic)
			if (buff.order() == ByteOrder.BIG_ENDIAN)
				buff.order(ByteOrder.LITTLE_ENDIAN);
			else if (buff.order() == ByteOrder.LITTLE_ENDIAN)
				buff.order(ByteOrder.BIG_ENDIAN);

		this.time = Instant.ofEpochSecond(buff.getLong(), buff.getInt());
	}

	public PacketHeader()
	{
		this.time = Instant.now();
	}

	public Instant getTime()
	{
		return this.time;
	}

	public void exportTo(ByteBuffer buff)
	{
		buff.putInt(properMagic);
		buff.putLong(this.time.getEpochSecond());
		buff.putInt(this.time.getNano());
	}

	public static int getSize()
	{
		int size = 0;
		// 4 bytes for the magic number
		size += 4;
		// 8 for the long, and 4 for the int for the timestamp
		size += 12;
		return size;
	}

	@Override
	public String toString()
	{
		return this.time.toString();
	}
}
