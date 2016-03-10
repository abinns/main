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

package org.openpnp.machine.reference.feeder.wizards;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.machine.reference.feeder.ReferenceTrayFeeder;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class ReferenceTrayFeederConfigurationWizard extends AbstractReferenceFeederConfigurationWizard
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ReferenceTrayFeeder feeder;

	private JTextField	textFieldOffsetsX;
	private JTextField	textFieldOffsetsY;
	private JTextField	textFieldTrayCountX;
	private JTextField	textFieldTrayCountY;
	private JTextField	textFieldFeedCount;

	public ReferenceTrayFeederConfigurationWizard(ReferenceTrayFeeder feeder)
	{
		super(feeder);
		this.feeder = feeder;

		JPanel panelFields = new JPanel();

		panelFields.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblX = new JLabel("X");
		panelFields.add(lblX, "4, 2");

		JLabel lblY = new JLabel("Y");
		panelFields.add(lblY, "6, 2");

		JLabel lblFeedStartLocation = new JLabel("Offsets");
		panelFields.add(lblFeedStartLocation, "2, 4, right, default");

		this.textFieldOffsetsX = new JTextField();
		panelFields.add(this.textFieldOffsetsX, "4, 4, fill, default");
		this.textFieldOffsetsX.setColumns(10);

		this.textFieldOffsetsY = new JTextField();
		panelFields.add(this.textFieldOffsetsY, "6, 4, fill, default");
		this.textFieldOffsetsY.setColumns(10);

		JLabel lblTrayCount = new JLabel("Tray Count");
		panelFields.add(lblTrayCount, "2, 6, right, default");

		this.textFieldTrayCountX = new JTextField();
		panelFields.add(this.textFieldTrayCountX, "4, 6, fill, default");
		this.textFieldTrayCountX.setColumns(10);

		this.textFieldTrayCountY = new JTextField();
		panelFields.add(this.textFieldTrayCountY, "6, 6, fill, default");
		this.textFieldTrayCountY.setColumns(10);

		JSeparator separator = new JSeparator();
		panelFields.add(separator, "4, 8, 3, 1");

		JLabel lblFeedCount = new JLabel("Feed Count");
		panelFields.add(lblFeedCount, "2, 10, right, default");

		this.textFieldFeedCount = new JTextField();
		panelFields.add(this.textFieldFeedCount, "4, 10, fill, default");
		this.textFieldFeedCount.setColumns(10);

		this.contentPanel.add(panelFields);
	}

	@Override
	public void createBindings()
	{
		super.createBindings();
		LengthConverter lengthConverter = new LengthConverter();
		IntegerConverter integerConverter = new IntegerConverter();

		MutableLocationProxy offsets = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.feeder, "offsets", offsets, "location");
		this.addWrappedBinding(offsets, "lengthX", this.textFieldOffsetsX, "text", lengthConverter);
		this.addWrappedBinding(offsets, "lengthY", this.textFieldOffsetsY, "text", lengthConverter);

		this.addWrappedBinding(this.feeder, "trayCountX", this.textFieldTrayCountX, "text", integerConverter);
		this.addWrappedBinding(this.feeder, "trayCountY", this.textFieldTrayCountY, "text", integerConverter);

		this.addWrappedBinding(this.feeder, "feedCount", this.textFieldFeedCount, "text", integerConverter);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldOffsetsX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldOffsetsY);

		ComponentDecorators.decorateWithAutoSelect(this.textFieldTrayCountX);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldTrayCountY);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldFeedCount);
	}

	@Override
	protected void saveToModel()
	{
		super.saveToModel();
		if (this.feeder.getOffsets().getX() == 0 && this.feeder.getTrayCountX() > 1)
			MessageBoxes.errorBox(this, "Error", "X offset must be greater than 0 if X tray count is greater than 1 or feed failure will occur.");
		if (this.feeder.getOffsets().getY() == 0 && this.feeder.getTrayCountY() > 1)
			MessageBoxes.errorBox(this, "Error", "Y offset must be greater than 0 if Y tray count is greater than 1 or feed failure will occur.");
	}
}
