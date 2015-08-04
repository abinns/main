package Graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import Util.Utilities;

public class VertexArray
{

	private int VAO, VBO, IBO, TCBO;

	private int count;

	public VertexArray(float[] vertices, byte[] indices, float[] textureCoordinates)
	{
		this.count = indices.length;

		// this initializes our Vertex Array Object - VAO
		// now all we have to do is create buffer objects
		// for our vertices, indices and textureCoordinates
		// and then bind them to our VAO
		this.VAO = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(this.VAO);

		this.VBO = GL15.glGenBuffers();
		// binds a named buffer object, in this case our
		// VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.VBO);
		// creates and initializes a buffer objects' data store
		// aka it passes in our vertices array into our newly
		// created Vertex Buffer Object.
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, Utilities.createFloatBuffer(vertices), GL15.GL_STATIC_DRAW);
		// This specifies the characteristics of the data we've just passed in
		// Shader.VERTEX_ATTRIB is equal to 0 in this case and it sets the ID of
		// this VBO to 0 or our first position.
		// '3' then represents the 3 points that make up each vertex (x, y, z)
		// 'GL_FLOAT' specifies the type of data
		// and false signifies that we do not want this data to be normalised
		// For the purpose of this tutorial you don't have to worry about the
		// last two parameters.
		GL20.glVertexAttribPointer(Shader.VERTEX_ATTRIB, 3, GL11.GL_FLOAT, false, 0, 0);
		// This line of code enables our vertex attribute array at position 0
		GL20.glEnableVertexAttribArray(0);

		// We follow the same pattern as above for our
		// texture coordinates Vertex Buffer Object.
		this.TCBO = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.TCBO);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, Utilities.createFloatBuffer(textureCoordinates), GL15.GL_STATIC_DRAW);
		// Note this time that we are passing in Shader.TEXTURE_COORDS_ATTRIB
		// which we have defined as the value 1 (the second value in our array
		// which stats
		// at position 0.
		// We also pass in the value 2 instead of 3 as texture coordinates are
		// represented in 2D and not 3D like our vertices.
		GL20.glVertexAttribPointer(Shader.TEXTURE_COORDS_ATTRIB, 2, GL11.GL_FLOAT, false, 0, 0);
		GL20.glEnableVertexAttribArray(Shader.TEXTURE_COORDS_ATTRIB);

		// Again we follow a similar pattern to above
		this.IBO = GL15.glGenBuffers();
		// This time however we are defining our buffer
		// as a GL_ELEMENT_ARRAY_BUFFER instead of our
		// normal GL_ARRAY_BUFFER
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.IBO);
		// We are passing in a byteBuffer object storing all our indices here
		// instead
		// of a floatbuffer as we had above.
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, Utilities.createByteBuffer(indices), GL15.GL_STATIC_DRAW);

		// Lastly we want to unbind all our buffers
		// Another tutorial describes this as
		// unselecting a layer in photoshop
		// so that you can effectively work on other
		// layers.
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	public void bind()
	{
		GL30.glBindVertexArray(this.VAO);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.IBO);
	}

	public void draw()
	{
		GL11.glDrawElements(GL11.GL_TRIANGLES, this.count, GL11.GL_UNSIGNED_BYTE, 0);
	}

	public void render()
	{
		this.bind();
		this.draw();
	}

	public void unbind()
	{
		GL30.glBindVertexArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

}
