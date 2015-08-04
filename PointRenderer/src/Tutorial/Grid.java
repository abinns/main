
package Tutorial;

import org.lwjgl.glfw.GLFW;

import Graphics.Shader;
import Input.Input;
import Math.Matrix4f;
import Math.Vector3f;

public class Grid extends GameObject
{

	public static float		width		= 3.5f;
	public static float		height		= 3.5f;
	private static float[]	vertices	= new float[]
											{ -Grid.width, 0.2f, -Grid.height, -Grid.width, 0.2f, Grid.height, Grid.width, 0.2f, Grid.height, Grid.width, 0.2f, -Grid.height };

	private static float[]	texCoords	= new float[]
											{ 0, 1, 0, 0, 1, 0, 1, 1 };
	private static byte[]	indices		= new byte[]
											{ 0, 1, 2, 2, 3, 0 };
	private static String	texPath		= "assets/grid.png";
	public Vector3f			delta		= new Vector3f();

	public boolean	running	= false;
	public boolean	jumping	= false;
	public boolean	idle	= true;

	public boolean walking = false;

	public int spritePos = 0;

	public int counter = 0;

	public int animState = 0;

	public float rot = 0.0f;

	public Grid()
	{
		super(Grid.vertices, Grid.indices, Grid.texCoords, Grid.texPath);
	}

	@Override
	public void render()
	{

		this.tex.bind();
		Shader.shader1.enable();
		// Uncomment different lines to see different rotation effects
		// Shader.shader1.setUniformMat4f("ml_matrix",
		// Matrix4f.translate(position).multiply(Matrix4f.rotateX(rot)));
		// Shader.shader1.setUniformMat4f("ml_matrix",
		// Matrix4f.translate(position).multiply(Matrix4f.rotateY(rot)));
		Shader.shader1.setUniformMat4f("ml_matrix", Matrix4f.translate(this.position).multiply(Matrix4f.rotateZ(this.rot)));
		this.VAO.render();
		Shader.shader1.disable();
		this.tex.unbind();

	}

	@Override
	public void update()
	{
		// rot += 0.1f;
		// Uncomment different lines to see different translation effects
		// position.x += 0.01f;
		// position.y += 0.01f;
		if (Input.isKeyDown(GLFW.GLFW_KEY_SPACE))
			this.position.z += 0.1f;
	}

}
