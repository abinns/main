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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.CameraItem;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Head;

/**
 * Shows a square grid of cameras or a blown up image from a single camera.
 */
@SuppressWarnings("serial")
public class CameraPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private static int			maximumFps			= 15;
	private static final String	SHOW_NONE_ITEM		= "Show None";
	private static final String	SHOW_ALL_ITEM		= "Show All";

	private static final String PREF_SELECTED_CAMERA_VIEW = "JobPanel.dividerPosition";

	private Map<Camera, CameraView>	cameraViews	= new LinkedHashMap<>();
	private JComboBox				camerasCombo;

	private JPanel camerasPanel;

	private CameraView	selectedCameraView;
	private Preferences	prefs	= Preferences.userNodeForPackage(CameraPanel.class);

	private AbstractAction cameraSelectedAction = new AbstractAction("")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ev)
		{
			CameraPanel.this.selectedCameraView = null;
			CameraPanel.this.camerasPanel.removeAll();
			if (CameraPanel.this.camerasCombo.getSelectedItem().equals(CameraPanel.SHOW_NONE_ITEM))
			{
				CameraPanel.this.camerasPanel.setLayout(new BorderLayout());
				JPanel panel = new JPanel();
				panel.setBackground(Color.black);
				CameraPanel.this.camerasPanel.add(panel);
				CameraPanel.this.selectedCameraView = null;
			} else if (CameraPanel.this.camerasCombo.getSelectedItem().equals(CameraPanel.SHOW_ALL_ITEM))
			{
				int columns = (int) Math.ceil(Math.sqrt(CameraPanel.this.cameraViews.size()));
				if (columns == 0)
					columns = 1;
				CameraPanel.this.camerasPanel.setLayout(new GridLayout(0, columns, 1, 1));
				for (CameraView cameraView : CameraPanel.this.cameraViews.values())
				{
					cameraView.setMaximumFps(CameraPanel.maximumFps / Math.max(CameraPanel.this.cameraViews.size(), 1));
					CameraPanel.this.camerasPanel.add(cameraView);
					if (CameraPanel.this.cameraViews.size() == 1)
						CameraPanel.this.selectedCameraView = cameraView;
				}
				if (CameraPanel.this.cameraViews.size() > 2)
					for (int i = 0; i < columns * columns - CameraPanel.this.cameraViews.size(); i++)
					{
						JPanel panel = new JPanel();
						panel.setBackground(Color.black);
						CameraPanel.this.camerasPanel.add(panel);
					}
				CameraPanel.this.selectedCameraView = null;
			} else
			{
				CameraPanel.this.camerasPanel.setLayout(new BorderLayout());
				Camera camera = ((CameraItem) CameraPanel.this.camerasCombo.getSelectedItem()).getCamera();
				CameraView cameraView = CameraPanel.this.getCameraView(camera);
				cameraView.setMaximumFps(CameraPanel.maximumFps);
				CameraPanel.this.camerasPanel.add(cameraView);

				CameraPanel.this.selectedCameraView = cameraView;
			}
			CameraPanel.this.revalidate();
			CameraPanel.this.repaint();
		}
	};

	public CameraPanel()
	{
		this.createUi();
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationComplete(Configuration configuration) throws Exception
			{
				for (Head head : Configuration.get().getMachine().getHeads())
					for (Camera camera : head.getCameras())
						CameraPanel.this.addCamera(camera);
				for (Camera camera : configuration.getMachine().getCameras())
					CameraPanel.this.addCamera(camera);

				String selectedCameraView = CameraPanel.this.prefs.get(CameraPanel.PREF_SELECTED_CAMERA_VIEW, null);
				if (selectedCameraView != null)
					for (int i = 0; i < CameraPanel.this.camerasCombo.getItemCount(); i++)
					{
						Object o = CameraPanel.this.camerasCombo.getItemAt(i);
						if (o.toString().equals(selectedCameraView))
							CameraPanel.this.camerasCombo.setSelectedItem(o);
					}
				CameraPanel.this.camerasCombo.addActionListener((event) -> {
					try
					{
						CameraPanel.this.prefs.put(CameraPanel.PREF_SELECTED_CAMERA_VIEW, CameraPanel.this.camerasCombo.getSelectedItem().toString());
						CameraPanel.this.prefs.flush();
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				});
			}
		});
	}

	public void addCamera(Camera camera)
	{
		CameraView cameraView = new CameraView(CameraPanel.maximumFps / Math.max(this.cameraViews.size(), 1));
		cameraView.setCamera(camera);
		this.cameraViews.put(camera, cameraView);
		this.camerasCombo.addItem(new CameraItem(camera));
		if (this.cameraViews.size() == 1)
			// First camera being added, so select it
			this.camerasCombo.setSelectedIndex(1);
		else if (this.cameraViews.size() == 2)
			// Otherwise this is the second camera so mix in the
			// show all item.
			this.camerasCombo.insertItemAt(CameraPanel.SHOW_ALL_ITEM, 1);
	}

	private void createUi()
	{
		this.camerasPanel = new JPanel();

		this.camerasCombo = new JComboBox();
		this.camerasCombo.addActionListener(this.cameraSelectedAction);

		this.setLayout(new BorderLayout());

		this.camerasCombo.addItem(CameraPanel.SHOW_NONE_ITEM);

		this.add(this.camerasCombo, BorderLayout.NORTH);
		this.add(this.camerasPanel);
	}

	/**
	 * Make sure the given Camera is visible in the UI. If All Cameras is
	 * selected we do nothing, otherwise we select the specified Camera.
	 * 
	 * @param camera
	 * @return
	 */
	public void ensureCameraVisible(Camera camera)
	{
		if (this.camerasCombo.getSelectedItem().equals(CameraPanel.SHOW_ALL_ITEM))
			return;
		this.setSelectedCamera(camera);
	}

	public CameraView getCameraView(Camera camera)
	{
		return this.cameraViews.get(camera);
	}

	public CameraView setSelectedCamera(Camera camera)
	{
		if (this.selectedCameraView != null && this.selectedCameraView.getCamera() == camera)
			return this.selectedCameraView;
		for (int i = 0; i < this.camerasCombo.getItemCount(); i++)
		{
			Object o = this.camerasCombo.getItemAt(i);
			if (o instanceof CameraItem)
			{
				Camera c = ((CameraItem) o).getCamera();
				if (c == camera)
				{
					this.camerasCombo.setSelectedIndex(i);
					return this.selectedCameraView;
				}
			}
		}
		return null;
	}
}
