
public class TestObj implements FastSerializable
{
	private Vec2F	pos;
	private Vec2F	vel;
	private Vec2F	accel;

	public TestObj(float x, float y, float xVel, float yVel, float xAccel, float yAccel)
	{
		accel = new Vec2F(xAccel, yAccel);
		vel = new Vec2F(xVel, yVel);
		pos = new Vec2F(x, y);
	}

	public void tick()
	{
		pos.add(vel);
		vel.add(accel);
	}

	@Override
	public int getSize()
	{
		int size = 0;
		size += pos.getSize();
		size += vel.getSize();
		size += accel.getSize();
		return size;
	}

	@Override
	public void stateGet(UnsafeMemory dst)
	{
		pos.stateGet(dst);
		vel.stateGet(dst);
		accel.stateGet(dst);
	}

	@Override
	public void stateSet(UnsafeMemory src)
	{
		pos.stateSet(src);
		vel.stateSet(src);
		accel.stateSet(src);
	}

	@Override
	public String toString()
	{
		return this.pos + " :: " + this.vel + " :: " + this.accel;
	}
}
