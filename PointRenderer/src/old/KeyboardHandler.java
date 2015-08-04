package old;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class KeyboardHandler extends GLFWKeyCallback
{

	public static boolean[] keys = new boolean[65536];

	// boolean method that returns true if a given key
	// is pressed.
	public static boolean isKeyDown(int keycode)
	{
		return KeyboardHandler.keys[keycode];
	}

	// The GLFWKeyCallback class is an abstract method that
	// can't be instantiated by itself and must instead be extended
	//
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods)
	{
		// TODO Auto-generated method stub
		KeyboardHandler.keys[key] = action != GLFW.GLFW_RELEASE;
	}
}