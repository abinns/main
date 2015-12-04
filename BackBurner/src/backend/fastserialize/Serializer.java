package backend.fastserialize;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import backend.U;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class Serializer
{
	private static final Unsafe unsafe;

	private static final TObjectIntMap<Class<?>> sizes;

	static
	{
		try
		{
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		sizes = new TObjectIntHashMap<>();
		sizes.put(boolean.class, 1);
		sizes.put(byte.class, 1);
		sizes.put(char.class, 2);
		sizes.put(short.class, 2);
		sizes.put(int.class, 4);
		sizes.put(long.class, 8);
		sizes.put(float.class, 4);
		sizes.put(double.class, 8);
	}

	private static int sizeOf(final Class<?> struct)
	{
		if (sizes.containsKey(struct))
			return sizes.get(struct);
		U.p("Calculating size of " + struct.getName());
		U.p(struct.isArray());
		int size = 0;
		Class<?> clazz = struct;
		while (clazz != Object.class)
		{
			for (Field f : clazz.getDeclaredFields())
				if ((f.getModifiers() & Modifier.STATIC) == 0)
					size += sizeOf(f.getType());

			clazz = clazz.getSuperclass();
		}
		U.p(struct + " is " + size);
		sizes.put(struct, size);
		U.p(sizes);
		return size;
	}

	private static Unsafe getUnsafe()
	{
		return unsafe;
	}

	public void register(Class<?> clazz)
	{
		// Pre-load sizes
		sizeOf(clazz);
		U.p(sizeOf(clazz));
	}

	public byte[] getArr(Class<?> clazz)
	{
		return new byte[sizes.get(clazz)];
	}

	public void storeState(Object o, byte[] arr)
	{
		Class<?> clazz = o.getClass();

	}
}
