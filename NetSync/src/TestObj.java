public class TestObj
{
	private Vec2F	pos;
	private Vec2F	vel;
	private Vec2F	accel;

	public TestObj(float x, float y, float xVel, float yVel, float xAccel, float yAccel)
	{
		this.accel = new Vec2F(xAccel, yAccel, yAccel * 1);
		this.vel = new Vec2F(xVel, yVel, yAccel * 2);
		this.pos = new Vec2F(x, y, yAccel * 3.141592);
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
