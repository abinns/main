package Graphics;

import org.lwjgl.glfw.GLFW;

import Input.Input;
import Math.Matrix4f;
import Math.Vector3f;

public class Camera
{

	public Matrix4f	cameraMat	= new Matrix4f();
	public Vector3f	position	= new Vector3f();

	float			rot	= 0.0f;
	private float	pitch;
	private float	yaw;
	private float	roll;

	public Camera(Matrix4f cameraMat)
	{
		this.cameraMat = cameraMat;
	}

	public Matrix4f getCameraMat()
	{
		return this.cameraMat;
	}

	public Matrix4f getMatrix()
	{
		return this.cameraMat;
	}

	public float getPitch()
	{
		return this.pitch;
	}

	public Vector3f getPosition()
	{
		return this.position;
	}

	public float getRoll()
	{
		return this.roll;
	}

	public float getRot()
	{
		return this.rot;
	}

	public float getYaw()
	{
		return this.yaw;
	}

	public void render()
	{

		Shader.shader1.enable();

		// Uncomment different lines to see different rotation effects
		// Shader.shader1.setUniformMat4f("ml_matrix",
		// Matrix4f.translate(position).multiply(Matrix4f.rotateX(rot)));
		// Shader.shader1.setUniformMat4f("ml_matrix",
		// Matrix4f.translate(position).multiply(Matrix4f.rotateY(rot)));
		Shader.shader1.setUniformMat4f("vw_matrix", Matrix4f.translate(new Vector3f(-this.position.x, -this.position.y, -this.position.z))
				.multiply(Matrix4f.rotateZ(this.roll).multiply(Matrix4f.rotateY(this.yaw)).multiply(Matrix4f.rotateX(this.pitch))));

		Shader.shader1.disable();

	}

	public void setCameraMat(Matrix4f cameraMat)
	{
		this.cameraMat = cameraMat;
	}

	public void setPosition(Vector3f pos)
	{
		this.position = pos;
	}

	public Matrix4f setupViewMatrix()
	{
		Matrix4f viewMatrix = new Matrix4f();
		viewMatrix = Matrix4f.identity();

		Vector3f negativeCameraPos = new Vector3f(-this.position.x, -this.position.y, -this.position.z);

		Matrix4f.translate(negativeCameraPos);
		return viewMatrix;

	}

	public void update()
	{
		if (Input.isKeyDown(GLFW.GLFW_KEY_W))
			this.position.y += 0.05f;
		if (Input.isKeyDown(GLFW.GLFW_KEY_S))
			this.position.y -= 0.05f;
		if (Input.isKeyDown(GLFW.GLFW_KEY_D))
			this.position.x += 0.05f;
		if (Input.isKeyDown(GLFW.GLFW_KEY_A))
			this.position.x -= 0.05f;
	}

}
