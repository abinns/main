package game;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.Game;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;

import backend.Renderable;
import backend.Updateable;

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
		Projectile proj = Projectile.genTestCursorCircles(new Vec2(0, 0), 10, Color.blue, Color.green, Color.red, Color.magenta);
		this.renderables.add(proj);
		this.updateables.add(proj);
	}

	@Override
	public void update(GameContainer gc, int millis) throws SlickException
	{
		for (Updateable u : this.updateables)
			u.update(gc, millis);
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException
	{
		for (Renderable r : this.renderables)
			r.render(gc, g);
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