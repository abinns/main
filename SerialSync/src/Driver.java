import backend.U;
import backend.fastserialize.UnsafeMemory;

public class Driver
{

	public static void main(String[] args)
	{

		TestObj a = new TestObj(0, 0, 0, 0, 1.2f, 1.3f);
		TestObj b = new TestObj(0, 0, 0, 0, 1.2f, 1.54363f);

		U.p("A => " + a);
		U.p("B => " + b);

		byte[] arr = new byte[a.getSize()];
		UnsafeMemory mem = new UnsafeMemory(arr);

		long start, total = 0;
		for (int i = 0; i < 1000000000; i++)
		{
			start = System.nanoTime();
			mem.reset();
			a.stateGet(mem);
			mem.reset();
			b.stateSet(mem);
			total += System.nanoTime() - start;
			a.tick();
		}
		U.p(total / 1000000000.0 + "sec");
		U.p(total / 1000000000.0 + "ns/per");

		U.p("A => " + a);
		U.p("B => " + b);
	}

}
