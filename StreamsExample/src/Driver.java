import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.stream.Stream;

public class Driver
{

	private static final int size = 100;

	public static void main(String[] args) throws IOException
	{
		String filename = "D:\\GameVids\\Vids.7z";
		getStream(filename).map(Arrays::toString).forEach(System.out::println);

	}

	private static Stream<byte[]> getStream(String filename) throws FileNotFoundException, IOException
	{
		try (RandomAccessFile randomAccessFile = new RandomAccessFile(filename, "r"))
		{
			MappedByteBuffer buffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, randomAccessFile.length());
			byte[] buff = new byte[100];

			return Stream.generate(() -> {
				buffer.get(buff);
				return buff;
			});
		}
	}

}
