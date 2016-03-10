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

package org.openpnp.machine.reference.camera.wizards;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.openpnp.machine.reference.camera.Webcams;
import org.openpnp.machine.reference.wizards.ReferenceCameraConfigurationWizard;

import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class WebcamConfigurationWizard extends ReferenceCameraConfigurationWizard implements WebcamDiscoveryListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Webcams camera;

	private JPanel		panelGeneral;
	private JComboBox	comboBoxDeviceId;
	private JCheckBox	chckbxGray;

	public WebcamConfigurationWizard(Webcams camera)
	{
		super(camera);
		this.camera = camera;

		this.panelGeneral = new JPanel();
		this.contentPanel.add(this.panelGeneral);
		this.panelGeneral.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "General", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.panelGeneral.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblDeviceId = new JLabel("Device ID");
		this.panelGeneral.add(lblDeviceId, "2, 2, right, default");

		Object[] deviceIds = null;
		try
		{
			deviceIds = camera.getDeviceIds().toArray(new String[]
			{});
		} catch (Exception e)
		{
			// TODO:
		}
		this.comboBoxDeviceId = new JComboBox(deviceIds);
		this.panelGeneral.add(this.comboBoxDeviceId, "4, 2, left, default");

		this.chckbxGray = new JCheckBox("Force Grayscale?");
		this.panelGeneral.add(this.chckbxGray, "2, 4, 3, 1");
	}

	@Override
	public void createBindings()
	{
		super.createBindings();
		// The order of the properties is important. We want all the booleans
		// to be set before we set the driver because setting the driver
		// applies all the settings.
		this.addWrappedBinding(this.camera, "forceGray", this.chckbxGray, "selected");
		this.addWrappedBinding(this.camera, "deviceId", this.comboBoxDeviceId, "selectedItem");
	}

	private void updateList()
	{
		this.comboBoxDeviceId.removeAllItems();
		try
		{
			int i = 0;
			String id = null;
			for (String s : this.camera.getDeviceIds().toArray(new String[]
			{}))
			{
				this.comboBoxDeviceId.addItem(s);
				if (s.equals(this.camera.getDeviceId()))
					id = s;
				i++;
			}
			if (id != null)
				this.comboBoxDeviceId.setSelectedItem(id);
		} catch (Exception e)
		{
			;
		}
		this.comboBoxDeviceId.repaint();
		this.panelGeneral.revalidate(); // for JFrame up to Java7 is there only
										 // validate()
		this.panelGeneral.repaint();
	}

	@Override
	public void webcamFound(WebcamDiscoveryEvent event)
	{
		if (this.camera.getDeviceId().equals(event.getWebcam().getName()))
			System.out.format("Webcam connected: %s \n", event.getWebcam().getName());
		this.updateList();
	}

	@Override
	public void webcamGone(WebcamDiscoveryEvent event)
	{
		if (this.camera.getDeviceId().equals(event.getWebcam().getName()))
			System.out.format("Webcam disconnected: %s \n", event.getWebcam().getName());
		this.updateList();
	}

}
