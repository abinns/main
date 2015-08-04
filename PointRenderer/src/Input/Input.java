package Input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

// Notice how this class 'extends' GLFWKeyCallback
// this allows us to inherit most of the functionality
// of the GLFWKeyCallback class and add in some extra
// stuff that we'll need for our game.
public class Input extends GLFWKeyCallback
{

	// a boolean array of all our keys.
	public static boolean[] keys = new boolean[65535];

	public static boolean isKeyDown(int keycode)
	{
		return Input.keys[keycode];
	}

	public static boolean isKeyUp(int keycode)
	{
		return Input.keys[keycode];
	}

	// Overrides GLFW's own implementation of the Invoke method
	// This gets called everytime a key is pressed.
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods)
	{
		Input.keys[key] = action != GLFW.GLFW_RELEASE;
	}

}
