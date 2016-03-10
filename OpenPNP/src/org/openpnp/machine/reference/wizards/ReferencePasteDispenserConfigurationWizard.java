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

package org.openpnp.machine.reference.wizards;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.machine.reference.ReferencePasteDispenser;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class ReferencePasteDispenserConfigurationWizard extends AbstractConfigurationWizard
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ReferencePasteDispenser dispenser;

	private JTextField	locationX;
	private JTextField	locationY;
	private JTextField	locationZ;
	private JPanel		panelOffsets;
	private JTextField	textFieldSafeZ;

	public ReferencePasteDispenserConfigurationWizard(ReferencePasteDispenser dispenser)
	{
		this.dispenser = dispenser;

		this.panelOffsets = new JPanel();
		this.contentPanel.add(this.panelOffsets);
		this.panelOffsets.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Offsets", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.panelOffsets.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblX = new JLabel("X");
		this.panelOffsets.add(lblX, "2, 2");

		JLabel lblY = new JLabel("Y");
		this.panelOffsets.add(lblY, "4, 2");

		JLabel lblZ = new JLabel("Z");
		this.panelOffsets.add(lblZ, "6, 2");

		this.locationX = new JTextField();
		this.panelOffsets.add(this.locationX, "2, 4");
		this.locationX.setColumns(5);

		this.locationY = new JTextField();
		this.panelOffsets.add(this.locationY, "4, 4");
		this.locationY.setColumns(5);

		this.locationZ = new JTextField();
		this.panelOffsets.add(this.locationZ, "6, 4");
		this.locationZ.setColumns(5);

		this.contentPanel.add(this.panelOffsets);

		JPanel panelSafeZ = new JPanel();
		panelSafeZ.setBorder(new TitledBorder(null, "Safe Z", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(panelSafeZ);
		panelSafeZ.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblSafeZ = new JLabel("Safe Z");
		panelSafeZ.add(lblSafeZ, "2, 2, right, default");

		this.textFieldSafeZ = new JTextField();
		panelSafeZ.add(this.textFieldSafeZ, "4, 2, fill, default");
		this.textFieldSafeZ.setColumns(10);

	}

	@Override
	public void createBindings()
	{
		LengthConverter lengthConverter = new LengthConverter();

		MutableLocationProxy headOffsets = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.dispenser, "headOffsets", headOffsets, "location");
		this.addWrappedBinding(headOffsets, "lengthX", this.locationX, "text", lengthConverter);
		this.addWrappedBinding(headOffsets, "lengthY", this.locationY, "text", lengthConverter);
		this.addWrappedBinding(headOffsets, "lengthZ", this.locationZ, "text", lengthConverter);
		this.addWrappedBinding(this.dispenser, "safeZ", this.textFieldSafeZ, "text", lengthConverter);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.locationX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.locationY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.locationZ);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldSafeZ);
	}
}
