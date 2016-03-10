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

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.Action;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.openpnp.CameraListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.wizards.CameraConfigurationWizard;
import org.openpnp.machine.reference.ReferenceCamera;
import org.openpnp.machine.reference.camera.wizards.OpenCvCameraConfigurationWizard;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.util.OpenCvUtils;
import org.simpleframework.xml.Attribute;

/**
 * A Camera implementation based on the OpenCV FrameGrabbers.
 */
public class OpenCvCamera extends ReferenceCamera implements Runnable
{
	static
	{
		nu.pattern.OpenCV.loadShared();
		System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
	}

	@Attribute(name = "deviceIndex", required = true)
	private int deviceIndex = 0;

	@Attribute(required = false)
	private int	preferredWidth;
	@Attribute(required = false)
	private int	preferredHeight;

	private VideoCapture	fg		= new VideoCapture();
	private Thread			thread;
	private boolean			dirty	= false;

	public OpenCvCamera()
	{
	}

	@Override
	public synchronized BufferedImage capture()
	{
		if (this.thread == null)
			this.setDeviceIndex(this.deviceIndex);
		try
		{
			Mat mat = new Mat();
			if (!this.fg.read(mat))
				return null;
			BufferedImage img = OpenCvUtils.toBufferedImage(mat);
			mat.release();
			return this.transformImage(img);
		} catch (Exception e)
		{
			return null;
		}
	}

	@Override
	public void close() throws IOException
	{
		super.close();
		if (this.thread != null)
		{
			this.thread.interrupt();
			try
			{
				this.thread.join();
			} catch (Exception e)
			{

			}
		}
		if (this.fg.isOpened())
			this.fg.release();
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
		return new OpenCvCameraConfigurationWizard(this);
	}

	public int getDeviceIndex()
	{
		return this.deviceIndex;
	}

	public int getPreferredHeight()
	{
		return this.preferredHeight;
	}

	public int getPreferredWidth()
	{
		return this.preferredWidth;
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

	public boolean isDirty()
	{
		return this.dirty;
	}

	@Override
	public void run()
	{
		while (!Thread.interrupted())
		{
			try
			{
				BufferedImage image = this.capture();
				if (image != null)
					this.broadcastCapture(image);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				Thread.sleep(1000 / 24);
			} catch (InterruptedException e)
			{
				break;
			}
		}
	}

	public synchronized void setDeviceIndex(int deviceIndex)
	{
		this.deviceIndex = deviceIndex;
		if (this.thread != null)
		{
			this.thread.interrupt();
			try
			{
				this.thread.join();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			this.thread = null;
		}
		try
		{
			this.setDirty(false);
			this.width = null;
			this.height = null;
			this.fg.open(deviceIndex);
			if (this.preferredWidth != 0)
				this.fg.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, this.preferredWidth);
			if (this.preferredHeight != 0)
				this.fg.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, this.preferredHeight);
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		this.thread = new Thread(this);
		this.thread.start();
	}

	public void setDirty(boolean dirty)
	{
		this.dirty = dirty;
	}

	public void setPreferredHeight(int preferredHeight)
	{
		this.preferredHeight = preferredHeight;
		this.setDirty(true);
	}

	public void setPreferredWidth(int preferredWidth)
	{
		this.preferredWidth = preferredWidth;
		this.setDirty(true);
	}

	@Override
	public synchronized void startContinuousCapture(CameraListener listener, int maximumFps)
	{
		if (this.thread == null)
			this.setDeviceIndex(this.deviceIndex);
		super.startContinuousCapture(listener, maximumFps);
	}
}
