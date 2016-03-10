package reciever;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.DatagramChannel;

import shared.Util;

public class RecieverDriver
{
	// One second in nanoseconds
	private static final long	OFFSET_NS	= 1000000000L;
	private static int			count		= 0;
	private static long			time		= System.nanoTime() + OFFSET_NS;

	public static void main(String[] args)
	{
		try
		{
			DatagramChannel chan = Util.openMulticastChannel(Util.DST_PORT, InetAddress.getByName(Util.DST_GROUP));

			Util.getPacketStream(chan).forEach(p -> {
				count++;
				if (time <= System.nanoTime())
				{
					time = System.nanoTime() + OFFSET_NS;
					System.out.println(count + "/sec");
					count = 0;
				}
			});
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
