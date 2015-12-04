package game;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.Game;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import engine.Projectile;
import engine.Renderable;
import engine.Updateable;
import engine.Vec2;

public class TowerTestGame implements Game
{

	private List<Renderable>	renderables;
	private List<Updateable>	updateables;

	public TowerTestGame()
	{
		renderables = new ArrayList<>();
		updateables = new ArrayList<>();
	}

	@Override
	public void init(GameContainer gc) throws SlickException
	{
		Projectile proj = Projectile.genTestCursorCircles(new Vec2(0, 0), 8, Color.blue, Color.green, Color.red, Color.magenta);
		this.renderables.add(proj);
		this.updateables.add(proj);

		for (int i = 0; i < 20; i++)
		{
			proj = Projectile.genBouncer(gc.getWidth(), gc.getHeight());
			this.renderables.add(proj);
			this.updateables.add(proj);
		}
	}

	@Override
	public void update(GameContainer gc, int millis) throws SlickException
	{
		// for (Updateable u : this.updateables)
		// u.update(gc, millis);
		for (int i = 0; i < this.updateables.size(); i++)
			this.updateables.get(i).update(gc, millis);
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException
	{
		// for (Renderable r : this.renderables)
		// r.render(gc, g);
		for (int i = 0; i < this.renderables.size(); i++)
			this.renderables.get(i).render(gc, g);
	}

	@Override
	public boolean closeRequested()
	{
		return true;
	}

	@Override
	public String getTitle()
	{
		return "TowerTest";
	}
}