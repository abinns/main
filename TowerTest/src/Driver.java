import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import game.TowerTestGame;

public class Driver
{
	public static void main(String[] args)
	{
		try
		{
			AppGameContainer appgc;
			appgc = new AppGameContainer(new TowerTestGame());
			appgc.setDisplayMode(1024, 600, false);
			appgc.start();
		} catch (SlickException ex)
		{
			Logger.getLogger(TowerTestGame.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
