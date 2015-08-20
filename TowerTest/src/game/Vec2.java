package game;

public class Vec2
{
	public Vec2(float x, float y)
	{
		this.x = x;
		this.y = y;
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
}
