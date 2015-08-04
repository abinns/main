package Graphics;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import Util.Utilities;

public class Texture
{

	private int	width, height;
	private int	texture;

	public Texture(String path)
	{
		this.texture = this.load(path);
	}

	public void bind()
	{
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
	}

	private int load(String path)
	{
		int[] pixels = null;
		try
		{
			BufferedImage image = ImageIO.read(new FileInputStream(path));
			this.width = image.getWidth();
			this.height = image.getHeight();
			pixels = new int[this.width * this.height];
			image.getRGB(0, 0, this.width, this.height, pixels, 0, this.width);
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		int[] data = new int[this.width * this.height];
		for (int i = 0; i < this.width * this.height; i++)
		{
			int a = (pixels[i] & 0xff000000) >> 24;
			int r = (pixels[i] & 0xff0000) >> 16;
			int g = (pixels[i] & 0xff00) >> 8;
			int b = pixels[i] & 0xff;

			data[i] = a << 24 | b << 16 | g << 8 | r;
		}

		int result = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, result);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, this.width, this.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, Utilities.createIntBuffer(data));

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		return result;
	}

	public void setTex(int i)
	{
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
	}

	public void unbind()
	{
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

}
