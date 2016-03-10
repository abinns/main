/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.gui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.openpnp.CameraListener;
import org.openpnp.gui.components.reticle.Reticle;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.spi.Camera;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;
import org.openpnp.util.XmlSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class CameraView extends JComponent implements CameraListener
{
	private enum HandlePosition
	{
		NW, N, NE, E, SE, S, SW, W
	}

	private enum SelectionMode
	{
		Resizing, Moving, Creating
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger logger = LoggerFactory.getLogger(CameraView.class);

	private static final String PREF_RETICLE = "CamerView.reticle";

	private static final String DEFAULT_RETICLE_KEY = "DEFAULT_RETICLE_KEY";

	private final static int HANDLE_DIAMETER = 8;

	private static float[] selectionDashProfile = new float[]
	{ 6f, 6f };

	// 11 is the sum of the dash lengths minus 1.
	private static float selectionDashPhaseStart = 11f;

	/**
	 * Draws a standard handle centered on the given x and y position.
	 * 
	 * @param g2d
	 * @param x
	 * @param y
	 */
	private static void drawHandle(Graphics2D g2d, int x, int y)
	{
		g2d.setStroke(new BasicStroke(1f));
		g2d.setColor(new Color(153, 153, 187));
		g2d.fillArc(x - CameraView.HANDLE_DIAMETER / 2, y - CameraView.HANDLE_DIAMETER / 2, CameraView.HANDLE_DIAMETER, CameraView.HANDLE_DIAMETER, 0, 360);
		g2d.setColor(Color.white);
		g2d.drawArc(x - CameraView.HANDLE_DIAMETER / 2, y - CameraView.HANDLE_DIAMETER / 2, CameraView.HANDLE_DIAMETER, CameraView.HANDLE_DIAMETER, 0, 360);
	}

	private static void drawImageInfo(Graphics2D g2d, int topLeftX, int topLeftY, BufferedImage image)
	{
		if (image == null)
			return;
		String text = String.format("Resolution: %d x %d\nHistogram:", image.getWidth(), image.getHeight());
		Insets insets = new Insets(10, 10, 10, 10);
		int interLineSpacing = 4;
		int cornerRadius = 8;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.setFont(g2d.getFont().deriveFont(12.0f));
		String[] lines = text.split("\n");
		List<TextLayout> textLayouts = new ArrayList<>();
		int textWidth = 0, textHeight = 0;
		for (String line : lines)
		{
			TextLayout textLayout = new TextLayout(line, g2d.getFont(), g2d.getFontRenderContext());
			textWidth = (int) Math.max(textWidth, textLayout.getBounds().getWidth());
			textHeight += (int) textLayout.getBounds().getHeight() + interLineSpacing;
			textLayouts.add(textLayout);
		}
		textHeight -= interLineSpacing;

		int histogramHeight = 50 + 2;
		int histogramWidth = 255 + 2;

		int width = Math.max(textWidth, histogramWidth);
		int height = textHeight + histogramHeight;

		g2d.setColor(new Color(0, 0, 0, 0.75f));
		g2d.fillRoundRect(topLeftX, topLeftY, width + insets.left + insets.right, height + insets.top + insets.bottom, cornerRadius, cornerRadius);
		g2d.setColor(Color.white);
		g2d.drawRoundRect(topLeftX, topLeftY, width + insets.left + insets.right, height + insets.top + insets.bottom, cornerRadius, cornerRadius);
		int yPen = topLeftY + insets.top;
		for (TextLayout textLayout : textLayouts)
		{
			yPen += textLayout.getBounds().getHeight();
			textLayout.draw(g2d, topLeftX + insets.left, yPen);
			yPen += interLineSpacing;
		}

		g2d.setColor(new Color(1, 1, 1, 0.20f));
		g2d.fillRect(topLeftX + insets.left, yPen, histogramWidth, histogramHeight);

		// Calculate the histogram
		long[][] histogram = new long[3][256];
		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++)
			{
				int rgb = image.getRGB(x, y);
				int r = rgb >> 16 & 0xff;
				int g = rgb >> 8 & 0xff;
				int b = rgb >> 0 & 0xff;
				histogram[0][r]++;
				histogram[1][g]++;
				histogram[2][b]++;
			}
		// find the highest value in the histogram
		long maxVal = 0;
		for (int channel = 0; channel < 3; channel++)
			for (int bucket = 0; bucket < 256; bucket++)
				maxVal = Math.max(maxVal, histogram[channel][bucket]);
		// and scale it to 50 pixels tall
		double scale = 50.0 / maxVal;
		Color[] colors = new Color[]
		{ Color.red, Color.green, Color.blue };
		for (int channel = 0; channel < 3; channel++)
		{
			g2d.setColor(colors[channel]);
			for (int bucket = 0; bucket < 256; bucket++)
			{
				int value = (int) (histogram[channel][bucket] * scale);
				g2d.drawLine(topLeftX + insets.left + 1 + bucket, yPen + 1 + 50 - value, topLeftX + insets.left + 1 + bucket, yPen + 1 + 50 - value);
			}
		}
	}

	/**
	 * Draws text in a nice bubble at the given position. Newline characters in
	 * the text cause line breaks.
	 * 
	 * @param g2d
	 * @param topLeftX
	 * @param topLeftY
	 * @param text
	 */
	private static void drawTextOverlay(Graphics2D g2d, int topLeftX, int topLeftY, String text)
	{
		Insets insets = new Insets(10, 10, 10, 10);
		int interLineSpacing = 4;
		int cornerRadius = 8;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.setFont(g2d.getFont().deriveFont(12.0f));
		String[] lines = text.split("\n");
		List<TextLayout> textLayouts = new ArrayList<>();
		int textWidth = 0, textHeight = 0;
		for (String line : lines)
		{
			TextLayout textLayout = new TextLayout(line, g2d.getFont(), g2d.getFontRenderContext());
			textWidth = (int) Math.max(textWidth, textLayout.getBounds().getWidth());
			textHeight += (int) textLayout.getBounds().getHeight() + interLineSpacing;
			textLayouts.add(textLayout);
		}
		textHeight -= interLineSpacing;
		g2d.setColor(new Color(0, 0, 0, 0.75f));
		g2d.fillRoundRect(topLeftX, topLeftY, textWidth + insets.left + insets.right, textHeight + insets.top + insets.bottom, cornerRadius, cornerRadius);
		g2d.setColor(Color.white);
		g2d.drawRoundRect(topLeftX, topLeftY, textWidth + insets.left + insets.right, textHeight + insets.top + insets.bottom, cornerRadius, cornerRadius);
		int yPen = topLeftY + insets.top;
		for (TextLayout textLayout : textLayouts)
		{
			yPen += textLayout.getBounds().getHeight();
			textLayout.draw(g2d, topLeftX + insets.left, yPen);
			yPen += interLineSpacing;
		}
	}

	public static Cursor getCursorForHandlePosition(HandlePosition handlePosition)
	{
		switch (handlePosition)
		{
			case NW:
				return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
			case N:
				return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
			case NE:
				return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
			case E:
				return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
			case SE:
				return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
			case S:
				return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
			case SW:
				return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
			case W:
				return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
		}
		return null;
	}

	/**
	 * Changes the HandlePosition to it's inverse if the given rectangle has a
	 * negative width, height or both.
	 * 
	 * @param r
	 */
	private static HandlePosition getOpposingHandle(Rectangle r, HandlePosition handlePosition)
	{
		if (r.getWidth() < 0 && r.getHeight() < 0)
		{
			if (handlePosition == HandlePosition.NW)
				return HandlePosition.SE;
			else if (handlePosition == HandlePosition.NE)
				return HandlePosition.SW;
			else if (handlePosition == HandlePosition.SE)
				return HandlePosition.NW;
			else if (handlePosition == HandlePosition.SW)
				return HandlePosition.NE;
		} else if (r.getWidth() < 0)
		{
			if (handlePosition == HandlePosition.NW)
				return HandlePosition.NE;
			else if (handlePosition == HandlePosition.NE)
				return HandlePosition.NW;
			else if (handlePosition == HandlePosition.SE)
				return HandlePosition.SW;
			else if (handlePosition == HandlePosition.SW)
				return HandlePosition.SE;
			else if (handlePosition == HandlePosition.E)
				return HandlePosition.W;
			else if (handlePosition == HandlePosition.W)
				return HandlePosition.E;
		} else if (r.getHeight() < 0)
			if (handlePosition == HandlePosition.SW)
				return HandlePosition.NW;
			else if (handlePosition == HandlePosition.SE)
				return HandlePosition.NE;
			else if (handlePosition == HandlePosition.NW)
				return HandlePosition.SW;
			else if (handlePosition == HandlePosition.NE)
				return HandlePosition.SE;
			else if (handlePosition == HandlePosition.S)
				return HandlePosition.N;
			else if (handlePosition == HandlePosition.N)
				return HandlePosition.S;
		return handlePosition;
	}

	private static boolean isWithin(int pointX, int pointY, int boundsX, int boundsY, int boundsWidth, int boundsHeight)
	{
		return pointX >= boundsX && pointX <= boundsX + boundsWidth && pointY >= boundsY && pointY <= boundsY + boundsHeight;
	}

	/**
	 * A specialization of isWithin() that uses uses the bounding box of a
	 * handle.
	 * 
	 * @param x
	 * @param y
	 * @param handleX
	 * @param handleY
	 * @return
	 */
	private static boolean isWithinHandle(int x, int y, int handleX, int handleY)
	{
		return CameraView.isWithin(x, y, handleX - 4, handleY - 4, 8, 8);
	}

	/**
	 * Builds a rectangle with the given parameters. If the width or height is
	 * negative the corresponding x or y value is modified and the width or
	 * height is made positive.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	private static Rectangle normalizeRectangle(int x, int y, int width, int height)
	{
		if (width < 0)
		{
			width *= -1;
			x -= width;
		}
		if (height < 0)
		{
			height *= -1;
			y -= height;
		}
		return new Rectangle(x, y, width, height);
	}

	private static Rectangle normalizeRectangle(Rectangle r)
	{
		return CameraView.normalizeRectangle(r.x, r.y, r.width, r.height);
	}

	/**
	 * The Camera we are viewing.
	 */
	private Camera camera;

	/**
	 * The last frame received, reported by the Camera.
	 */
	private BufferedImage					lastFrame;
	/**
	 * The maximum frames per second that we'll display.
	 */
	private int								maximumFps;
	private LinkedHashMap<Object, Reticle>	reticles	= new LinkedHashMap<>();
	private JPopupMenu						popupMenu;
	/**
	 * The last width and height of the component that we painted for. If the
	 * width or height is different from these values at the start of paint
	 * we'll recalculate all the scaling data.
	 */
	private double							lastWidth, lastHeight;
	/**
	 * The last width and height of the image that we painted for. If the width
	 * or height is different from these values at the start of paint we'll
	 * recalculate all the scaling data.
	 */
	private double							lastSourceWidth, lastSourceHeight;
	private Location						lastUnitsPerPixel;
	/**
	 * The width and height of the image after it has been scaled to fit the
	 * bounds of the component.
	 */
	private int								scaledWidth, scaledHeight;
	/**
	 * The ratio of scaled width and height to unscaled width and height.
	 * scaledWidth * scaleRatioX = sourceWidth. scaleRatioX = sourceWidth /
	 * scaledWidth
	 */
	private double							scaleRatioX, scaleRatioY;
	/**
	 * The Camera's units per pixel scaled at the same ratio as the image. That
	 * is, each pixel in the scaled image is scaledUnitsPerPixelX wide and
	 * scaledUnitsPerPixelY high.
	 */
	private double							scaledUnitsPerPixelX, scaledUnitsPerPixelY;

	/**
	 * The top left position within the component at which the scaled image can
	 * be drawn for it to be centered.
	 */
	private int imageX, imageY;

	private boolean selectionEnabled;

	/**
	 * Rectangle describing the bounds of the selection in image coordinates.
	 */
	private Rectangle selection;

	/**
	 * The scaled version of the selection Rectangle. Rescaled any time the
	 * component's size is changed.
	 */
	private Rectangle selectionScaled;

	private SelectionMode selectionMode;

	private HandlePosition selectionActiveHandle;

	private int selectionStartX, selectionStartY;

	private float	selectionFlashOpacity;
	private float	selectionDashPhase;

	private CameraViewSelectionTextDelegate selectionTextDelegate;

	private ScheduledExecutorService scheduledExecutor;

	private Preferences prefs = Preferences.userNodeForPackage(CameraView.class);

	private String text;

	private boolean showImageInfo;

	private List<CameraViewActionListener> actionListeners = new ArrayList<>();

	private CameraViewFilter cameraViewFilter;

	private long flashStartTimeMs;

	private long flashLengthMs = 250;

	private MouseListener mouseListener = new MouseAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.isPopupTrigger() || e.isShiftDown() || SwingUtilities.isRightMouseButton(e))
				return;
			// double click captures an image from the camera and writes it to
			// disk.
			if (e.getClickCount() == 2)
				CameraView.this.captureSnapshot();
			else
				CameraView.this.fireActionEvent(e);
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			if (e.isPopupTrigger())
			{
				CameraView.this.popupMenu.show(e.getComponent(), e.getX(), e.getY());
				return;
			} else if (e.isShiftDown())
				CameraView.this.moveToClick(e);
			else if (CameraView.this.selectionEnabled)
				CameraView.this.beginSelection(e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (e.isPopupTrigger())
			{
				CameraView.this.popupMenu.show(e.getComponent(), e.getX(), e.getY());
				return;
			} else
				CameraView.this.endSelection();
		}
	};

	private MouseMotionListener mouseMotionListener = new MouseMotionAdapter()
	{
		@Override
		public void mouseDragged(MouseEvent e)
		{
			if (CameraView.this.selectionEnabled)
				CameraView.this.continueSelection(e);
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			CameraView.this.updateCursor();
		}
	};

	private ComponentListener componentListener = new ComponentAdapter()
	{
		@Override
		public void componentResized(ComponentEvent e)
		{
			CameraView.this.calculateScalingData();
		}
	};

	public CameraViewSelectionTextDelegate pixelsAndUnitsTextSelectionDelegate = new CameraViewSelectionTextDelegate()
	{
		@Override
		public String getSelectionText(CameraView cameraView)
		{
			double widthInUnits = CameraView.this.selection.width * CameraView.this.camera.getUnitsPerPixel().getX();
			double heightInUnits = CameraView.this.selection.height * CameraView.this.camera.getUnitsPerPixel().getY();

			String text = String.format(Locale.US, "%dpx, %dpx\n%2.3f%s, %2.3f%s", (int) CameraView.this.selection.getWidth(), (int) CameraView.this.selection.getHeight(), widthInUnits,
					CameraView.this.camera.getUnitsPerPixel().getUnits().getShortName(), heightInUnits, CameraView.this.camera.getUnitsPerPixel().getUnits().getShortName());
			return text;
		}
	};

	public CameraView()
	{
		this.setBackground(Color.black);
		this.setOpaque(true);

		String reticlePref = this.prefs.get(CameraView.PREF_RETICLE, null);
		try
		{
			Reticle reticle = (Reticle) XmlSerialize.deserialize(reticlePref);
			this.setDefaultReticle(reticle);
		} catch (Exception e)
		{
			// logger.warn("Warning: Unable to load Reticle preference");
		}

		this.popupMenu = new CameraViewPopupMenu(this);

		this.addMouseListener(this.mouseListener);
		this.addMouseMotionListener(this.mouseMotionListener);
		this.addComponentListener(this.componentListener);

		this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

		// TODO: Cancel this when it's not being used instead of spinning,
		// or maybe create a real thread and wait().
		this.scheduledExecutor.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				if (CameraView.this.selectionEnabled && CameraView.this.selection != null)
				{
					// Adjust the dash phase so the line marches on the next
					// paint
					CameraView.this.selectionDashPhase -= 1f;
					if (CameraView.this.selectionDashPhase < 0)
						CameraView.this.selectionDashPhase = CameraView.selectionDashPhaseStart;
					CameraView.this.repaint();
				}
			}
		}, 0, 50, TimeUnit.MILLISECONDS);
	}

	public CameraView(int maximumFps)
	{
		this();
		this.setMaximumFps(maximumFps);
	}

	public void addActionListener(CameraViewActionListener listener)
	{
		if (!this.actionListeners.contains(listener))
			this.actionListeners.add(listener);
	}

	private void beginSelection(MouseEvent e)
	{
		// If we're not doing anything currently, we can start
		// a new operation.
		if (this.selectionMode == null)
		{
			int x = e.getX();
			int y = e.getY();

			// See if there is a handle under the cursor.
			HandlePosition handlePosition = this.getSelectionHandleAtPosition(x, y);
			if (handlePosition != null)
			{
				this.selectionMode = SelectionMode.Resizing;
				this.selectionActiveHandle = handlePosition;
			}
			// If not, perhaps they want to move the rectangle
			else if (this.selection != null && this.selectionScaled.contains(x, y))
			{

				this.selectionMode = SelectionMode.Moving;
				// Store the distance between the rectangle's origin and
				// where they started moving it from.
				this.selectionStartX = x - this.selectionScaled.x;
				this.selectionStartY = y - this.selectionScaled.y;
			}
			// If not those, it's time to create a rectangle
			else
			{
				this.selectionMode = SelectionMode.Creating;
				this.selectionStartX = x;
				this.selectionStartY = y;
			}
		}
	}

	/**
	 * Calculates a bunch of scaling data that we cache to speed up painting.
	 * This is recalculated when the size of the component or the size of the
	 * source changes. This method is synchronized, along with paintComponent()
	 * so that the updates to the cached data are atomic. TODO: Also need to
	 * update if the camera's units per pixels changes.
	 */
	private synchronized void calculateScalingData()
	{
		BufferedImage image = this.lastFrame;

		if (image == null)
			return;

		Insets ins = this.getInsets();
		int width = this.getWidth() - ins.left - ins.right;
		int height = this.getHeight() - ins.top - ins.bottom;

		double destWidth = width, destHeight = height;

		this.lastWidth = width;
		this.lastHeight = height;

		this.lastSourceWidth = image.getWidth();
		this.lastSourceHeight = image.getHeight();

		double heightRatio = this.lastSourceHeight / destHeight;
		double widthRatio = this.lastSourceWidth / destWidth;

		if (heightRatio > widthRatio)
		{
			double aspectRatio = this.lastSourceWidth / this.lastSourceHeight;
			this.scaledHeight = (int) destHeight;
			this.scaledWidth = (int) (this.scaledHeight * aspectRatio);
		} else
		{
			double aspectRatio = this.lastSourceHeight / this.lastSourceWidth;
			this.scaledWidth = (int) destWidth;
			this.scaledHeight = (int) (this.scaledWidth * aspectRatio);
		}

		this.imageX = ins.left + width / 2 - this.scaledWidth / 2;
		this.imageY = ins.top + height / 2 - this.scaledHeight / 2;

		this.scaleRatioX = this.lastSourceWidth / this.scaledWidth;
		this.scaleRatioY = this.lastSourceHeight / this.scaledHeight;

		this.lastUnitsPerPixel = this.camera.getUnitsPerPixel();
		this.scaledUnitsPerPixelX = this.lastUnitsPerPixel.getX() * this.scaleRatioX;
		this.scaledUnitsPerPixelY = this.lastUnitsPerPixel.getY() * this.scaleRatioY;

		if (this.selectionEnabled && this.selection != null)
			// setSelection() handles updating the scaled rectangle
			this.setSelection(this.selection);
	}

	public BufferedImage captureSelectionImage()
	{
		if (this.selection == null || this.lastFrame == null)
			return null;

		this.selectionFlashOpacity = 1.0f;

		ScheduledFuture future = this.scheduledExecutor.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				if (CameraView.this.selectionFlashOpacity > 0)
				{
					CameraView.this.selectionFlashOpacity -= 0.07;
					CameraView.this.selectionFlashOpacity = Math.max(0, CameraView.this.selectionFlashOpacity);
					CameraView.this.repaint();
				} else
					throw new RuntimeException();
			}
		}, 0, 30, TimeUnit.MILLISECONDS);

		int sx = this.selection.x;
		int sy = this.selection.y;
		int sw = this.selection.width;
		int sh = this.selection.height;

		BufferedImage image = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.drawImage(this.lastFrame, 0, 0, sw, sh, sx, sy, sx + sw, sy + sh, null);
		g.dispose();

		while (!future.isDone())
			;

		return image;
	}

	/**
	 * Capture the current image (unscaled, unmodified) and write it to disk.
	 */
	private void captureSnapshot()
	{
		try
		{
			this.flash();
			File dir = new File(Configuration.get().getConfigurationDirectory(), "snapshots");
			dir.mkdirs();
			DateFormat df = new SimpleDateFormat("YYYY-MM-dd_HH.mm.ss.SSS");
			File file = new File(dir, this.camera.getName() + "_" + df.format(new Date()) + ".png");
			ImageIO.write(this.lastFrame, "png", file);
		} catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	private void continueSelection(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();

		if (this.selectionMode == SelectionMode.Resizing)
		{
			int rx = this.selectionScaled.x;
			int ry = this.selectionScaled.y;
			int rw = this.selectionScaled.width;
			int rh = this.selectionScaled.height;

			if (this.selectionActiveHandle == HandlePosition.NW)
				this.setScaledSelection(x, y, rw - (x - rx), rh - (y - ry));
			else if (this.selectionActiveHandle == HandlePosition.NE)
				this.setScaledSelection(rx, y, x - rx, rh - (y - ry));
			else if (this.selectionActiveHandle == HandlePosition.N)
				this.setScaledSelection(rx, y, rw, rh - (y - ry));
			else if (this.selectionActiveHandle == HandlePosition.E)
				this.setScaledSelection(rx, ry, rw + x - (rx + rw), rh);
			else if (this.selectionActiveHandle == HandlePosition.SE)
				this.setScaledSelection(rx, ry, rw + x - (rx + rw), rh + y - (ry + rh));
			else if (this.selectionActiveHandle == HandlePosition.S)
				this.setScaledSelection(rx, ry, rw, rh + y - (ry + rh));
			else if (this.selectionActiveHandle == HandlePosition.SW)
				this.setScaledSelection(x, ry, rw - (x - rx), rh + y - (ry + rh));
			else if (this.selectionActiveHandle == HandlePosition.W)
				this.setScaledSelection(x, ry, rw - (x - rx), rh);
		} else if (this.selectionMode == SelectionMode.Moving)
			this.setScaledSelection(x - this.selectionStartX, y - this.selectionStartY, this.selectionScaled.width, this.selectionScaled.height);
		else if (this.selectionMode == SelectionMode.Creating)
		{
			int sx = this.selectionStartX;
			int sy = this.selectionStartY;
			int w = x - sx;
			int h = y - sy;
			this.setScaledSelection(sx, sy, w, h);
		}
		this.updateCursor();
		this.repaint();
	}

	private void endSelection()
	{
		this.selectionMode = null;
		this.selectionActiveHandle = null;
	}

	private void fireActionEvent(MouseEvent e)
	{
		if (this.actionListeners.isEmpty())
			return;

		int x = e.getX();
		int y = e.getY();

		// Find the difference in X and Y from the center of the image
		// to the mouse click.
		double offsetX = this.scaledWidth / 2.0D - (x - this.imageX);
		double offsetY = this.scaledHeight / 2.0D - (y - this.imageY);

		// Invert the X so that the offsets represent a bottom left to
		// top right coordinate system.
		offsetX = -offsetX;

		// Scale the offsets by the units per pixel for the camera.
		offsetX *= this.scaledUnitsPerPixelX;
		offsetY *= this.scaledUnitsPerPixelY;

		// The offsets now represent the distance to move the camera
		// in the Camera's units per pixel's units.

		// Create a location in the Camera's units per pixel's units
		// and with the values of the offsets.
		Location offsets = this.camera.getUnitsPerPixel().derive(offsetX, offsetY, 0.0, 0.0);
		// Add the offsets to the Camera's position.
		Location location = this.camera.getLocation().add(offsets);
		CameraViewActionEvent action = new CameraViewActionEvent(CameraView.this, e.getX(), e.getY(), e.getX() * this.scaledUnitsPerPixelX, e.getY() * this.scaledUnitsPerPixelY, location);
		for (CameraViewActionListener listener : new ArrayList<>(this.actionListeners))
			listener.actionPerformed(action);
	}

	/**
	 * Causes a short flash in the CameraView to get the user's attention.
	 */
	public void flash()
	{
		this.flashStartTimeMs = System.currentTimeMillis();
		this.scheduledExecutor.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				if (System.currentTimeMillis() - CameraView.this.flashStartTimeMs < CameraView.this.flashLengthMs)
					CameraView.this.repaint();
				else
				{
					CameraView.this.flashStartTimeMs = 0;
					throw new RuntimeException();
				}
			}
		}, 0, 30, TimeUnit.MILLISECONDS);
	}

	@Override
	public void frameReceived(BufferedImage img)
	{
		if (this.cameraViewFilter != null)
			img = this.cameraViewFilter.filterCameraImage(this.camera, img);
		BufferedImage oldFrame = this.lastFrame;
		this.lastFrame = img;
		if (oldFrame == null || oldFrame.getWidth() != img.getWidth() || oldFrame.getHeight() != img.getHeight() || this.camera.getUnitsPerPixel() != this.lastUnitsPerPixel)
			this.calculateScalingData();
		this.repaint();
	}

	public Camera getCamera()
	{
		return this.camera;
	}

	public Reticle getDefaultReticle()
	{
		return this.reticles.get(CameraView.DEFAULT_RETICLE_KEY);
	}

	public int getMaximumFps()
	{
		return this.maximumFps;
	}

	public Reticle getReticle(Object key)
	{
		return this.reticles.get(key);
	}

	public Rectangle getSelection()
	{
		return this.selection;
	}

	/**
	 * Gets the HandlePosition, if any, at the given x and y. Returns null if
	 * there is not a handle at that position.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private HandlePosition getSelectionHandleAtPosition(int x, int y)
	{
		if (this.selection == null)
			return null;

		int rx = this.selectionScaled.x;
		int ry = this.selectionScaled.y;
		int rw = this.selectionScaled.width;
		int rh = this.selectionScaled.height;
		int rx2 = rx + rw;
		int ry2 = ry + rh;
		int rxc = rx + rw / 2;
		int ryc = ry + rh / 2;

		if (CameraView.isWithinHandle(x, y, rx, ry))
			return HandlePosition.NW;
		else if (CameraView.isWithinHandle(x, y, rx2, ry))
			return HandlePosition.NE;
		else if (CameraView.isWithinHandle(x, y, rx, ry2))
			return HandlePosition.SW;
		else if (CameraView.isWithinHandle(x, y, rx2, ry2))
			return HandlePosition.SE;
		else if (CameraView.isWithinHandle(x, y, rxc, ry))
			return HandlePosition.N;
		else if (CameraView.isWithinHandle(x, y, rx2, ryc))
			return HandlePosition.E;
		else if (CameraView.isWithinHandle(x, y, rxc, ry2))
			return HandlePosition.S;
		else if (CameraView.isWithinHandle(x, y, rx, ryc))
			return HandlePosition.W;
		return null;
	}

	public CameraViewSelectionTextDelegate getSelectionTextDelegate()
	{
		return this.selectionTextDelegate;
	}

	public String getText()
	{
		return this.text;
	}

	public boolean isSelectionEnabled()
	{
		return this.selectionEnabled;
	}

	public boolean isShowImageInfo()
	{
		return this.showImageInfo;
	}

	private void moveToClick(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();

		// Find the difference in X and Y from the center of the image
		// to the mouse click.
		double offsetX = this.scaledWidth / 2.0D - (x - this.imageX);
		double offsetY = this.scaledHeight / 2.0D - (y - this.imageY) + 1;

		// Invert the X so that the offsets represent a bottom left to
		// top right coordinate system.
		offsetX = -offsetX;

		// Scale the offsets by the units per pixel for the camera.
		offsetX *= this.scaledUnitsPerPixelX;
		offsetY *= this.scaledUnitsPerPixelY;

		// The offsets now represent the distance to move the camera
		// in the Camera's units per pixel's units.

		// Create a location in the Camera's units per pixel's units
		// and with the values of the offsets.
		Location offsets = this.camera.getUnitsPerPixel().derive(offsetX, offsetY, 0.0, 0.0);
		// Add the offsets to the Camera's position.
		Location location = this.camera.getLocation().add(offsets);
		// And move there.
		UiUtils.submitUiMachineTask(() -> {
			MovableUtils.moveToLocationAtSafeZ(this.camera, location, 1.0);
		});
	}

	@Override
	protected synchronized void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		BufferedImage image = this.lastFrame;
		Insets ins = this.getInsets();
		int width = this.getWidth() - ins.left - ins.right;
		int height = this.getHeight() - ins.top - ins.bottom;
		Graphics2D g2d = (Graphics2D) g;
		g.setColor(this.getBackground());
		g2d.fillRect(ins.left, ins.top, width, height);
		if (image != null)
		{
			// Only render if there is a valid image.
			g2d.drawImage(this.lastFrame, this.imageX, this.imageY, this.scaledWidth, this.scaledHeight, null);

			double c = this.camera.getLocation().getRotation();

			for (Reticle reticle : this.reticles.values())
				reticle.draw(g2d, this.camera.getUnitsPerPixel().getUnits(), this.scaledUnitsPerPixelX, this.scaledUnitsPerPixelY, ins.left + width / 2, ins.top + height / 2, this.scaledWidth,
						this.scaledHeight, c);

			if (this.text != null)
				CameraView.drawTextOverlay(g2d, 10, 10, this.text);

			if (this.showImageInfo && this.text == null)
				CameraView.drawImageInfo(g2d, 10, 10, image);

			if (this.selectionEnabled && this.selection != null)
				this.paintSelection(g2d);
		} else
		{
			g.setColor(Color.red);
			g.drawLine(ins.left, ins.top, ins.right, ins.bottom);
			g.drawLine(ins.right, ins.top, ins.left, ins.bottom);
		}

		if (this.flashStartTimeMs > 0)
		{
			long timeLeft = this.flashLengthMs - (System.currentTimeMillis() - this.flashStartTimeMs);
			float alpha = 1f / this.flashLengthMs * timeLeft;
			alpha = Math.min(alpha, 1);
			alpha = Math.max(alpha, 0);
			g2d.setColor(new Color(1f, 1f, 1f, alpha));
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
	}

	private void paintSelection(Graphics2D g2d)
	{
		int rx = this.selectionScaled.x;
		int ry = this.selectionScaled.y;
		int rw = this.selectionScaled.width;
		int rh = this.selectionScaled.height;
		int rx2 = rx + rw;
		int ry2 = ry + rh;
		int rxc = rx + rw / 2;
		int ryc = ry + rh / 2;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// Draw the dashed rectangle, black background, white dashes
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(1f));
		g2d.drawRect(rx, ry, rw, rh);
		g2d.setColor(Color.white);
		g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, CameraView.selectionDashProfile, this.selectionDashPhase));
		g2d.drawRect(rx, ry, rw, rh);

		if (this.selectionMode != SelectionMode.Creating)
		{
			// If we're not drawing a whole new rectangle, draw the
			// handles for the existing one.
			CameraView.drawHandle(g2d, rx, ry);
			CameraView.drawHandle(g2d, rx2, ry);
			CameraView.drawHandle(g2d, rx, ry2);
			CameraView.drawHandle(g2d, rx2, ry2);

			CameraView.drawHandle(g2d, rxc, ry);
			CameraView.drawHandle(g2d, rx2, ryc);
			CameraView.drawHandle(g2d, rxc, ry2);
			CameraView.drawHandle(g2d, rx, ryc);
		}

		if (this.selectionTextDelegate != null)
		{
			String text = this.selectionTextDelegate.getSelectionText(this);
			if (text != null)
				// TODO: Be awesome like Apple and move the overlay inside
				// the rect if it goes past the edge of the window
				CameraView.drawTextOverlay(g2d, rx + rw + 6, ry + rh + 6, text);
		}

		if (this.selectionFlashOpacity > 0)
		{
			g2d.setColor(new Color(1.0f, 1.0f, 1.0f, this.selectionFlashOpacity));
			g2d.fillRect(rx, ry, rw, rh);
		}
	}

	public boolean removeActionListener(CameraViewActionListener listener)
	{
		return this.actionListeners.remove(listener);
	}

	public Reticle removeReticle(Object key)
	{
		return this.reticles.remove(key);
	}

	public void setCamera(Camera camera)
	{
		// turn off capture for the camera we are replacing, if any
		if (this.camera != null)
			this.camera.stopContinuousCapture(this);
		this.camera = camera;
		// turn on capture for the new camera
		if (this.camera != null)
			this.camera.startContinuousCapture(this, this.maximumFps);
	}

	public void setCameraViewFilter(CameraViewFilter cameraViewFilter)
	{
		this.cameraViewFilter = cameraViewFilter;
	}

	public void setDefaultReticle(Reticle reticle)
	{
		this.setReticle(CameraView.DEFAULT_RETICLE_KEY, reticle);

		this.prefs.put(CameraView.PREF_RETICLE, XmlSerialize.serialize(reticle));
		try
		{
			this.prefs.flush();
		} catch (Exception e)
		{

		}
	}

	public void setMaximumFps(int maximumFps)
	{
		this.maximumFps = maximumFps;
		// turn off capture for the camera we are replacing, if any
		if (this.camera != null)
			this.camera.stopContinuousCapture(this);
		// turn on capture for the new camera
		if (this.camera != null)
			this.camera.startContinuousCapture(this, maximumFps);
	}

	public void setReticle(Object key, Reticle reticle)
	{
		if (reticle == null)
			this.removeReticle(key);
		else
			this.reticles.put(key, reticle);
	}

	/**
	 * Set the selection rectangle in component coordinates. Updates the
	 * selection property with the properly scaled coordinates.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void setScaledSelection(int x, int y, int width, int height)
	{
		this.selectionScaled = new Rectangle(x, y, width, height);
		this.selectionActiveHandle = CameraView.getOpposingHandle(this.selectionScaled, this.selectionActiveHandle);
		this.selectionScaled = CameraView.normalizeRectangle(this.selectionScaled);

		int rx = (int) ((x - this.imageX) * this.scaleRatioX);
		int ry = (int) ((y - this.imageY) * this.scaleRatioY);
		int rw = (int) (width * this.scaleRatioX);
		int rh = (int) (height * this.scaleRatioY);

		this.selection = new Rectangle(rx, ry, rw, rh);
	}

	/**
	 * Set the selection rectangle in image coordinates.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setSelection(int x, int y, int width, int height)
	{
		this.setSelection(new Rectangle(x, y, width, height));
	}

	/**
	 * Set the selection rectangle in image coordinates.
	 * 
	 * @param r
	 */
	public void setSelection(Rectangle r)
	{
		if (r == null)
		{
			this.selection = null;
			this.selectionMode = null;
		} else
		{
			this.selectionActiveHandle = CameraView.getOpposingHandle(r, this.selectionActiveHandle);
			this.selection = CameraView.normalizeRectangle(r);

			int rx = (int) (this.imageX + this.selection.x / this.scaleRatioX);
			int ry = (int) (this.imageY + this.selection.y / this.scaleRatioY);
			int rw = (int) (this.selection.width / this.scaleRatioX);
			int rh = (int) (this.selection.height / this.scaleRatioY);
			this.selectionScaled = new Rectangle(rx, ry, rw, rh);
		}
	}

	public void setSelectionEnabled(boolean selectionEnabled)
	{
		this.selectionEnabled = selectionEnabled;
	}

	public void setSelectionTextDelegate(CameraViewSelectionTextDelegate selectionTextDelegate)
	{
		this.selectionTextDelegate = selectionTextDelegate;
	}

	public void setShowImageInfo(boolean showImageInfo)
	{
		this.showImageInfo = showImageInfo;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	/**
	 * Show image instead of the camera image for milliseconds. After
	 * milliseconds elapses the view goes back to showing the camera image. The
	 * image should be the same width and height as the camera image otherwise
	 * the behavior is undefined. This function is intended to be used to
	 * briefly show the result of image processing. This is a shortcut to
	 * setCameraViewFilter(CameraViewFilter) which simply removes itself after
	 * the specified time.
	 * 
	 * @param image
	 * @param millseconds
	 */
	public void showFilteredImage(final BufferedImage filteredImage, final long milliseconds)
	{
		this.setCameraViewFilter(new CameraViewFilter()
		{
			long t = System.currentTimeMillis();

			@Override
			public BufferedImage filterCameraImage(Camera camera, BufferedImage image)
			{
				if (System.currentTimeMillis() - this.t < milliseconds)
					return filteredImage;
				else
				{
					CameraView.this.setCameraViewFilter(null);
					return image;
				}
			}
		});
	}

	/**
	 * Updates the Cursor to reflect the current state of the component.
	 */
	private void updateCursor()
	{
		if (this.selectionEnabled)
		{
			if (this.selectionMode == SelectionMode.Moving)
				this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			else if (this.selectionMode == SelectionMode.Resizing)
				this.setCursor(CameraView.getCursorForHandlePosition(this.selectionActiveHandle));
			else if (this.selectionMode == null && this.selection != null)
			{
				int x = this.getMousePosition().x;
				int y = this.getMousePosition().y;

				HandlePosition handlePosition = this.getSelectionHandleAtPosition(x, y);
				if (handlePosition != null)
					this.setCursor(CameraView.getCursorForHandlePosition(handlePosition));
				else if (this.selectionScaled.contains(x, y))
					this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				else
					this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			} else
				this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		} else
			this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
}
