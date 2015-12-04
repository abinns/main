package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

public class Projectile implements Renderable, Updateable
{
	private List<Piece>			shapes;
	private Vec2				pos;
	private Consumer<Integer>	tick;

	private Projectile(Vec2 pos)
	{
		this.pos = pos;
		shapes = new ArrayList<>();
	}

	private double state = 0;

	public static Projectile genBouncer(int width, int height)
	{
		Projectile proj = new Projectile(Vec2.random(0, 0, width, height));
		Vec2 vel = Vec2.random(-10, -10, 10, 10);
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		float buffer = 20.0f;
		proj.tick = in -> {
		};
		proj.shapes.add(Piece.genCircle(proj.pos, rand.nextFloat() * 10 + 1, new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()), (origin, result, delta) -> {
			result.x += vel.x / 10 * delta;
			result.y += vel.y / 10 * delta;
			if (result.x < buffer)
				result.x = width - buffer;
			if (result.x > width - buffer)
				result.x = buffer;
			if (result.y < 10.0)
				result.y = height - buffer;
			if (result.y > height - buffer)
				result.y = buffer;
		}));

		return proj;
	}

	public static Projectile genTestCursorCircles(Vec2 pos, float radius, Color... colors)
	{
		Projectile proj = new Projectile(pos);
		proj.tick = delta -> {
			proj.state += delta / 1.2;
			proj.state %= 360;
		};
		int i = 0;
		proj.shapes.add(Piece.genCircle(pos, radius, colors[i], (origin, result, delta) -> {
			result.x = (float) (radius * 2.0 * Math.cos(proj.state * Math.PI / 180.0)) + origin.x;
			result.y = (float) (radius * 2.0 * Math.sin(proj.state * Math.PI / 180.0)) + origin.y;
		}));
		i = (i + 1) % colors.length;
		proj.shapes.add(Piece.genCircle(pos, radius, colors[i], (origin, result, delta) -> {
			result.x = (float) (radius * -2.0 * Math.cos(proj.state * Math.PI / 180.0)) + origin.x;
			result.y = (float) (radius * 2.0 * Math.sin(proj.state * Math.PI / 180.0)) + origin.y;
		}));
		i = (i + 1) % colors.length;
		proj.shapes.add(Piece.genCircle(pos, radius, colors[i], (origin, result, delta) -> {
			result.x = (float) (radius * 2.0 * Math.cos(proj.state * Math.PI / 180.0)) + origin.x;
			result.y = (float) (radius * -2.0 * Math.sin(proj.state * Math.PI / 180.0)) + origin.y;
		}));
		i = (i + 1) % colors.length;
		proj.shapes.add(Piece.genCircle(pos, radius, colors[i], (origin, result, delta) -> {
			result.x = (float) (radius * -2.0 * Math.cos(proj.state * Math.PI / 180.0)) + origin.x;
			result.y = (float) (radius * -2.0 * Math.sin(proj.state * Math.PI / 180.0)) + origin.y;
		}));
		return proj;
	}

	@Override
	public void render(GameContainer gc, Graphics g)
	{
		for (int i = 0; i < this.shapes.size(); i++)
			this.shapes.get(i).render(gc, g);
	}

	@Override
	public void update(GameContainer gc, int millis)
	{
		pos.x = gc.getInput().getMouseX();
		pos.y = gc.getInput().getMouseY();
		this.tick.accept(millis);
		// for (Piece cur : this.shapes)
		// cur.update(gc, millis, this.pos);
		for (int i = 0; i < this.shapes.size(); i++)
			this.shapes.get(i).update(gc, millis, this.pos);
	}
}
