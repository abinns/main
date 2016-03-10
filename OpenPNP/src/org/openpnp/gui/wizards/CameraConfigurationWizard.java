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

package org.openpnp.gui.wizards;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.CameraView;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.LongConverter;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Camera;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class CameraConfigurationWizard extends AbstractConfigurationWizard
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private final Camera		camera;
	private JPanel				panelUpp;
	private JButton				btnMeasure;
	private JButton				btnCancelMeasure;
	private JLabel				lblUppInstructions;

	private Action measureAction = new AbstractAction("Measure")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			CameraConfigurationWizard.this.btnMeasure.setAction(CameraConfigurationWizard.this.confirmMeasureAction);
			CameraConfigurationWizard.this.cancelMeasureAction.setEnabled(true);
			CameraView cameraView = MainFrame.cameraPanel.setSelectedCamera(CameraConfigurationWizard.this.camera);
			cameraView.setSelectionEnabled(true);
			cameraView.setSelection(0, 0, 100, 100);
		}
	};

	private Action confirmMeasureAction = new AbstractAction("Confirm")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			CameraConfigurationWizard.this.btnMeasure.setAction(CameraConfigurationWizard.this.measureAction);
			CameraConfigurationWizard.this.cancelMeasureAction.setEnabled(false);
			CameraView cameraView = MainFrame.cameraPanel.getCameraView(CameraConfigurationWizard.this.camera);
			cameraView.setSelectionEnabled(false);
			Rectangle selection = cameraView.getSelection();
			double width = Double.parseDouble(CameraConfigurationWizard.this.textFieldWidth.getText());
			double height = Double.parseDouble(CameraConfigurationWizard.this.textFieldHeight.getText());
			CameraConfigurationWizard.this.textFieldUppX.setText(String.format(Locale.US, Configuration.get().getLengthDisplayFormat(), width / selection.width));
			CameraConfigurationWizard.this.textFieldUppY.setText(String.format(Locale.US, Configuration.get().getLengthDisplayFormat(), height / selection.height));
		}
	};

	private Action cancelMeasureAction = new AbstractAction("Cancel")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			CameraConfigurationWizard.this.btnMeasure.setAction(CameraConfigurationWizard.this.measureAction);
			CameraConfigurationWizard.this.cancelMeasureAction.setEnabled(false);
			CameraView cameraView = MainFrame.cameraPanel.getCameraView(CameraConfigurationWizard.this.camera);
			cameraView.setSelectionEnabled(false);
		}
	};

	private JTextField textFieldWidth;

	private JTextField	textFieldHeight;
	private JTextField	textFieldUppX;
	private JTextField	textFieldUppY;
	private JLabel		lblWidth;
	private JLabel		lblHeight;
	private JLabel		lblX;
	private JLabel		lblY;
	private JPanel		panelVision;
	private JLabel		lblSettleTimems;
	private JTextField	textFieldSettleTime;

	public CameraConfigurationWizard(Camera camera)
	{
		this.camera = camera;

		this.panelUpp = new JPanel();
		this.contentPanel.add(this.panelUpp);
		this.panelUpp.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Units Per Pixel", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.panelUpp.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, },
				new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.lblWidth = new JLabel("Width");
		this.panelUpp.add(this.lblWidth, "2, 2");

		this.lblHeight = new JLabel("Height");
		this.panelUpp.add(this.lblHeight, "4, 2");

		this.lblX = new JLabel("X");
		this.panelUpp.add(this.lblX, "6, 2");

		this.lblY = new JLabel("Y");
		this.panelUpp.add(this.lblY, "8, 2");

		this.textFieldWidth = new JTextField();
		this.textFieldWidth.setText("1");
		this.panelUpp.add(this.textFieldWidth, "2, 4");
		this.textFieldWidth.setColumns(8);

		this.textFieldHeight = new JTextField();
		this.textFieldHeight.setText("1");
		this.panelUpp.add(this.textFieldHeight, "4, 4");
		this.textFieldHeight.setColumns(8);

		this.textFieldUppX = new JTextField();
		this.textFieldUppX.setColumns(8);
		this.panelUpp.add(this.textFieldUppX, "6, 4, fill, default");

		this.textFieldUppY = new JTextField();
		this.textFieldUppY.setColumns(8);
		this.panelUpp.add(this.textFieldUppY, "8, 4, fill, default");

		this.btnMeasure = new JButton("Measure");
		this.btnMeasure.setAction(this.measureAction);
		this.panelUpp.add(this.btnMeasure, "10, 4");

		this.btnCancelMeasure = new JButton("Cancel");
		this.btnCancelMeasure.setAction(this.cancelMeasureAction);
		this.panelUpp.add(this.btnCancelMeasure, "12, 4");

		this.lblUppInstructions = new JLabel(
				"<html>\n<ol>\n<li>Place an object with a known width and height on the table. Graphing paper is a good, easy choice for this.\n<li>Enter the width and height of the object into the Width and Height fields.\n<li>Jog the camera to where it is centered over the object and in focus.\n<li>Press Measure and use the camera selection rectangle to measure the object. Press Confirm when finished.\n<li>The calculated units per pixel values will be inserted into the X and Y fields.\n</ol>\n</html>");
		this.panelUpp.add(this.lblUppInstructions, "2, 6, 10, 1, default, fill");

		this.panelVision = new JPanel();
		this.panelVision.setBorder(new TitledBorder(null, "Vision", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(this.panelVision);
		this.panelVision.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.lblSettleTimems = new JLabel("Settle Time (ms)");
		this.panelVision.add(this.lblSettleTimems, "2, 2, right, default");

		this.textFieldSettleTime = new JTextField();
		this.panelVision.add(this.textFieldSettleTime, "4, 2, fill, default");
		this.textFieldSettleTime.setColumns(10);
	}

	@Override
	public void createBindings()
	{
		LengthConverter lengthConverter = new LengthConverter();
		LongConverter longConverter = new LongConverter();

		MutableLocationProxy unitsPerPixel = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.camera, "unitsPerPixel", unitsPerPixel, "location");
		this.addWrappedBinding(unitsPerPixel, "lengthX", this.textFieldUppX, "text", lengthConverter);
		this.addWrappedBinding(unitsPerPixel, "lengthY", this.textFieldUppY, "text", lengthConverter);

		this.addWrappedBinding(this.camera, "settleTimeMs", this.textFieldSettleTime, "text", longConverter);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldUppX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldUppY);

		ComponentDecorators.decorateWithAutoSelect(this.textFieldWidth);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldHeight);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldSettleTime);
	}
}
