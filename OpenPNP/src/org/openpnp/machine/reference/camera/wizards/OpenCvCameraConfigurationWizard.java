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

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.machine.reference.camera.OpenCvCamera;
import org.openpnp.machine.reference.wizards.ReferenceCameraConfigurationWizard;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class OpenCvCameraConfigurationWizard extends ReferenceCameraConfigurationWizard
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final OpenCvCamera camera;

	private JPanel panelGeneral;

	private JComboBox comboBoxDeviceIndex;

	private JLabel lblPreferredWidth;

	private JLabel lblPreferredHeight;

	private JTextField	textFieldPreferredWidth;
	private JTextField	textFieldPreferredHeight;
	private JLabel		lbluseFor;
	private JLabel		lbluseFor_1;

	public OpenCvCameraConfigurationWizard(OpenCvCamera camera)
	{
		super(camera);

		this.camera = camera;

		this.panelGeneral = new JPanel();
		this.contentPanel.add(this.panelGeneral);
		this.panelGeneral.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "General", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.panelGeneral.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblDeviceId = new JLabel("Device Index");
		this.panelGeneral.add(lblDeviceId, "2, 2, right, default");

		this.comboBoxDeviceIndex = new JComboBox();
		for (int i = 0; i < 10; i++)
			this.comboBoxDeviceIndex.addItem(new Integer(i));
		this.panelGeneral.add(this.comboBoxDeviceIndex, "4, 2, left, default");

		this.lblPreferredWidth = new JLabel("Preferred Width");
		this.panelGeneral.add(this.lblPreferredWidth, "2, 6, right, default");

		this.textFieldPreferredWidth = new JTextField();
		this.panelGeneral.add(this.textFieldPreferredWidth, "4, 6, fill, default");
		this.textFieldPreferredWidth.setColumns(10);

		this.lbluseFor = new JLabel("(Use 0 for native resolution)");
		this.panelGeneral.add(this.lbluseFor, "6, 6");

		this.lblPreferredHeight = new JLabel("Preferred Height");
		this.panelGeneral.add(this.lblPreferredHeight, "2, 8, right, default");

		this.textFieldPreferredHeight = new JTextField();
		this.panelGeneral.add(this.textFieldPreferredHeight, "4, 8, fill, default");
		this.textFieldPreferredHeight.setColumns(10);

		this.lbluseFor_1 = new JLabel("(Use 0 for native resolution)");
		this.panelGeneral.add(this.lbluseFor_1, "6, 8");
	}

	@Override
	public void createBindings()
	{
		IntegerConverter intConverter = new IntegerConverter();
		super.createBindings();
		this.addWrappedBinding(this.camera, "preferredWidth", this.textFieldPreferredWidth, "text", intConverter);
		this.addWrappedBinding(this.camera, "preferredHeight", this.textFieldPreferredHeight, "text", intConverter);
		// Should always be last so that it doesn't trigger multiple camera
		// reloads.
		this.addWrappedBinding(this.camera, "deviceIndex", this.comboBoxDeviceIndex, "selectedItem");

		ComponentDecorators.decorateWithAutoSelect(this.textFieldPreferredWidth);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldPreferredHeight);
	}

	@Override
	protected void saveToModel()
	{
		super.saveToModel();
		if (this.camera.isDirty())
			this.camera.setDeviceIndex(this.camera.getDeviceIndex());
	}
}
