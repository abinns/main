import backend.fastserialize.FastSerializable;
import backend.fastserialize.UnsafeMemory;

public class TestObj implements FastSerializable
{
	private Vec2F	pos;
	private Vec2F	vel;
	private Vec2F	accel;

	public TestObj(float x, float y, float xVel, float yVel, float xAccel, float yAccel)
	{
		this.accel = new Vec2F(xAccel, yAccel);
		this.vel = new Vec2F(xVel, yVel);
		this.pos = new Vec2F(x, y);
	}

	@Override
	public int getSize()
	{
		int size = 0;
		size += this.pos.getSize();
		size += this.vel.getSize();
		size += this.accel.getSize();
		return size;
	}

	@Override
	public void stateGet(UnsafeMemory dst)
	{
		this.pos.stateGet(dst);
		this.vel.stateGet(dst);
		this.accel.stateGet(dst);
	}

	@Override
	public void stateSet(UnsafeMemory src)
	{
		this.pos.stateSet(src);
		this.vel.stateSet(src);
		this.accel.stateSet(src);
	}

	public void tick()
	{
		this.pos.add(this.vel);
		this.vel.add(this.accel);
	}

	@Override
	public String toString()
	{
		return this.pos + " :: " + this.vel + " :: " + this.accel;
	}
}
