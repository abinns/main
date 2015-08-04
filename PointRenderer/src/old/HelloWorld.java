package old;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

import lwjgl.math.Matrix4f;
import lwjgl.math.Vector3f;

public class HelloWorld
{

	private int					vertexBufferID;
	private int					colourBufferID;
	private int					numberIndices	= 0;
	// We need to strongly reference callback instances.
	private GLFWErrorCallback	errorCallback;
	private GLFWKeyCallback		keyCallback;

	// The window handle
	private long			window;
	private MouseHandler	mouseCallback;

	private Vector3f position = new Vector3f();

	// Not restricted to FloatBuffer
	private void bindArrayBuffer(int id, FloatBuffer buffer)
	{
		// Bind buffer (also specifies type of buffer)
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
		// Send up the data and specify usage hint.
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}

	private int createBuffer()
	{
		IntBuffer buffer = BufferUtils.createIntBuffer(1);
		GL15.glGenBuffers(buffer);
		return buffer.get(0);
	}

	private void init()
	{
		// Setup an error callback. The default implementation will print the
		// error message in System.err.
		GLFW.glfwSetErrorCallback(this.errorCallback = Callbacks.errorCallbackPrint(System.err));

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (GLFW.glfwInit() != GL11.GL_TRUE)
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure our window
		GLFW.glfwDefaultWindowHints();
		// optional, the current window hints are already the default
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
		// the window will stay hidden after creation
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);
		// the window will be resizable

		int WIDTH = 800;
		int HEIGHT = 600;

		// Create the window
		this.window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", MemoryUtil.NULL, MemoryUtil.NULL);
		if (this.window == MemoryUtil.NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed,
		// repeated or released.
		GLFW.glfwSetKeyCallback(this.window, new KeyboardHandler());

		GLFW.glfwSetCursorPosCallback(this.window, this.mouseCallback = new MouseHandler());

		Matrix4f pr_matrix = Matrix4f.orthographic(-10.0f, 10.0f, -10.0f * 9.0f / 16.0f, 10.0f * 9.0f / 16.0f, -10.0f, 10.0f);

		// Get the resolution of the primary monitor
		ByteBuffer vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		// Center our window
		GLFW.glfwSetWindowPos(this.window, (GLFWvidmode.width(vidmode) - WIDTH) / 2, (GLFWvidmode.height(vidmode) - HEIGHT) / 2);

		// Make the OpenGL context current
		GLFW.glfwMakeContextCurrent(this.window);
		// Enable v-sync
		GLFW.glfwSwapInterval(1);

		// Make the window visible
		GLFW.glfwShowWindow(this.window);

		GLContext.createFromCurrent();

		this.initBuffers();

	}

	private void initBuffers()
	{
		int size = 10;
		// TODO
		this.vertexBufferID = this.createBuffer();
		this.colourBufferID = this.createBuffer();

		FloatBuffer vertexData = BufferUtils.createFloatBuffer(size * 3);
		for (int i = 0; i < size; i++)
			for (int j = 0; j < 3; j++)
				vertexData.put(-i);
		this.bindArrayBuffer(this.vertexBufferID, vertexData);

		FloatBuffer colorData = BufferUtils.createFloatBuffer(size * 3);
		for (int i = 0; i < size; i++)
			for (int j = 0; j < 3; j++)
				colorData.put(1.0f);
		this.bindArrayBuffer(this.colourBufferID, colorData);

	}

	private void loop()
	{
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the ContextCapabilities instance and makes the OpenGL
		// bindings available for use.

		// Set the clear color
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while (GLFW.glfwWindowShouldClose(this.window) == GL11.GL_FALSE)
		{
			// Shader.shader1.setUniformMat4f("vw_matrix",
			// Matrix4f.translate(camera.position));
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			// clear the framebuffer

			this.renderVBO();
			GLFW.glfwSwapBuffers(this.window); // swap the color buffers
			this.renderVBO();

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			GLFW.glfwPollEvents();
		}
	}

	private void renderVBO()
	{
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBufferID);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.colourBufferID);
		GL11.glColorPointer(4, GL11.GL_FLOAT, 0, 0);

		// If you are not using IBOs:
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.numberIndices);

		// The alternate glDrawElements.
		// GL12.glDrawRangeElements(GL11.GL_TRIANGLES, 0, maxIndex,
		// this.numberIndices, GL11.GL_UNSIGNED_INT, 0);
	}

	public void run()
	{
		System.out.println("Hello LWJGL " + Sys.getVersion() + "!");

		try
		{
			this.init();
			this.loop();

			// Release window and window callbacks
			GLFW.glfwDestroyWindow(this.window);
			this.keyCallback.release();
		} finally
		{
			// Terminate GLFW and release the GLFWerrorfun
			GLFW.glfwTerminate();
			this.errorCallback.release();
		}
	}

	public void update()
	{
		if (KeyboardHandler.isKeyDown(GLFW.GLFW_KEY_W))
			this.position.y += 0.05f;
		if (KeyboardHandler.isKeyDown(GLFW.GLFW_KEY_S))
			this.position.y -= 0.05f;
		if (KeyboardHandler.isKeyDown(GLFW.GLFW_KEY_D))
			this.position.x += 0.05f;
		if (KeyboardHandler.isKeyDown(GLFW.GLFW_KEY_A))
			this.position.x -= 0.05f;
	}

}