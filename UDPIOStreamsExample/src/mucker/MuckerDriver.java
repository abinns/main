package mucker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.Supplier;

import shared.Packet;
import shared.Util;

public class MuckerDriver
{
	// One second in nanoseconds
	private static final long	OFFSET_NS	= 1000000000L;
	private static int			count		= 0;
	private static long			time		= System.nanoTime() + OFFSET_NS;

	public static void main(String[] args) throws IOException
	{
		DatagramChannel chan = Util.openMulticastChannel(Util.SRC_PORT, InetAddress.getByName(Util.SRC_GROUP));
		InetSocketAddress dest = new InetSocketAddress(InetAddress.getByName(Util.DST_GROUP), Util.DST_PORT);
		Supplier<ByteBuffer> localBuff = Util.getLocalPacketBufferSet();

		Util.getPacketStream(chan).map(MuckerDriver::muckWith).forEach(packet -> {
			try
			{
				ByteBuffer buff = localBuff.get();
				buff.rewind();
				packet.exportTo(buff);
				buff.rewind();
				chan.send(buff, dest);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}

	public static Packet muckWith(Packet p)
	{
		count++;
		if (time <= System.nanoTime())
		{
			time = System.nanoTime() + OFFSET_NS;
			System.out.println(count + "/sec");
			count = 0;
		}
		short[] data = p.getData();
		for (int i = 0; i < data.length / 2; i++)
		{
			short temp = data[i];
			data[i] = data[data.length - i - 1];
			data[data.length - i - 1] = temp;
		}
		return p;
	}
}
