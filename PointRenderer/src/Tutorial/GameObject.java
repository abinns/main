package Tutorial;

import Graphics.Shader;
import Graphics.Texture;
import Graphics.VertexArray;
import Math.Matrix4f;
import Math.Vector3f;

public class GameObject
{

	public static Shader	shader;
	public VertexArray		VAO;
	public Texture			tex;
	public float[]			vertices, texCoords;
	public byte[]			indices;

	public Vector3f position = new Vector3f();

	public float delta = 0.01f;

	public GameObject(float[] vertices, byte[] indices, float[] texCoords, String texPath)
	{
		this.vertices = vertices;
		this.indices = indices;
		this.texCoords = texCoords;
		this.tex = new Texture(texPath);
		this.VAO = new VertexArray(this.vertices, this.indices, this.texCoords);
	}

	public void loadShader()
	{
		GameObject.shader = new Shader("shaders/bg.vert", "shaders/bg.frag");
	}

	public void render()
	{
		// TODO Auto-generated method stub
		this.tex.bind();
		Shader.shader1.enable();
		Shader.shader1.setUniformMat4f("ml_matrix", Matrix4f.translate(this.position));
		this.VAO.render();
		Shader.shader1.disable();
		this.tex.unbind();

	}

	public void sinUpdate()
	{
		this.position.y += (float) Math.sin(this.delta) / 105.0f;
	}

	public void translate(lwjgl.math.Vector3f vector3f)
	{
		this.position.x += vector3f.x;
		this.position.y += vector3f.y;
		this.position.z += vector3f.z;
	}

	public void update()
	{
		// our default update function
	}

}
