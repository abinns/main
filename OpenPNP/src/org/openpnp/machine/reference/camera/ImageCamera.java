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

package org.openpnp.machine.reference.camera;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Action;

import org.openpnp.CameraListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.wizards.CameraConfigurationWizard;
import org.openpnp.machine.reference.ReferenceCamera;
import org.openpnp.machine.reference.camera.wizards.ImageCameraConfigurationWizard;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.PropertySheetHolder;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.core.Commit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageCamera extends ReferenceCamera implements Runnable
{
	private final static Logger logger = LoggerFactory.getLogger(ImageCamera.class);

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@Attribute(required = false)
	private int fps = 24;

	@Element
	private String sourceUri = "classpath://samples/pnp-test/pnp-test.png";

	@Attribute(required = false)
	private int width = 640;

	@Attribute(required = false)
	private int height = 480;

	private BufferedImage source;

	private Thread thread;

	public ImageCamera()
	{
		this.unitsPerPixel = new Location(LengthUnit.Inches, 0.04233, 0.04233, 0, 0);
	}

	@Override
	public synchronized BufferedImage capture()
	{
		/*
		 * Create a buffer that we will render the center tile and it's
		 * surrounding tiles to.
		 */
		BufferedImage frame = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);

		Graphics gFrame = frame.getGraphics();

		Location location = this.getLocation();
		double locationX = location.getX();
		double locationY = location.getY();

		double pixelX = locationX / this.getUnitsPerPixel().getX();
		double pixelY = locationY / this.getUnitsPerPixel().getY();

		int dx1 = (int) (pixelX - this.width / 2);
		int dy1 = (int) (this.source.getHeight() - (pixelY + this.height / 2));

		gFrame.drawImage(this.source, 0, 0, this.width - 1, this.height - 1, dx1, dy1, dx1 + this.width - 1, dy1 + this.height - 1, null);

		gFrame.dispose();

		return this.transformImage(frame);
	}

	@SuppressWarnings("unused")
	@Commit
	private void commit() throws Exception
	{
		this.setSourceUri(this.sourceUri);
	}

	@Override
	public PropertySheetHolder[] getChildPropertySheetHolders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Wizard getConfigurationWizard()
	{
		return new ImageCameraConfigurationWizard(this);
	}

	@Override
	public Action[] getPropertySheetHolderActions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPropertySheetHolderTitle()
	{
		return this.getClass().getSimpleName() + " " + this.getName();
	}

	@Override
	public PropertySheet[] getPropertySheets()
	{
		return new PropertySheet[]
		{ new PropertySheetWizardAdapter(new CameraConfigurationWizard(this)), new PropertySheetWizardAdapter(this.getConfigurationWizard()) };
	}

	public String getSourceUri()
	{
		return this.sourceUri;
	}

	private synchronized void initialize() throws Exception
	{
		this.stop();

		if (this.sourceUri.startsWith("classpath://"))
			this.source = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(this.sourceUri.substring("classpath://".length())));
		else
			this.source = ImageIO.read(new URL(this.sourceUri));

		if (this.listeners.size() > 0)
			this.start();
	}

	@Override
	public void run()
	{
		while (!Thread.interrupted())
		{
			BufferedImage frame = this.capture();
			this.broadcastCapture(frame);
			try
			{
				Thread.sleep(1000 / this.fps);
			} catch (InterruptedException e)
			{
				return;
			}
		}
	}

	public void setSourceUri(String sourceUri) throws Exception
	{
		String oldValue = this.sourceUri;
		this.sourceUri = sourceUri;
		this.pcs.firePropertyChange("sourceUri", oldValue, sourceUri);
		this.initialize();
	}

	private synchronized void start()
	{
		if (this.thread == null)
		{
			this.thread = new Thread(this);
			this.thread.start();
		}
	}

	@Override
	public synchronized void startContinuousCapture(CameraListener listener, int maximumFps)
	{
		this.start();
		super.startContinuousCapture(listener, maximumFps);
	}

	private synchronized void stop()
	{
		if (this.thread != null && this.thread.isAlive())
		{
			this.thread.interrupt();
			try
			{
				this.thread.join();
			} catch (Exception e)
			{

			}
			this.thread = null;
		}
	}

	@Override
	public synchronized void stopContinuousCapture(CameraListener listener)
	{
		super.stopContinuousCapture(listener);
		if (this.listeners.size() == 0)
			this.stop();
	}
}
