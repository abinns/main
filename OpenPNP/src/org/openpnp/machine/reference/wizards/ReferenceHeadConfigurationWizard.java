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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.ApplyResetBindingListener;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.JBindings.WrappedBinding;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.support.WizardContainer;
import org.openpnp.machine.reference.ReferenceHead;
import org.openpnp.model.Configuration;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class ReferenceHeadConfigurationWizard extends JPanel implements Wizard
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ReferenceHead head;

	private JCheckBox	chckbxSoftLimitsEnabled;
	private JTextField	textFieldFeedRate;
	private JTextField	textFieldPickDwell;
	private JTextField	textFieldPlaceDwell;
	private JLabel		lblNewLabel;
	private JLabel		lblX;
	private JLabel		lblY;
	private JLabel		lblZ;
	private JLabel		lblC;
	private JTextField	textFieldSoftLimitsXMin;
	private JTextField	textFieldSoftLimitsXMax;
	private JLabel		lblMinimum;
	private JLabel		lblMacimum;
	private JTextField	textFieldSoftLimitsYMin;
	private JTextField	textFieldSoftLimitsYMax;
	private JTextField	textFieldSoftLimitsZMin;
	private JTextField	textFieldSoftLimitsZMax;
	private JTextField	textFieldSoftLimitsCMin;
	private JTextField	textFieldSoftLimitsCMax;
	private JCheckBox	chckbxVisionEnabled;
	private JLabel		lblHomingDotDiameter;
	private JLabel		lblNewLabel_1;
	private JTextField	textFieldHomingDotDiameter;
	private JLabel		lblX_1;
	private JLabel		lblY_1;
	private JLabel		lblZ_1;
	private JTextField	textFieldHomingDotX;
	private JTextField	textFieldHomingDotY;
	private JTextField	textFieldHomingDotZ;
	private JButton		btnSave;
	private JButton		btnCancel;

	private WizardContainer	wizardContainer;
	private JPanel			panelGeneral;
	private JPanel			panelSoftLimits;
	private JPanel			panelHoming;
	private JPanel			panelVision;
	private JPanel			panelActions;
	private JLabel			lblX_2;
	private JLabel			lblY_2;
	private JLabel			lblZ_2;
	private JLabel			lblC_1;
	private JLabel			lblHomeLocation;
	private JTextField		textFieldHomeLocationX;
	private JTextField		textFieldHomeLocationY;
	private JTextField		textFieldHomeLocationZ;
	private JTextField		textFieldHomeLocationC;
	private JScrollPane		scrollPane;
	private JPanel			panelMain;

	private List<WrappedBinding> wrappedBindings = new ArrayList<>();

	private Action saveAction = new AbstractAction("Apply")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			ReferenceHeadConfigurationWizard.this.saveToModel();
			ReferenceHeadConfigurationWizard.this.wizardContainer.wizardCompleted(ReferenceHeadConfigurationWizard.this);
		}
	};

	private Action cancelAction = new AbstractAction("Reset")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			ReferenceHeadConfigurationWizard.this.loadFromModel();
		}
	};

	private JLabel label;

	private JTextField textFieldSafeZ;

	// TODO: Most of what this class did is deprecated and has been moved into
	// Nozzles, Actuators and Cameras. We may still want to do softlimits, but
	// these will likely move to the driver. Revisit this and see what is and
	// isn't needed.
	public ReferenceHeadConfigurationWizard(ReferenceHead head)
	{
		this.head = head;

		this.setLayout(new BorderLayout(0, 0));

		this.panelMain = new JPanel();

		this.scrollPane = new JScrollPane(this.panelMain);
		this.scrollPane.getVerticalScrollBar().setUnitIncrement(Configuration.get().getVerticalScrollUnitIncrement());
		this.scrollPane.setBorder(null);
		this.panelMain.setLayout(new BoxLayout(this.panelMain, BoxLayout.Y_AXIS));

		this.panelGeneral = new JPanel();
		this.panelMain.add(this.panelGeneral);
		this.panelGeneral.setBorder(new TitledBorder(null, "General", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.panelGeneral.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.label = new JLabel("Safe-Z");
		this.panelGeneral.add(this.label, "2, 2, right, default");

		this.textFieldSafeZ = new JTextField();
		this.textFieldSafeZ.setColumns(8);
		this.panelGeneral.add(this.textFieldSafeZ, "4, 2");

		JLabel lblFeedRate = new JLabel("Feed Rate (units/min)");
		this.panelGeneral.add(lblFeedRate, "6, 2, right, default");

		this.textFieldFeedRate = new JTextField();
		this.panelGeneral.add(this.textFieldFeedRate, "8, 2");
		this.textFieldFeedRate.setColumns(8);

		this.lblNewLabel = new JLabel("Pick Dwell (ms)");
		this.panelGeneral.add(this.lblNewLabel, "2, 4, right, default");

		this.textFieldPickDwell = new JTextField();
		this.panelGeneral.add(this.textFieldPickDwell, "4, 4");
		this.textFieldPickDwell.setColumns(8);

		JLabel lblPlaceDwell = new JLabel("Place Dwell (ms)");
		this.panelGeneral.add(lblPlaceDwell, "6, 4, right, default");

		this.textFieldPlaceDwell = new JTextField();
		this.panelGeneral.add(this.textFieldPlaceDwell, "8, 4");
		this.textFieldPlaceDwell.setColumns(8);

		this.panelSoftLimits = new JPanel();
		this.panelMain.add(this.panelSoftLimits);
		this.panelSoftLimits.setBorder(new TitledBorder(null, "Soft Limits", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.panelSoftLimits.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.chckbxSoftLimitsEnabled = new JCheckBox("Soft Limits Enabled?");
		this.panelSoftLimits.add(this.chckbxSoftLimitsEnabled, "2, 2, 5, 1");

		this.lblMinimum = new JLabel("Minimum");
		this.panelSoftLimits.add(this.lblMinimum, "4, 4");

		this.lblMacimum = new JLabel("Maximum");
		this.panelSoftLimits.add(this.lblMacimum, "6, 4");

		this.lblX = new JLabel("X");
		this.panelSoftLimits.add(this.lblX, "2, 6, right, default");

		this.textFieldSoftLimitsXMin = new JTextField();
		this.panelSoftLimits.add(this.textFieldSoftLimitsXMin, "4, 6");
		this.textFieldSoftLimitsXMin.setColumns(5);

		this.textFieldSoftLimitsXMax = new JTextField();
		this.panelSoftLimits.add(this.textFieldSoftLimitsXMax, "6, 6");
		this.textFieldSoftLimitsXMax.setColumns(5);

		this.lblY = new JLabel("Y");
		this.panelSoftLimits.add(this.lblY, "2, 8, right, default");

		this.textFieldSoftLimitsYMin = new JTextField();
		this.panelSoftLimits.add(this.textFieldSoftLimitsYMin, "4, 8");
		this.textFieldSoftLimitsYMin.setColumns(5);

		this.textFieldSoftLimitsYMax = new JTextField();
		this.panelSoftLimits.add(this.textFieldSoftLimitsYMax, "6, 8");
		this.textFieldSoftLimitsYMax.setColumns(5);

		this.lblZ = new JLabel("Z");
		this.panelSoftLimits.add(this.lblZ, "2, 10, right, default");

		this.textFieldSoftLimitsZMin = new JTextField();
		this.panelSoftLimits.add(this.textFieldSoftLimitsZMin, "4, 10");
		this.textFieldSoftLimitsZMin.setColumns(5);

		this.textFieldSoftLimitsZMax = new JTextField();
		this.panelSoftLimits.add(this.textFieldSoftLimitsZMax, "6, 10");
		this.textFieldSoftLimitsZMax.setColumns(5);

		this.lblC = new JLabel("C");
		this.panelSoftLimits.add(this.lblC, "2, 12, right, default");

		this.textFieldSoftLimitsCMin = new JTextField();
		this.panelSoftLimits.add(this.textFieldSoftLimitsCMin, "4, 12");
		this.textFieldSoftLimitsCMin.setColumns(5);

		this.textFieldSoftLimitsCMax = new JTextField();
		this.panelSoftLimits.add(this.textFieldSoftLimitsCMax, "6, 12");
		this.textFieldSoftLimitsCMax.setColumns(5);

		this.panelHoming = new JPanel();
		this.panelMain.add(this.panelHoming);
		this.panelHoming.setBorder(new TitledBorder(null, "Homing", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.panelHoming.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.lblX_2 = new JLabel("X");
		this.panelHoming.add(this.lblX_2, "4, 2, center, default");

		this.lblY_2 = new JLabel("Y");
		this.panelHoming.add(this.lblY_2, "6, 2, center, default");

		this.lblZ_2 = new JLabel("Z");
		this.panelHoming.add(this.lblZ_2, "8, 2, center, default");

		this.lblC_1 = new JLabel("C");
		this.panelHoming.add(this.lblC_1, "10, 2, center, default");

		this.lblHomeLocation = new JLabel("Home Location");
		this.lblHomeLocation.setToolTipText("Coordinates that will be applied when the machine is homed. This is position you want the DROs to show after homing.");
		this.panelHoming.add(this.lblHomeLocation, "2, 4, right, default");

		this.textFieldHomeLocationX = new JTextField();
		this.panelHoming.add(this.textFieldHomeLocationX, "4, 4, fill, default");
		this.textFieldHomeLocationX.setColumns(5);

		this.textFieldHomeLocationY = new JTextField();
		this.panelHoming.add(this.textFieldHomeLocationY, "6, 4, fill, default");
		this.textFieldHomeLocationY.setColumns(5);

		this.textFieldHomeLocationZ = new JTextField();
		this.panelHoming.add(this.textFieldHomeLocationZ, "8, 4, fill, default");
		this.textFieldHomeLocationZ.setColumns(5);

		this.textFieldHomeLocationC = new JTextField();
		this.panelHoming.add(this.textFieldHomeLocationC, "10, 4, fill, default");
		this.textFieldHomeLocationC.setColumns(5);

		this.panelVision = new JPanel();
		this.panelMain.add(this.panelVision);
		this.panelVision.setBorder(new TitledBorder(null, "Vision", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.panelVision.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.chckbxVisionEnabled = new JCheckBox("Vision Enabled?");
		this.panelVision.add(this.chckbxVisionEnabled, "2, 2");

		this.lblX_1 = new JLabel("X");
		this.panelVision.add(this.lblX_1, "4, 4, center, default");

		this.lblY_1 = new JLabel("Y");
		this.panelVision.add(this.lblY_1, "6, 4, center, default");

		this.lblZ_1 = new JLabel("Z");
		this.panelVision.add(this.lblZ_1, "8, 4, center, default");

		this.lblNewLabel_1 = new JLabel("Homing Dot Location");
		this.lblNewLabel_1
				.setToolTipText("The location of the homing dot in relation to the Home Location. When Vision is used for homing, this will be applied to the DROs after Vision Homing completes.");
		this.panelVision.add(this.lblNewLabel_1, "2, 6, right, default");

		this.textFieldHomingDotX = new JTextField();
		this.panelVision.add(this.textFieldHomingDotX, "4, 6");
		this.textFieldHomingDotX.setColumns(8);

		this.textFieldHomingDotY = new JTextField();
		this.panelVision.add(this.textFieldHomingDotY, "6, 6");
		this.textFieldHomingDotY.setColumns(8);

		this.textFieldHomingDotZ = new JTextField();
		this.panelVision.add(this.textFieldHomingDotZ, "8, 6");
		this.textFieldHomingDotZ.setColumns(8);

		this.lblHomingDotDiameter = new JLabel("Homing Dot Diameter (mm)");
		this.panelVision.add(this.lblHomingDotDiameter, "2, 8, right, default");

		this.textFieldHomingDotDiameter = new JTextField();
		this.panelVision.add(this.textFieldHomingDotDiameter, "4, 8");
		this.textFieldHomingDotDiameter.setColumns(5);
		this.add(this.scrollPane, BorderLayout.CENTER);

		this.panelActions = new JPanel();
		FlowLayout fl_panelActions = (FlowLayout) this.panelActions.getLayout();
		fl_panelActions.setAlignment(FlowLayout.RIGHT);
		this.add(this.panelActions, BorderLayout.SOUTH);

		this.btnCancel = new JButton(this.cancelAction);
		this.panelActions.add(this.btnCancel);

		this.btnSave = new JButton(this.saveAction);
		this.panelActions.add(this.btnSave);

		this.createBindings();
		this.loadFromModel();
	}

	private void createBindings()
	{
		LengthConverter lengthConverter = new LengthConverter();
		DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());
		IntegerConverter integerConverter = new IntegerConverter();
		ApplyResetBindingListener listener = new ApplyResetBindingListener(this.saveAction, this.cancelAction);
		// wrappedBindings.add(JBindings.bind(head, "safeZ", textFieldSafeZ,
		// "text",
		// lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head, "feedRate",
		// textFieldFeedRate, "text",
		// lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head, "pickDwellMilliseconds",
		// textFieldPickDwell, "text", integerConverter, listener));
		// wrappedBindings.add(JBindings.bind(head, "placeDwellMilliseconds",
		// textFieldPlaceDwell, "text", integerConverter, listener));

		// wrappedBindings.add(JBindings.bind(head, "softLimits.enabled",
		// chckbxSoftLimitsEnabled,
		// "selected", listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "softLimits.minimums.lengthX",
		// textFieldSoftLimitsXMin, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "softLimits.maximums.lengthX",
		// textFieldSoftLimitsXMax, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "softLimits.minimums.lengthY",
		// textFieldSoftLimitsYMin, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "softLimits.maximums.lengthY",
		// textFieldSoftLimitsYMax, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "softLimits.minimums.lengthZ",
		// textFieldSoftLimitsZMin, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "softLimits.maximums.lengthZ",
		// textFieldSoftLimitsZMax, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "softLimits.minimums.rotation",
		// textFieldSoftLimitsCMin, "text", doubleConverter, listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "softLimits.maximums.rotation",
		// textFieldSoftLimitsCMax, "text", doubleConverter, listener));

		// wrappedBindings.add(JBindings.bind(head, "homing.location.lengthX",
		// textFieldHomeLocationX, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head, "homing.location.lengthY",
		// textFieldHomeLocationY, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head, "homing.location.lengthZ",
		// textFieldHomeLocationZ, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head, "homing.location.rotation",
		// textFieldHomeLocationC, "text", doubleConverter, listener));
		//
		// wrappedBindings.add(JBindings.bind(head, "homing.vision.enabled",
		// chckbxVisionEnabled,
		// "selected", listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "homing.vision.homingDotDiameter",
		// textFieldHomingDotDiameter, "text", doubleConverter, listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "homing.vision.homingDotLocation.lengthX",
		// textFieldHomingDotX, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "homing.vision.homingDotLocation.lengthY",
		// textFieldHomingDotY, "text", lengthConverter, listener));
		// wrappedBindings.add(JBindings.bind(head,
		// "homing.vision.homingDotLocation.lengthZ",
		// textFieldHomingDotZ, "text", lengthConverter, listener));
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldSafeZ);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedRate);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldPickDwell);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldPlaceDwell);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldSoftLimitsXMin);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldSoftLimitsXMax);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldSoftLimitsYMin);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldSoftLimitsYMax);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldSoftLimitsZMin);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldSoftLimitsZMax);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldHomeLocationX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldHomeLocationY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldHomeLocationZ);

		ComponentDecorators.decorateWithAutoSelect(this.textFieldHomingDotDiameter);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldHomingDotX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldHomingDotX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldHomingDotX);
	}

	@Override
	public String getWizardName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JPanel getWizardPanel()
	{
		return this;
	}

	private void loadFromModel()
	{
		for (WrappedBinding wrappedBinding : this.wrappedBindings)
			wrappedBinding.reset();
		this.saveAction.setEnabled(false);
		this.cancelAction.setEnabled(false);
	}

	private void saveToModel()
	{
		for (WrappedBinding wrappedBinding : this.wrappedBindings)
			wrappedBinding.save();
		this.saveAction.setEnabled(false);
		this.cancelAction.setEnabled(false);
	}

	@Override
	public void setWizardContainer(WizardContainer wizardContainer)
	{
		this.wizardContainer = wizardContainer;
	}
}
