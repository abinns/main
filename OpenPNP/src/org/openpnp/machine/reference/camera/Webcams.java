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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.openpnp.CameraListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.wizards.CameraConfigurationWizard;
import org.openpnp.machine.reference.ReferenceCamera;
import org.openpnp.machine.reference.camera.wizards.WebcamConfigurationWizard;
import org.openpnp.spi.PropertySheetHolder;
import org.simpleframework.xml.Attribute;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.util.jh.JHGrayFilter;

/**
 * A Camera implementation based on the OpenCV FrameGrabbers.
 */
public class Webcams extends ReferenceCamera implements Runnable, WebcamImageTransformer
{

	private static final JHGrayFilter GRAY = new JHGrayFilter();

	@Attribute(required = false)
	protected String	deviceId		= "###DEVICE###";
	@Attribute(required = false)
	private int			preferredWidth	= 0;

	@Attribute(required = false)
	private int			preferredHeight	= 0;
	protected Webcam	webcam;
	private Thread		thread;
	private boolean		forceGray;

	private BufferedImage image;

	private BufferedImage lastImage = null;

	private BufferedImage redImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	public Webcams()
	{

	}

	@Override
	public synchronized BufferedImage capture()
	{
		if (this.thread == null)
			this.setDeviceId(this.deviceId);
		if (this.thread == null)
			return null;
		try
		{
			BufferedImage img = this.webcam.getImage();
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
			this.webcam.close();
		}
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
		return new WebcamConfigurationWizard(this);
	}

	public String getDeviceId()
	{
		return this.deviceId;
	}

	public List<String> getDeviceIds() throws Exception
	{
		ArrayList<String> deviceIds = new ArrayList<>();
		for (Webcam cam : Webcam.getWebcams())
			deviceIds.add(cam.getName());
		return deviceIds;
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

	public boolean isForceGray()
	{
		return this.forceGray;
	}

	@Override
	public void run()
	{
		while (!Thread.interrupted())
		{
			try
			{
				BufferedImage image = this.capture();
				if (image == null)
					image = this.redImage;
				this.broadcastCapture(image);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				Thread.sleep(1000 / 30);
			} catch (InterruptedException e)
			{
				break;
			}
		}
	}

	public synchronized void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
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
			this.webcam.close();
		}
		try
		{
			this.webcam = null;
			for (Webcam cam : Webcam.getWebcams())
				if (cam.getName().equals(deviceId))
					this.webcam = cam;
			if (this.webcam == null)
				return;
			if (this.preferredWidth != 0 && this.preferredHeight != 0)
				this.webcam.setViewSize(new Dimension(this.preferredWidth, this.preferredHeight));
			this.webcam.open();
			if (this.forceGray)
				this.webcam.setImageTransformer(this);
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		this.thread = new Thread(this);
		this.thread.start();
	}

	public void setForceGray(boolean val)
	{
		this.forceGray = val;
	}

	public void setPreferredHeight(int preferredHeight)
	{
		this.preferredHeight = preferredHeight;
	}

	public void setPreferredWidth(int preferredWidth)
	{
		this.preferredWidth = preferredWidth;
	}

	@Override
	public synchronized void startContinuousCapture(CameraListener listener, int maximumFps)
	{
		if (this.thread == null)
			this.setDeviceId(this.deviceId);
		super.startContinuousCapture(listener, maximumFps);
	}

	@Override
	public BufferedImage transform(BufferedImage image)
	{
		return Webcams.GRAY.filter(image, null);
	}
}
