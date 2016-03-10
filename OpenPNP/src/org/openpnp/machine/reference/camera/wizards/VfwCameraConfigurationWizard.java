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

import org.openpnp.machine.reference.camera.VfwCamera;
import org.openpnp.machine.reference.wizards.ReferenceCameraConfigurationWizard;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class VfwCameraConfigurationWizard extends ReferenceCameraConfigurationWizard
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final VfwCamera camera;

	private JPanel		panelGeneral;
	private JComboBox	comboBoxDriver;
	private JCheckBox	chckbxShowVideoSource;
	private JCheckBox	chckbxShowVideoFormat;
	private JCheckBox	chckbxShowVideoDisplay;

	public VfwCameraConfigurationWizard(VfwCamera camera)
	{
		super(camera);

		this.camera = camera;

		this.panelGeneral = new JPanel();
		this.contentPanel.add(this.panelGeneral);
		this.panelGeneral.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "General", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.panelGeneral.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblDeviceId = new JLabel("Driver");
		this.panelGeneral.add(lblDeviceId, "2, 2, right, default");

		Object[] deviceIds = null;
		try
		{
			deviceIds = camera.getDrivers().toArray(new String[]
			{});
		} catch (Exception e)
		{
			// TODO:
		}

		this.comboBoxDriver = new JComboBox(deviceIds);
		this.panelGeneral.add(this.comboBoxDriver, "4, 2, left, default");

		this.chckbxShowVideoSource = new JCheckBox("Show Video Source Dialog?");
		this.panelGeneral.add(this.chckbxShowVideoSource, "2, 4, 3, 1");

		this.chckbxShowVideoFormat = new JCheckBox("Show Video Format Dialog?");
		this.panelGeneral.add(this.chckbxShowVideoFormat, "2, 6, 3, 1");

		this.chckbxShowVideoDisplay = new JCheckBox("Show Video Display Dialog?");
		this.panelGeneral.add(this.chckbxShowVideoDisplay, "2, 8, 3, 1");
	}

	@Override
	public void createBindings()
	{
		// The order of the properties is important. We want all the booleans
		// to be set before we set the driver because setting the driver
		// applies all the settings.
		this.addWrappedBinding(this.camera, "showVideoSourceDialog", this.chckbxShowVideoSource, "selected");
		this.addWrappedBinding(this.camera, "showVideoFormatDialog", this.chckbxShowVideoFormat, "selected");
		this.addWrappedBinding(this.camera, "showVideoDisplayDialog", this.chckbxShowVideoDisplay, "selected");
		this.addWrappedBinding(this.camera, "driver", this.comboBoxDriver, "selectedItem");
	}

}
