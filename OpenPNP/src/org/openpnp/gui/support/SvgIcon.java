/*
 * SVGIcon.java
 *
 * A Swing Icon that draws an SVG image.
 *
 * Cameron McCormack <cam (at) mcc.id.au>
 *
 * Permission is hereby granted to use, copy, modify and distribte this code for any purpose,
 * without fee.
 *
 * Initial version: April 21, 2005
 */

package org.openpnp.gui.support;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.GrayFilter;
import javax.swing.Icon;

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

/**
 * A Swing Icon that draws an SVG image.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 */
public class SvgIcon implements Icon
{

	/**
	 * A transcoder that generates a BufferedImage.
	 */
	protected class BufferedImageTranscoder extends ImageTranscoder
	{

		/**
		 * The BufferedImage generated from the SVG document.
		 */
		protected BufferedImage bufferedImage;

		/**
		 * Creates a new ARGB image with the specified dimension.
		 * 
		 * @param width
		 *            the image width in pixels
		 * @param height
		 *            the image height in pixels
		 */
		@Override
		public BufferedImage createImage(int width, int height)
		{
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}

		/**
		 * Returns the BufferedImage generated from the SVG document.
		 */
		public BufferedImage getBufferedImage()
		{
			return this.bufferedImage;
		}

		/**
		 * Set the dimensions to be used for the image.
		 */
		public void setDimensions(int w, int h)
		{
			this.hints.put(SVGAbstractTranscoder.KEY_WIDTH, new Float(w));
			this.hints.put(SVGAbstractTranscoder.KEY_HEIGHT, new Float(h));
		}

		/**
		 * Writes the specified image to the specified output.
		 * 
		 * @param img
		 *            the image to write
		 * @param output
		 *            the output where to store the image
		 * @param TranscoderException
		 *            if an error occured while storing the image
		 */
		@Override
		public void writeImage(BufferedImage img, TranscoderOutput output) throws TranscoderException
		{
			this.bufferedImage = img;
		}
	}

	/**
	 * The BufferedImage generated from the SVG document.
	 */
	protected BufferedImage bufferedImage;

	protected Image bufferedImageDisabled;

	/**
	 * The width of the rendered image.
	 */
	protected int width;

	/**
	 * The height of the rendered image.
	 */
	protected int height;

	/**
	 * Create a new SVGIcon object.
	 * 
	 * @param uri
	 *            The URI to read the SVG document from.
	 */
	public SvgIcon(URL url)
	{
		this(url, 24, 24);
	}

	/**
	 * Create a new SVGIcon object.
	 * 
	 * @param uri
	 *            The URI to read the SVG document from.
	 */
	public SvgIcon(URL url, int width, int height)
	{
		try
		{
			this.generateBufferedImage(new TranscoderInput(url.toString()), width, height);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// Icon //////////////////////////////////////////////////////////////////

	/**
	 * Generate the BufferedImage.
	 */
	protected void generateBufferedImage(TranscoderInput in, int w, int h) throws TranscoderException
	{
		BufferedImageTranscoder t = new BufferedImageTranscoder();
		if (w != 0 && h != 0)
			t.setDimensions(w, h);
		t.transcode(in, null);
		this.bufferedImage = t.getBufferedImage();
		this.bufferedImageDisabled = GrayFilter.createDisabledImage(this.bufferedImage);
		this.width = this.bufferedImage.getWidth();
		this.height = this.bufferedImage.getHeight();
	}

	/**
	 * Returns the icon's height.
	 */
	@Override
	public int getIconHeight()
	{
		return this.height;
	}

	/**
	 * Returns the icon's width.
	 */
	@Override
	public int getIconWidth()
	{
		return this.width;
	}

	/**
	 * Draw the icon at the specified location.
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Image image = this.bufferedImage;
		if (c != null && !c.isEnabled())
			image = this.bufferedImageDisabled;
		g.drawImage(image, x, y, null);
	}
}
