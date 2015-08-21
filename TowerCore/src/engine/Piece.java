package engine;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Shape;

public class Piece
{
	private PieceUpdater	onTick;
	private Shape			shape;
	private Vec2			pos;
	private Color			color;

	public Piece()
	{
		this.pos = new Vec2(0.0f, 0.0f);
	}

	public void update(GameContainer gc, int millis, Vec2 parentOrigin)
	{
		this.onTick.update(parentOrigin, pos, millis);
	}

	public Shape getPiece()
	{
		return this.shape;
	}

	public static Piece genCircle(Vec2 origin, float radius, Color color, PieceUpdater updater)
	{
		Piece piece = new Piece();
		updater.update(origin, piece.pos, 0);
		piece.shape = new Circle(origin.x, origin.y, radius);
		piece.color = color;
		piece.onTick = updater;
		return piece;
	}

	public void render(GameContainer gc, Graphics g)
	{
		this.shape.setCenterX(pos.x);
		this.shape.setCenterY(pos.y);
		g.setColor(this.color);
		g.draw(this.shape);
	}

}
