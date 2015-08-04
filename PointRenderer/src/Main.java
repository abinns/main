import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;

import Graphics.Camera;
import Graphics.Shader;
import Input.Input;
import Math.Matrix4f;
import Tutorial.Crate;
import lwjgl.math.Vector3f;

public class Main implements Runnable
{

	public static void main(String args[])
	{
		Main game = new Main();
		game.start();
	}

	private Thread thread;

	private boolean running = true;

	private long window;

	private int width = 800, height = 800;

	private GLFWKeyCallback	keyCallback;
	private Crate			crate1;

	private Cloud cloud1;

	public Camera			camera	= new Camera(new Matrix4f());

	public void init()
	{
		// Initializes our window creator library - GLFW
		// This basically means, if this glfwInit() doesn't run properlly
		// print an error to the console
		if (GLFW.glfwInit() != GL11.GL_TRUE)
			// Throw an error.
			System.err.println("GLFW initialization failed!");

		// Allows our window to be resizable
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);

		// Creates our window. You'll need to declare private long window at the
		// top of the class though.
		// We pass the width and height of the game we want as well as the title
		// for
		// the window. The last 2 NULL parameters are for more advanced uses and
		// you
		// shouldn't worry about them right now.
		this.window = GLFW.glfwCreateWindow(this.width, this.height, "Algebra Tutorials", MemoryUtil.NULL, MemoryUtil.NULL);

		// This code performs the appropriate checks to ensure that the
		// window was successfully created.
		// If not then it prints an error to the console
		if (this.window == MemoryUtil.NULL)
			// Throw an Error
			System.err.println("Could not create our Window!");

		GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		// Sets the initial position of our game window.
		GLFW.glfwSetWindowPos(this.window, 100, 100);

		// Sets our keycallback to equal our newly created Input class()
		GLFW.glfwSetKeyCallback(this.window, this.keyCallback = new Input());

		// Sets the context of GLFW, this is vital for our program to work.
		GLFW.glfwMakeContextCurrent(this.window);
		// finally shows our created window in all it's glory.
		GLFW.glfwShowWindow(this.window);

		// In order to perform OpenGL rendering, a context must be "made
		// current"
		// we can do this by using this line of code:
		GLContext.createFromCurrent();

		// Clears color buffers and gives us a nice color background.
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		GL13.glActiveTexture(GL13.GL_TEXTURE1);

		// Enables depth testing which will be important to make sure
		// triangles are not rendering in front of each other when they
		// shouldn't be.
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		// Prints out the current OpenGL version to the console.
		System.out.println("OpenGL: " + GL11.glGetString(GL11.GL_VERSION));

		Shader.loadAll();

		Shader.shader1.enable();
		Matrix4f pr_matrix = Matrix4f.orthographic(-10.0f, 10.0f, -10.0f * 9.0f / 16.0f, 10.0f * 9.0f / 16.0f, -10.0f, 10.0f);
		// Matrix4f pr_matrix = Matrix4f.perspective(10.0f, 10.0f, 10.0f, 10.0f,
		// -1.0f, 100.0f, 15.0f, (float)width/(float)height);
		Shader.shader1.setUniformMat4f("vw_matrix", Matrix4f.translate(this.camera.position));
		Shader.shader1.setUniformMat4f("pr_matrix", pr_matrix);
		Shader.shader1.setUniform1i("tex", 1);

		Shader.shader1.disable();

		this.crate1 = new Crate();

		this.crate1.translate(new Vector3f(0, 0.75f, 0.0f));
		this.cloud1 = new Cloud();
	}

	public void render()
	{
		//
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		this.camera.render();

		this.crate1.render();

		this.cloud1.render();

		int i = GL11.glGetError();
		if (i != GL11.GL_NO_ERROR)
			System.out.println(i);

		// Swaps out our bufferss
		GLFW.glfwSwapBuffers(this.window);
	}

	@Override
	public void run()
	{
		// All our initialization code
		this.init();
		// Our main game loop

		long lastTime = System.nanoTime();
		double delta = 0.0;
		double ns = 1000000000.0 / 60.0;
		long timer = System.currentTimeMillis();
		int updates = 0;
		int frames = 0;
		while (this.running)
		{
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if (delta >= 1.0)
			{
				this.update();
				updates++;
				delta--;
			}
			this.render();
			frames++;
			if (System.currentTimeMillis() - timer > 1000)
			{
				timer += 1000;
				System.out.println(updates + " ups, " + frames + " fps");
				updates = 0;
				frames = 0;
			}
			if (GLFW.glfwWindowShouldClose(this.window) == GL11.GL_TRUE)
				this.running = false;
		}

		this.keyCallback.release();
		GLFW.glfwDestroyWindow(this.window);
		GLFW.glfwTerminate();
	}

	public void start()
	{
		this.running = true;
		this.thread = new Thread(this, "AlgebraTuts");
		this.thread.start();
	}

	public void update()
	{
		// Polls for any window events such as the window closing etc.
		GLFW.glfwPollEvents();

		this.camera.update();

	}

}
