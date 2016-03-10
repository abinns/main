package sender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;

import shared.Packet;
import shared.Util;

public class SenderDriver
{

	public static void main(String[] args) throws IOException
	{
		int port = 4242;
		InetAddress group = InetAddress.getByName("230.1.2.3");

		new Thread(() -> {
			sendMessages(port, group);
		}).start();
		new Thread(() -> {
			sendMessages(port, group);
		}).start();
	}

	private static void sendMessages(int port, InetAddress group)
	{
		InetSocketAddress dest = new InetSocketAddress(group, port);
		try
		{
			DatagramChannel chan = Util.openMulticastChannel(port, group);
			ByteBuffer buff = ByteBuffer.allocateDirect(Packet.getSize()).order(ByteOrder.nativeOrder());
			while (true)
			{
				buff.rewind();
				new Packet().exportTo(buff);
				buff.rewind();
				chan.send(buff, dest);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

}
