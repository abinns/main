
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Vec2F
{
	public static Vec2F random(int minWidth, int minHeight, int maxWidth, int maxHeight)
	{
		double nX = ThreadLocalRandom.current().nextDouble(minWidth, maxWidth);
		double nY = ThreadLocalRandom.current().nextDouble(minHeight, maxHeight);
		double nZ = ThreadLocalRandom.current().nextDouble(minHeight, maxHeight);
		return new Vec2F(nX, nY, nZ);
	}

	public float x;

	public float y;

	public float[] z = new float[10];

	public Vec2F(double nX, double nY, double nZ)
	{
		this((float) nX, (float) nY, (float) nZ);
	}

	public Vec2F(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		for (int i = 0; i < this.z.length; i++)
			this.z[i] = i * z;
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
	public String toString()
	{
		return this.x + "," + this.y + Arrays.toString(z);
	}
}
