package old;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import backend.U;

public class ShaderUtils
{
	public static int create(String vert, String frag)
	{
		// creates a program object and assigns it to the
		// variable program.
		int program = GL20.glCreateProgram();
		// glCreateShader specificies the type of shader
		// that we want created. For the vertex shader
		// we define it as GL_VERTEX_SHADER
		int vertID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		// Specificies that we want to create a
		// GL_FRAGMENT_SHADER
		int fragID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		// glShaderSource replaces the source code in a shader
		// object.
		// We've defined our vertex shader object and now
		// we want to pass in our vertex shader that we
		// managed to build as a string in our load
		// function.
		//
		GL20.glShaderSource(vertID, vert);
		// does the same for our fragment shader
		GL20.glShaderSource(fragID, frag);

		// This group of code tries to compile our shader object
		// it then gets the status of that compiled shader and
		// if it proves to be false then it prints an error to
		// the command line.
		GL20.glCompileShader(vertID);
		if (GL20.glGetShaderi(vertID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println("Failed to compile vertexd shader!");
			System.err.println(GL20.glGetShaderInfoLog(vertID));
		}

		// This group of code tries to compile our shader object
		// it then gets the status of that compiled shader and
		// if it proves to be false then it prints an error to
		// the command line.
		GL20.glCompileShader(fragID);
		if (GL20.glGetShaderi(fragID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println("Failed to compile fragment shader!");
			System.err.println(GL20.glGetShaderInfoLog(fragID));
		}

		// This attaches our vertex and fragment shaders
		// to the program object that we defined at the
		// start of this tutorial.
		GL20.glAttachShader(program, vertID);
		GL20.glAttachShader(program, fragID);
		// this links our program object
		GL20.glLinkProgram(program);
		//
		GL20.glValidateProgram(program);

		// this then returns our created program
		// object.
		return program;
	}

	public static int load(String vertPath, String fragPath)
	{
		// These lines of code first take in the file path
		// for both our vertex shader and our fragment shader
		// and then create a string containing all
		// of the source code of both shaders and put them
		// into vert and frag. These will later be passed into
		// our created shader objects in our create() function
		String vert = U.loadAsString(vertPath);
		String frag = U.loadAsString(fragPath);
		return ShaderUtils.create(vert, frag);
	}

	private ShaderUtils()
	{
	}
}
