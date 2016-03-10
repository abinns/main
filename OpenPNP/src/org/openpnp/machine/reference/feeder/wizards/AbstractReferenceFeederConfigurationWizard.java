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

import java.awt.Color;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.components.LocationButtonsPanel;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.IdentifiableListCellRenderer;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.gui.support.PartsComboBoxModel;
import org.openpnp.machine.reference.ReferenceFeeder;
import org.openpnp.model.Configuration;
import org.openpnp.model.Part;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
/**
 * TODO: This should become it's own property sheet which the feeders can
 * include.
 */
public abstract class AbstractReferenceFeederConfigurationWizard extends AbstractConfigurationWizard
{
	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1L;
	private final ReferenceFeeder	feeder;
	private final boolean			includePickLocation;

	private JPanel		panelLocation;
	private JLabel		lblX_1;
	private JLabel		lblY_1;
	private JLabel		lblZ;
	private JLabel		lblRotation;
	private JTextField	textFieldLocationX;
	private JTextField	textFieldLocationY;
	private JTextField	textFieldLocationZ;
	private JTextField	textFieldLocationC;
	private JPanel		panelPart;

	private JComboBox				comboBoxPart;
	private LocationButtonsPanel	locationButtonsPanel;

	public AbstractReferenceFeederConfigurationWizard(ReferenceFeeder feeder)
	{
		this(feeder, true);
	}

	public AbstractReferenceFeederConfigurationWizard(ReferenceFeeder feeder, boolean includePickLocation)
	{
		this.feeder = feeder;
		this.includePickLocation = includePickLocation;

		this.panelPart = new JPanel();
		this.panelPart.setBorder(new TitledBorder(null, "Part", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(this.panelPart);
		this.panelPart.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.comboBoxPart = new JComboBox();
		try
		{
			this.comboBoxPart.setModel(new PartsComboBoxModel());
		} catch (Throwable t)
		{
			// Swallow this error. This happens during parsing in
			// in WindowBuilder but doesn't happen during normal run.
		}
		this.comboBoxPart.setRenderer(new IdentifiableListCellRenderer<Part>());
		this.panelPart.add(this.comboBoxPart, "2, 2, left, default");

		if (includePickLocation)
		{
			this.panelLocation = new JPanel();
			this.panelLocation.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Pick Location", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			this.contentPanel.add(this.panelLocation);
			this.panelLocation.setLayout(new FormLayout(new ColumnSpec[]
			{ FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:default:grow"), },
					new RowSpec[]
			{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

			this.lblX_1 = new JLabel("X");
			this.panelLocation.add(this.lblX_1, "2, 2");

			this.lblY_1 = new JLabel("Y");
			this.panelLocation.add(this.lblY_1, "4, 2");

			this.lblZ = new JLabel("Z");
			this.panelLocation.add(this.lblZ, "6, 2");

			this.lblRotation = new JLabel("Rotation");
			this.panelLocation.add(this.lblRotation, "8, 2");

			this.textFieldLocationX = new JTextField();
			this.panelLocation.add(this.textFieldLocationX, "2, 4");
			this.textFieldLocationX.setColumns(8);

			this.textFieldLocationY = new JTextField();
			this.panelLocation.add(this.textFieldLocationY, "4, 4");
			this.textFieldLocationY.setColumns(8);

			this.textFieldLocationZ = new JTextField();
			this.panelLocation.add(this.textFieldLocationZ, "6, 4");
			this.textFieldLocationZ.setColumns(8);

			this.textFieldLocationC = new JTextField();
			this.panelLocation.add(this.textFieldLocationC, "8, 4");
			this.textFieldLocationC.setColumns(8);

			this.locationButtonsPanel = new LocationButtonsPanel(this.textFieldLocationX, this.textFieldLocationY, this.textFieldLocationZ, this.textFieldLocationC);
			this.panelLocation.add(this.locationButtonsPanel, "10, 4");
		}
	}

	@Override
	public void createBindings()
	{
		LengthConverter lengthConverter = new LengthConverter();
		DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());

		this.addWrappedBinding(this.feeder, "part", this.comboBoxPart, "selectedItem");

		if (this.includePickLocation)
		{
			MutableLocationProxy location = new MutableLocationProxy();
			this.bind(UpdateStrategy.READ_WRITE, this.feeder, "location", location, "location");
			this.addWrappedBinding(location, "lengthX", this.textFieldLocationX, "text", lengthConverter);
			this.addWrappedBinding(location, "lengthY", this.textFieldLocationY, "text", lengthConverter);
			this.addWrappedBinding(location, "lengthZ", this.textFieldLocationZ, "text", lengthConverter);
			this.addWrappedBinding(location, "rotation", this.textFieldLocationC, "text", doubleConverter);
			ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldLocationX);
			ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldLocationY);
			ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldLocationZ);
			ComponentDecorators.decorateWithAutoSelect(this.textFieldLocationC);
		}
	}
}
