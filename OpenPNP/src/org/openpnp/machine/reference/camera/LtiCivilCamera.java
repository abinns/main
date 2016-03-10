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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.wizards.CameraConfigurationWizard;
import org.openpnp.machine.reference.ReferenceCamera;
import org.openpnp.machine.reference.camera.wizards.LtiCivilCameraConfigurationWizard;
import org.openpnp.model.Configuration;
import org.openpnp.spi.PropertySheetHolder;
import org.simpleframework.xml.Attribute;

import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;
import com.lti.civil.Image;
import com.lti.civil.VideoFormat;
import com.lti.civil.awt.AWTImageConverter;

@Deprecated
public class LtiCivilCamera extends ReferenceCamera implements CaptureObserver
{
	private CaptureSystemFactory	captureSystemFactory;
	private CaptureSystem			captureSystem;
	private CaptureStream			captureStream;
	private VideoFormat				videoFormat;

	@Attribute(required = false)
	private String	deviceId;
	@Attribute(required = false)
	private boolean	forceGrayscale;

	private int width, height;

	private BufferedImage lastImage;

	private Object captureLock = new Object();

	public LtiCivilCamera()
	{
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{

			@Override
			public void configurationLoaded(Configuration configuration) throws Exception
			{
				LtiCivilCamera.this.captureSystemFactory = DefaultCaptureSystemFactorySingleton.instance();
				LtiCivilCamera.this.captureSystem = LtiCivilCamera.this.captureSystemFactory.createCaptureSystem();

				if (LtiCivilCamera.this.deviceId != null && LtiCivilCamera.this.deviceId.trim().length() != 0)
					LtiCivilCamera.this.setDeviceId(LtiCivilCamera.this.deviceId);
			}
		});
	}

	@Override
	public BufferedImage capture()
	{
		synchronized (this.captureLock)
		{
			try
			{
				this.captureLock.wait();
				BufferedImage image = this.lastImage;
				return image;
			} catch (Exception e)
			{
				return null;
			}
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
		return new LtiCivilCameraConfigurationWizard(this);
	}

	public String getDeviceId()
	{
		return this.deviceId;
	}

	public List<String> getDeviceIds() throws Exception
	{
		ArrayList<String> deviceIds = new ArrayList<>();
		for (CaptureDeviceInfo captureDeviceInfo : (List<CaptureDeviceInfo>) this.captureSystem.getCaptureDeviceInfoList())
			deviceIds.add(captureDeviceInfo.getDeviceID());
		return deviceIds;
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

	public boolean isForceGrayscale()
	{
		return this.forceGrayscale;
	}

	@Override
	public void onError(CaptureStream captureStream, CaptureException captureException)
	{
	}

	@Override
	public void onNewImage(CaptureStream captureStream, Image newImage)
	{
		BufferedImage bImage = AWTImageConverter.toBufferedImage(newImage);
		if (this.forceGrayscale)
		{
			BufferedImage grayImage = new BufferedImage(this.width, this.height, BufferedImage.TYPE_BYTE_GRAY);
			Graphics g = grayImage.getGraphics();
			g.drawImage(bImage, 0, 0, null);
			g.dispose();
			this.lastImage = grayImage;
		} else
			this.lastImage = bImage;
		this.lastImage = this.transformImage(this.lastImage);
		this.broadcastCapture(this.lastImage);
		synchronized (this.captureLock)
		{
			this.captureLock.notify();
		}
	}

	public void setDeviceId(String deviceId) throws Exception
	{
		if (this.captureStream != null)
		{
			this.captureStream.stop();
			this.captureStream.dispose();
		}
		this.captureStream = this.captureSystem.openCaptureDeviceStream(deviceId);
		this.videoFormat = this.captureStream.getVideoFormat();
		this.width = this.videoFormat.getWidth();
		this.height = this.videoFormat.getHeight();
		this.captureStream.setObserver(this);
		this.captureStream.start();
		this.deviceId = deviceId;
	}

	public void setForceGrayscale(boolean forceGrayscale)
	{
		this.forceGrayscale = forceGrayscale;
	}
}
