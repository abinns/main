package engine;

import java.util.concurrent.ThreadLocalRandom;

public class Vec2
{
	public Vec2(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public Vec2(double nX, double nY)
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

	public static Vec2 random(int minWidth, int minHeight, int maxWidth, int maxHeight)
	{
		double nX = ThreadLocalRandom.current().nextDouble(minWidth, maxWidth);
		double nY = ThreadLocalRandom.current().nextDouble(minHeight, maxHeight);
		return new Vec2(nX, nY);
	}
}
