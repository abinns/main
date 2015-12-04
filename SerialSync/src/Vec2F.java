
import java.util.concurrent.ThreadLocalRandom;

import backend.fastserialize.FastSerializable;
import backend.fastserialize.UnsafeMemory;

public class Vec2F implements FastSerializable
{
	public static Vec2F random(int minWidth, int minHeight, int maxWidth, int maxHeight)
	{
		double nX = ThreadLocalRandom.current().nextDouble(minWidth, maxWidth);
		double nY = ThreadLocalRandom.current().nextDouble(minHeight, maxHeight);
		return new Vec2F(nX, nY);
	}

	public float x;

	public float y;

	public Vec2F(double nX, double nY)
	{
		this((float) nX, (float) nY);
	}

	public Vec2F(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public void add(double dx, double dy)
	{
		this.add((float) dx, (float) dy);
	}

	public void add(float dx, float dy)
	{
		this.x += dx;
		this.y += dy;
	}

	public void add(Vec2F other)
	{
		this.x += other.x;
		this.y += other.y;
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
		dst.putFloat(this.x);
		dst.putFloat(this.y);
	}

	@Override
	public void stateSet(UnsafeMemory src)
	{
		this.x = src.getFloat();
		this.y = src.getFloat();
	}

	@Override
	public String toString()
	{
		return this.x + "," + this.y;
	}
}
