package shared;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Util
{
	public static final int		DST_PORT	= 4243;
	public static final String	DST_GROUP	= "230.1.2.4";
	public static final int		SRC_PORT	= 4242;
	public static final String	SRC_GROUP	= "230.1.2.3";

	public static DatagramChannel openMulticastChannel(int port, InetAddress group) throws SocketException, UnknownHostException, IOException
	{
		NetworkInterface interf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
		DatagramChannel chan = DatagramChannel.open(StandardProtocolFamily.INET).setOption(StandardSocketOptions.SO_REUSEADDR, true).bind(new InetSocketAddress(port))
				.setOption(StandardSocketOptions.IP_MULTICAST_IF, interf);
		chan.configureBlocking(true);
		chan.join(group, interf);
		return chan;
	}

	public static Supplier<ByteBuffer> getLocalPacketBufferSet()
	{
		Map<Thread, ByteBuffer> buffs = new HashMap<>();
		return () -> {
			ByteBuffer buff = buffs.get(Thread.currentThread());
			if (buff == null)
			{
				buff = ByteBuffer.allocateDirect(Packet.getSize()).order(ByteOrder.nativeOrder());
				buffs.put(Thread.currentThread(), buff);
			}
			return buff;

		};
	}

	public static Stream<Packet> getPacketStream(DatagramChannel chan)
	{
		Supplier<ByteBuffer> localBuff = Util.getLocalPacketBufferSet();
		return Stream.generate(() -> {
			ByteBuffer buff = localBuff.get();
			buff.rewind();
			try
			{
				chan.receive(buff);
				buff.rewind();
			} catch (Exception e)
			{
				System.err.println("ERROR: " + e.getClass().getName() + ":" + e.getMessage());
			}
			Packet p = new Packet(buff);
			buff.clear();
			return p;
		});
	}
}
