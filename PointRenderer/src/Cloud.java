import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class Cloud
{

	private int	vertexBufferID;
	private int	colourBufferID;
	private int	numberIndices;

	public Cloud()
	{
		this(new float[]
		{ 0, 1, 0, 0, 1, 0, 0, 1, 0 });
	}

	public Cloud(float[] points)
	{
		this.vertexBufferID = createVBOID();
		this.colourBufferID = createVBOID();
		this.numberIndices = points.length / 3;

		FloatBuffer verticies = BufferUtils.createFloatBuffer(points.length);
		verticies.put(points);
		// verticies.rewind();
		vertexBufferData(vertexBufferID, verticies);
	}

	private int createVBOID()
	{
		IntBuffer buffer = BufferUtils.createIntBuffer(1);
		GL15.glGenBuffers(buffer);
		return buffer.get(0);
	}
	// Not restricted to FloatBuffer

	private void vertexBufferData(int id, FloatBuffer buffer)
	{
		// Bind buffer (also specifies type of buffer)
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
		// Send up the data and specify usage hint.
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}

	public void render()
	{
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferID);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);


		// If you are not using IBOs:
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numberIndices);
	}

}
