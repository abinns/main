package backend.fastserialize;
import java.lang.reflect.Field;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class UnsafeMemory
{

	private static final Unsafe unsafe;

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
	}

	private static final long	byteArrayOffset		= UnsafeMemory.unsafe.arrayBaseOffset(byte[].class);
	private static final long	longArrayOffset		= UnsafeMemory.unsafe.arrayBaseOffset(long[].class);
	private static final long	doubleArrayOffset	= UnsafeMemory.unsafe.arrayBaseOffset(double[].class);
	private static final long	floatArrayOffset	= UnsafeMemory.unsafe.arrayBaseOffset(float[].class);

	private static final int	SIZE_OF_BOOLEAN	= 1;
	private static final int	SIZE_OF_INT		= 4;
	private static final int	SIZE_OF_LONG	= 8;
	private int					pos				= 0;
	private final byte[]		buffer;

	public UnsafeMemory(final byte[] buffer)
	{
		if (null == buffer)
			throw new NullPointerException("buffer cannot be null");

		this.buffer = buffer;
	}

	public boolean getBoolean()
	{
		boolean value = UnsafeMemory.unsafe.getBoolean(this.buffer, UnsafeMemory.byteArrayOffset + this.pos);
		this.pos += UnsafeMemory.SIZE_OF_BOOLEAN;

		return value;
	}

	public double[] getDoubleArray()
	{
		int arraySize = this.getInt();
		double[] values = new double[arraySize];
		long bytesToCopy = values.length << 3;
		UnsafeMemory.unsafe.copyMemory(this.buffer, UnsafeMemory.byteArrayOffset + this.pos, values, UnsafeMemory.doubleArrayOffset, bytesToCopy);
		this.pos += bytesToCopy;
		return values;
	}

	public float getFloat()
	{
		float value = UnsafeMemory.unsafe.getFloat(this.buffer, UnsafeMemory.byteArrayOffset + this.pos);
		this.pos += UnsafeMemory.SIZE_OF_INT;
		return value;
	}

	public float[] getFloatArray()
	{
		int arraySize = this.getInt();
		float[] values = new float[arraySize];
		long bytesToCopy = values.length << 2;
		UnsafeMemory.unsafe.copyMemory(this.buffer, UnsafeMemory.floatArrayOffset + this.pos, values, UnsafeMemory.doubleArrayOffset, bytesToCopy);
		this.pos += bytesToCopy;
		return values;
	}

	public int getInt()
	{
		int value = UnsafeMemory.unsafe.getInt(this.buffer, UnsafeMemory.byteArrayOffset + this.pos);
		this.pos += UnsafeMemory.SIZE_OF_INT;

		return value;
	}

	public long getLong()
	{
		long value = UnsafeMemory.unsafe.getLong(this.buffer, UnsafeMemory.byteArrayOffset + this.pos);
		this.pos += UnsafeMemory.SIZE_OF_LONG;

		return value;
	}

	public long[] getLongArray()
	{
		int arraySize = this.getInt();
		long[] values = new long[arraySize];

		long bytesToCopy = values.length << 3;
		UnsafeMemory.unsafe.copyMemory(this.buffer, UnsafeMemory.byteArrayOffset + this.pos, values, UnsafeMemory.longArrayOffset, bytesToCopy);
		this.pos += bytesToCopy;

		return values;
	}

	public void putBoolean(final boolean value)
	{
		UnsafeMemory.unsafe.putBoolean(this.buffer, UnsafeMemory.byteArrayOffset + this.pos, value);
		this.pos += UnsafeMemory.SIZE_OF_BOOLEAN;
	}

	public void putDoubleArray(final double[] values)
	{
		this.putInt(values.length);

		long bytesToCopy = values.length << 3;
		UnsafeMemory.unsafe.copyMemory(values, UnsafeMemory.doubleArrayOffset, this.buffer, UnsafeMemory.byteArrayOffset + this.pos, bytesToCopy);
		this.pos += bytesToCopy;
	}

	public void putFloat(final float value)
	{
		UnsafeMemory.unsafe.putFloat(this.buffer, UnsafeMemory.byteArrayOffset + this.pos, value);
		this.pos += UnsafeMemory.SIZE_OF_INT;
	}

	public void putFloatArray(final float[] values)
	{
		this.putInt(values.length);

		long bytesToCopy = values.length << 2;
		UnsafeMemory.unsafe.copyMemory(values, UnsafeMemory.floatArrayOffset, this.buffer, UnsafeMemory.byteArrayOffset + this.pos, bytesToCopy);
		this.pos += bytesToCopy;
	}

	public void putInt(final int value)
	{
		UnsafeMemory.unsafe.putInt(this.buffer, UnsafeMemory.byteArrayOffset + this.pos, value);
		this.pos += UnsafeMemory.SIZE_OF_INT;
	}

	public void putLong(final long value)
	{
		UnsafeMemory.unsafe.putLong(this.buffer, UnsafeMemory.byteArrayOffset + this.pos, value);
		this.pos += UnsafeMemory.SIZE_OF_LONG;
	}

	public void putLongArray(final long[] values)
	{
		this.putInt(values.length);

		long bytesToCopy = values.length << 3;
		UnsafeMemory.unsafe.copyMemory(values, UnsafeMemory.longArrayOffset, this.buffer, UnsafeMemory.byteArrayOffset + this.pos, bytesToCopy);
		this.pos += bytesToCopy;
	}

	public void reset()
	{
		this.pos = 0;
	}
}