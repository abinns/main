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
	private double				time;
	private Consumer<Integer>	tick;

	private Projectile(Vec2 pos)
	{
		time = 0;
		this.pos = pos;
		shapes = new ArrayList<>();
	}

	private double state = 0;

	public static Projectile genBouncer(int width, int height)
	{
		Projectile proj = new Projectile(Vec2.random(0, 0, width, height));
		Vec2 vel = Vec2.random(-10, -10, 10, 10);
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		proj.tick = in -> {
		};
		proj.shapes.add(Piece.genCircle(proj.pos, rand.nextFloat() * 10 + 5, new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()), (origin, result, delta) -> {
			if (result.x + vel.x > width || result.x + vel.x < 0)
				vel.x = -vel.x;
			if (result.y + vel.y > width || result.y + vel.y < 0)
				vel.y = -vel.y;
			result.x += vel.x / 10 * delta;
			result.y += vel.y / 10 * delta;
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
		for (Piece cur : this.shapes)
			cur.render(gc, g);
	}

	@Override
	public void update(GameContainer gc, int millis)
	{
		pos.x = gc.getInput().getMouseX();
		pos.y = gc.getInput().getMouseY();
		this.tick.accept(millis);
		for (Piece cur : this.shapes)
			cur.update(gc, millis, this.pos);
	}
}
