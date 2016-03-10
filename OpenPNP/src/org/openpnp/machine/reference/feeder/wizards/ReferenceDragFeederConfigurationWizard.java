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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.CameraView;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.components.LocationButtonsPanel;
import org.openpnp.gui.support.BufferedImageIconConverter;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.machine.reference.feeder.ReferenceDragFeeder;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Camera;
import org.openpnp.util.UiUtils;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class ReferenceDragFeederConfigurationWizard extends AbstractReferenceFeederConfigurationWizard
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ReferenceDragFeeder feeder;

	private JTextField				textFieldFeedStartX;
	private JTextField				textFieldFeedStartY;
	private JTextField				textFieldFeedStartZ;
	private JTextField				textFieldFeedEndX;
	private JTextField				textFieldFeedEndY;
	private JTextField				textFieldFeedEndZ;
	private JTextField				textFieldFeedRate;
	private JLabel					lblActuatorId;
	private JTextField				textFieldActuatorId;
	private JPanel					panelGeneral;
	private JPanel					panelVision;
	private JPanel					panelLocations;
	private JCheckBox				chckbxVisionEnabled;
	private JPanel					panelVisionEnabled;
	private JPanel					panelTemplate;
	private JLabel					labelTemplateImage;
	private JButton					btnChangeTemplateImage;
	private JSeparator				separator;
	private JPanel					panelVisionTemplateAndAoe;
	private JPanel					panelAoE;
	private JLabel					lblX_1;
	private JLabel					lblY_1;
	private JTextField				textFieldAoiX;
	private JTextField				textFieldAoiY;
	private JTextField				textFieldAoiWidth;
	private JTextField				textFieldAoiHeight;
	private LocationButtonsPanel	locationButtonsPanelFeedStart;
	private LocationButtonsPanel	locationButtonsPanelFeedEnd;
	private JLabel					lblWidth;
	private JLabel					lblHeight;
	private JButton					btnChangeAoi;
	private JButton					btnCancelChangeAoi;
	private JPanel					panel;
	private JButton					btnCancelChangeTemplateImage;

	@SuppressWarnings("serial")
	private Action selectTemplateImageAction = new AbstractAction("Select")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				Camera camera = MainFrame.machineControlsPanel.getSelectedTool().getHead().getDefaultCamera();
				CameraView cameraView = MainFrame.cameraPanel.setSelectedCamera(camera);

				cameraView.setSelectionEnabled(true);
				// org.openpnp.model.Rectangle r =
				// feeder.getVision().getTemplateImageCoordinates();
				org.openpnp.model.Rectangle r = null;
				if (r == null || r.getWidth() == 0 || r.getHeight() == 0)
					cameraView.setSelection(0, 0, 100, 100);
				else
				{
					// cameraView.setSelection(r.getLeft(), r.getTop(),
					// r.getWidth(), r.getHeight());
				}
				ReferenceDragFeederConfigurationWizard.this.btnChangeTemplateImage.setAction(ReferenceDragFeederConfigurationWizard.this.confirmSelectTemplateImageAction);
				ReferenceDragFeederConfigurationWizard.this.cancelSelectTemplateImageAction.setEnabled(true);
			});
		}
	};

	@SuppressWarnings("serial")
	private Action confirmSelectTemplateImageAction = new AbstractAction("Confirm")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				Camera camera = MainFrame.machineControlsPanel.getSelectedTool().getHead().getDefaultCamera();
				CameraView cameraView = MainFrame.cameraPanel.setSelectedCamera(camera);

				BufferedImage image = cameraView.captureSelectionImage();
				if (image == null)
					MessageBoxes.errorBox(ReferenceDragFeederConfigurationWizard.this, "No Image Selected", "Please select an area of the camera image using the mouse.");
				else
					ReferenceDragFeederConfigurationWizard.this.labelTemplateImage.setIcon(new ImageIcon(image));
				cameraView.setSelectionEnabled(false);
				ReferenceDragFeederConfigurationWizard.this.btnChangeTemplateImage.setAction(ReferenceDragFeederConfigurationWizard.this.selectTemplateImageAction);
				ReferenceDragFeederConfigurationWizard.this.cancelSelectTemplateImageAction.setEnabled(false);
			});
		}
	};

	@SuppressWarnings("serial")
	private Action cancelSelectTemplateImageAction = new AbstractAction("Cancel")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				Camera camera = MainFrame.machineControlsPanel.getSelectedTool().getHead().getDefaultCamera();
				CameraView cameraView = MainFrame.cameraPanel.setSelectedCamera(camera);

				ReferenceDragFeederConfigurationWizard.this.btnChangeTemplateImage.setAction(ReferenceDragFeederConfigurationWizard.this.selectTemplateImageAction);
				ReferenceDragFeederConfigurationWizard.this.cancelSelectTemplateImageAction.setEnabled(false);
				cameraView.setSelectionEnabled(false);
			});
		}
	};

	@SuppressWarnings("serial")
	private Action selectAoiAction = new AbstractAction("Select")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				Camera camera = MainFrame.machineControlsPanel.getSelectedTool().getHead().getDefaultCamera();
				CameraView cameraView = MainFrame.cameraPanel.setSelectedCamera(camera);

				ReferenceDragFeederConfigurationWizard.this.btnChangeAoi.setAction(ReferenceDragFeederConfigurationWizard.this.confirmSelectAoiAction);
				ReferenceDragFeederConfigurationWizard.this.cancelSelectAoiAction.setEnabled(true);

				cameraView.setSelectionEnabled(true);
				org.openpnp.model.Rectangle r = ReferenceDragFeederConfigurationWizard.this.feeder.getVision().getAreaOfInterest();
				if (r == null || r.getWidth() == 0 || r.getHeight() == 0)
					cameraView.setSelection(0, 0, 100, 100);
				else
					cameraView.setSelection(r.getX(), r.getY(), r.getWidth(), r.getHeight());
			});
		}
	};

	@SuppressWarnings("serial")
	private Action confirmSelectAoiAction = new AbstractAction("Confirm")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				Camera camera = MainFrame.machineControlsPanel.getSelectedTool().getHead().getDefaultCamera();
				CameraView cameraView = MainFrame.cameraPanel.setSelectedCamera(camera);

				ReferenceDragFeederConfigurationWizard.this.btnChangeAoi.setAction(ReferenceDragFeederConfigurationWizard.this.selectAoiAction);
				ReferenceDragFeederConfigurationWizard.this.cancelSelectAoiAction.setEnabled(false);

				cameraView.setSelectionEnabled(false);
				final Rectangle rect = cameraView.getSelection();
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						ReferenceDragFeederConfigurationWizard.this.textFieldAoiX.setText(Integer.toString(rect.x));
						ReferenceDragFeederConfigurationWizard.this.textFieldAoiY.setText(Integer.toString(rect.y));
						ReferenceDragFeederConfigurationWizard.this.textFieldAoiWidth.setText(Integer.toString(rect.width));
						ReferenceDragFeederConfigurationWizard.this.textFieldAoiHeight.setText(Integer.toString(rect.height));
					}
				});
			});
		}
	};

	@SuppressWarnings("serial")
	private Action cancelSelectAoiAction = new AbstractAction("Cancel")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				Camera camera = MainFrame.machineControlsPanel.getSelectedTool().getHead().getDefaultCamera();
				CameraView cameraView = MainFrame.cameraPanel.setSelectedCamera(camera);

				ReferenceDragFeederConfigurationWizard.this.btnChangeAoi.setAction(ReferenceDragFeederConfigurationWizard.this.selectAoiAction);
				ReferenceDragFeederConfigurationWizard.this.cancelSelectAoiAction.setEnabled(false);
				ReferenceDragFeederConfigurationWizard.this.btnChangeAoi.setAction(ReferenceDragFeederConfigurationWizard.this.selectAoiAction);
				ReferenceDragFeederConfigurationWizard.this.cancelSelectAoiAction.setEnabled(false);
				cameraView.setSelectionEnabled(false);
			});
		}
	};

	public ReferenceDragFeederConfigurationWizard(ReferenceDragFeeder feeder)
	{
		super(feeder);
		this.feeder = feeder;

		JPanel panelFields = new JPanel();
		panelFields.setLayout(new BoxLayout(panelFields, BoxLayout.Y_AXIS));

		this.panelGeneral = new JPanel();
		this.panelGeneral.setBorder(new TitledBorder(null, "General Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		panelFields.add(this.panelGeneral);
		this.panelGeneral.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblFeedRate = new JLabel("Feed Speed (0 - 1)");
		this.panelGeneral.add(lblFeedRate, "2, 2");

		this.textFieldFeedRate = new JTextField();
		this.panelGeneral.add(this.textFieldFeedRate, "4, 2");
		this.textFieldFeedRate.setColumns(5);

		this.lblActuatorId = new JLabel("Actuator Name");
		this.panelGeneral.add(this.lblActuatorId, "2, 4, right, default");

		this.textFieldActuatorId = new JTextField();
		this.panelGeneral.add(this.textFieldActuatorId, "4, 4");
		this.textFieldActuatorId.setColumns(5);

		this.panelLocations = new JPanel();
		panelFields.add(this.panelLocations);
		this.panelLocations.setBorder(new TitledBorder(null, "Locations", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.panelLocations.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblX = new JLabel("X");
		this.panelLocations.add(lblX, "4, 4");

		JLabel lblY = new JLabel("Y");
		this.panelLocations.add(lblY, "6, 4");

		JLabel lblZ = new JLabel("Z");
		this.panelLocations.add(lblZ, "8, 4");

		JLabel lblFeedStartLocation = new JLabel("Feed Start Location");
		this.panelLocations.add(lblFeedStartLocation, "2, 6, right, default");

		this.textFieldFeedStartX = new JTextField();
		this.panelLocations.add(this.textFieldFeedStartX, "4, 6");
		this.textFieldFeedStartX.setColumns(8);

		this.textFieldFeedStartY = new JTextField();
		this.panelLocations.add(this.textFieldFeedStartY, "6, 6");
		this.textFieldFeedStartY.setColumns(8);

		this.textFieldFeedStartZ = new JTextField();
		this.panelLocations.add(this.textFieldFeedStartZ, "8, 6");
		this.textFieldFeedStartZ.setColumns(8);

		this.locationButtonsPanelFeedStart = new LocationButtonsPanel(this.textFieldFeedStartX, this.textFieldFeedStartY, this.textFieldFeedStartZ, null);
		this.panelLocations.add(this.locationButtonsPanelFeedStart, "10, 6");

		JLabel lblFeedEndLocation = new JLabel("Feed End Location");
		this.panelLocations.add(lblFeedEndLocation, "2, 8, right, default");

		this.textFieldFeedEndX = new JTextField();
		this.panelLocations.add(this.textFieldFeedEndX, "4, 8");
		this.textFieldFeedEndX.setColumns(8);

		this.textFieldFeedEndY = new JTextField();
		this.panelLocations.add(this.textFieldFeedEndY, "6, 8");
		this.textFieldFeedEndY.setColumns(8);

		this.textFieldFeedEndZ = new JTextField();
		this.panelLocations.add(this.textFieldFeedEndZ, "8, 8");
		this.textFieldFeedEndZ.setColumns(8);

		this.locationButtonsPanelFeedEnd = new LocationButtonsPanel(this.textFieldFeedEndX, this.textFieldFeedEndY, this.textFieldFeedEndZ, null);
		this.panelLocations.add(this.locationButtonsPanelFeedEnd, "10, 8");
		//
		this.panelVision = new JPanel();
		this.panelVision.setBorder(new TitledBorder(null, "Vision", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelFields.add(this.panelVision);
		this.panelVision.setLayout(new BoxLayout(this.panelVision, BoxLayout.Y_AXIS));

		this.panelVisionEnabled = new JPanel();
		FlowLayout fl_panelVisionEnabled = (FlowLayout) this.panelVisionEnabled.getLayout();
		fl_panelVisionEnabled.setAlignment(FlowLayout.LEFT);
		this.panelVision.add(this.panelVisionEnabled);

		this.chckbxVisionEnabled = new JCheckBox("Vision Enabled?");
		this.panelVisionEnabled.add(this.chckbxVisionEnabled);

		this.separator = new JSeparator();
		this.panelVision.add(this.separator);

		this.panelVisionTemplateAndAoe = new JPanel();
		this.panelVision.add(this.panelVisionTemplateAndAoe);
		this.panelVisionTemplateAndAoe.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.panelTemplate = new JPanel();
		this.panelTemplate.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Template Image", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.panelVisionTemplateAndAoe.add(this.panelTemplate, "2, 2, center, fill");
		this.panelTemplate.setLayout(new BoxLayout(this.panelTemplate, BoxLayout.Y_AXIS));

		this.labelTemplateImage = new JLabel("");
		this.labelTemplateImage.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.panelTemplate.add(this.labelTemplateImage);
		this.labelTemplateImage.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		this.labelTemplateImage.setMinimumSize(new Dimension(150, 150));
		this.labelTemplateImage.setMaximumSize(new Dimension(150, 150));
		this.labelTemplateImage.setHorizontalAlignment(SwingConstants.CENTER);
		this.labelTemplateImage.setSize(new Dimension(150, 150));
		this.labelTemplateImage.setPreferredSize(new Dimension(150, 150));

		this.panel = new JPanel();
		this.panelTemplate.add(this.panel);

		this.btnChangeTemplateImage = new JButton(this.selectTemplateImageAction);
		this.panel.add(this.btnChangeTemplateImage);
		this.btnChangeTemplateImage.setAlignmentX(Component.CENTER_ALIGNMENT);

		this.btnCancelChangeTemplateImage = new JButton(this.cancelSelectTemplateImageAction);
		this.panel.add(this.btnCancelChangeTemplateImage);

		this.panelAoE = new JPanel();
		this.panelAoE.setBorder(new TitledBorder(null, "Area of Interest", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.panelVisionTemplateAndAoe.add(this.panelAoE, "4, 2, fill, fill");
		this.panelAoE.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, },
				new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.lblX_1 = new JLabel("X");
		this.panelAoE.add(this.lblX_1, "2, 2");

		this.lblY_1 = new JLabel("Y");
		this.panelAoE.add(this.lblY_1, "4, 2");

		this.lblWidth = new JLabel("Width");
		this.panelAoE.add(this.lblWidth, "6, 2");

		this.lblHeight = new JLabel("Height");
		this.panelAoE.add(this.lblHeight, "8, 2");

		this.textFieldAoiX = new JTextField();
		this.panelAoE.add(this.textFieldAoiX, "2, 4, fill, default");
		this.textFieldAoiX.setColumns(5);

		this.textFieldAoiY = new JTextField();
		this.panelAoE.add(this.textFieldAoiY, "4, 4, fill, default");
		this.textFieldAoiY.setColumns(5);

		this.textFieldAoiWidth = new JTextField();
		this.panelAoE.add(this.textFieldAoiWidth, "6, 4, fill, default");
		this.textFieldAoiWidth.setColumns(5);

		this.textFieldAoiHeight = new JTextField();
		this.panelAoE.add(this.textFieldAoiHeight, "8, 4, fill, default");
		this.textFieldAoiHeight.setColumns(5);

		this.btnChangeAoi = new JButton("Change");
		this.btnChangeAoi.setAction(this.selectAoiAction);
		this.panelAoE.add(this.btnChangeAoi, "10, 4");

		this.btnCancelChangeAoi = new JButton("Cancel");
		this.btnCancelChangeAoi.setAction(this.cancelSelectAoiAction);
		this.panelAoE.add(this.btnCancelChangeAoi, "12, 4");

		this.cancelSelectTemplateImageAction.setEnabled(false);
		this.cancelSelectAoiAction.setEnabled(false);

		this.contentPanel.add(panelFields);
	}

	@Override
	public void createBindings()
	{
		super.createBindings();
		LengthConverter lengthConverter = new LengthConverter();
		IntegerConverter intConverter = new IntegerConverter();
		DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());
		BufferedImageIconConverter imageConverter = new BufferedImageIconConverter();

		this.addWrappedBinding(this.feeder, "feedSpeed", this.textFieldFeedRate, "text", doubleConverter);
		this.addWrappedBinding(this.feeder, "actuatorName", this.textFieldActuatorId, "text");

		MutableLocationProxy feedStartLocation = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.feeder, "feedStartLocation", feedStartLocation, "location");
		this.addWrappedBinding(feedStartLocation, "lengthX", this.textFieldFeedStartX, "text", lengthConverter);
		this.addWrappedBinding(feedStartLocation, "lengthY", this.textFieldFeedStartY, "text", lengthConverter);
		this.addWrappedBinding(feedStartLocation, "lengthZ", this.textFieldFeedStartZ, "text", lengthConverter);

		MutableLocationProxy feedEndLocation = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.feeder, "feedEndLocation", feedEndLocation, "location");
		this.addWrappedBinding(feedEndLocation, "lengthX", this.textFieldFeedEndX, "text", lengthConverter);
		this.addWrappedBinding(feedEndLocation, "lengthY", this.textFieldFeedEndY, "text", lengthConverter);
		this.addWrappedBinding(feedEndLocation, "lengthZ", this.textFieldFeedEndZ, "text", lengthConverter);

		this.addWrappedBinding(this.feeder, "vision.enabled", this.chckbxVisionEnabled, "selected");
		this.addWrappedBinding(this.feeder, "vision.templateImage", this.labelTemplateImage, "icon", imageConverter);

		this.addWrappedBinding(this.feeder, "vision.areaOfInterest.x", this.textFieldAoiX, "text", intConverter);
		this.addWrappedBinding(this.feeder, "vision.areaOfInterest.y", this.textFieldAoiY, "text", intConverter);

		this.addWrappedBinding(this.feeder, "vision.areaOfInterest.width", this.textFieldAoiWidth, "text", intConverter);
		this.addWrappedBinding(this.feeder, "vision.areaOfInterest.height", this.textFieldAoiHeight, "text", intConverter);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedRate);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldActuatorId);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedStartX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedStartY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedStartZ);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedEndX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedEndY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedEndZ);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldAoiX);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldAoiY);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldAoiWidth);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldAoiHeight);

		BeanProperty actuatorIdProperty = BeanProperty.create("actuatorId");
		Bindings.createAutoBinding(UpdateStrategy.READ, this.feeder, actuatorIdProperty, this.locationButtonsPanelFeedStart, actuatorIdProperty).bind();
		Bindings.createAutoBinding(UpdateStrategy.READ, this.feeder, actuatorIdProperty, this.locationButtonsPanelFeedEnd, actuatorIdProperty).bind();
	}
}
