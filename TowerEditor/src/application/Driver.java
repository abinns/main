package application;


import org.newdawn.slick.Color;

import engine.Projectile;
import engine.Vec2;

public class Driver
{
	public static void main(String... cheese)
	{
		Projectile proj = Projectile.genTestCursorCircles(new Vec2(0, 0), 100, Color.blue, Color.green, Color.red, Color.magenta);
	}
}
