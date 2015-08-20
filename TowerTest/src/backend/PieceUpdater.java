package backend;

import game.Vec2;

@FunctionalInterface
public interface PieceUpdater
{
	public void update(Vec2 parentLoc, Vec2 pieceLoc, int time);
}
