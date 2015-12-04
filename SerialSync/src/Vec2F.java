
import java.util.concurrent.ThreadLocalRandom;

public class Vec2F implements FastSerializable
{
	public Vec2F(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public Vec2F(double nX, double nY)
	{
		this((float) nX, (float) nY);
	}

	public float	x;
	public float	y;

	public void add(float dx, float dy)
	{
		x += dx;
		y += dy;
	}

	public void add(double dx, double dy)
	{
		this.add((float) dx, (float) dy);
	}

	@Override
	public String toString()
	{
		return x + "," + y;
	}

	public static Vec2F random(int minWidth, int minHeight, int maxWidth, int maxHeight)
	{
		double nX = ThreadLocalRandom.current().nextDouble(minWidth, maxWidth);
		double nY = ThreadLocalRandom.current().nextDouble(minHeight, maxHeight);
		return new Vec2F(nX, nY);
	}

	@Override
	public int getSize()
	{
		int size = 0;
		// x
		size += 4;
		// y
		size += 4;
		return size;

	}

	@Override
	public void stateGet(UnsafeMemory dst)
	{
		dst.putFloat(x);
		dst.putFloat(y);
	}

	@Override
	public void stateSet(UnsafeMemory src)
	{
		x = src.getFloat();
		y = src.getFloat();
	}

	public void add(Vec2F other)
	{
		this.x += other.x;
		this.y += other.y;
	}
}
