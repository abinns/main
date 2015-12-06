import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

import backend.U;

public class TDGDriver
{
	public static void main(String[] args)
	{
		try
		{
			AppGameContainer appgc;
			appgc = new AppGameContainer(new TDGContainer());
			appgc.setDisplayMode(1024, 600, false);
			appgc.start();
		} catch (SlickException ex)
		{
			U.e("Slick Error; ", ex);
		}
	}
}
