package old;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL20;

import lwjgl.math.Matrix4f;
import lwjgl.math.Vector3f;

public class Shader
{
	// We used these variables to set where we
	// would be storing both our vertex and texture
	// coordinates in our Vertex Array Object!
	public static final int	VERTEX_ATTRIB			= 0;
	public static final int	TEXTURE_COORDS_ATTRIB	= 1;
	public static Shader	shader1;

	public static void loadAll()
	{
		Shader.shader1 = new Shader("shaders/bg.vert", "shaders/bg.frag");
	}

	public static void loadShader(String vertShader, String fragShader)
	{
		new Shader(vertShader, fragShader);
	}

	// our way to identify what shader this is.
	private final int ID;

	// A performance increasing tactic taught I learned from
	// @TheCherno - You should definitely check out his
	// tutorials as I based the starting code on some of
	// the stuff I learned from his videos!
	private Map<String, Integer> locationCache = new HashMap<String, Integer>();

	private boolean enabled = false;

	public Shader(String vertex, String fragment)
	{
		this.ID = ShaderUtils.load(vertex, fragment);
	}

	public void disable()
	{
		GL20.glUseProgram(0);
		this.enabled = false;
	}

	public void enable()
	{
		GL20.glUseProgram(this.ID);
		this.enabled = true;
	}

	public int getUniform(String name)
	{
		if (this.locationCache.containsKey(name))
			return this.locationCache.get(name);
		int result = GL20.glGetUniformLocation(this.ID, name);
		if (result == -1)
			System.err.println("Could not find uniform variable'" + name + "'!");
		else
			this.locationCache.put(name, result);
		return GL20.glGetUniformLocation(this.ID, name);
	}

	public void setUniform1f(String name, float value)
	{
		if (!this.enabled)
			this.enable();
		GL20.glUniform1f(this.getUniform(name), value);
	}

	public void setUniform1i(String name, int value)
	{
		if (!this.enabled)
			this.enable();
		GL20.glUniform1i(this.getUniform(name), value);
	}

	public void setUniform2f(String name, float x, float y)
	{
		if (!this.enabled)
			this.enable();
		GL20.glUniform2f(this.getUniform(name), x, y);
	}

	public void setUniform3f(String name, Vector3f vector)
	{
		if (!this.enabled)
			this.enable();
		GL20.glUniform3f(this.getUniform(name), vector.x, vector.y, vector.z);
	}

	public void setUniformMat4f(String name, Matrix4f matrix)
	{
		if (!this.enabled)
			this.enable();

		GL20.glUniformMatrix4fv(this.getUniform(name), false, matrix.getBuffer());
	}
}
