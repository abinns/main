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
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.CameraView;
import org.openpnp.gui.components.CameraViewActionEvent;
import org.openpnp.gui.components.CameraViewActionListener;
import org.openpnp.gui.components.CameraViewFilter;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.components.LocationButtonsPanel;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.Helpers;
import org.openpnp.gui.support.IdentifiableListCellRenderer;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.gui.support.PartsComboBoxModel;
import org.openpnp.machine.reference.feeder.ReferenceStripFeeder;
import org.openpnp.machine.reference.feeder.ReferenceStripFeeder.TapeType;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.Location;
import org.openpnp.model.Part;
import org.openpnp.spi.Camera;
import org.openpnp.util.VisionUtils;
import org.openpnp.vision.FluentCv;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class ReferenceStripFeederConfigurationWizard extends AbstractConfigurationWizard
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ReferenceStripFeeder feeder;

	private JPanel panelPart;

	private JComboBox comboBoxPart;

	private JTextField				textFieldFeedStartX;
	private JTextField				textFieldFeedStartY;
	private JTextField				textFieldFeedStartZ;
	private JTextField				textFieldFeedEndX;
	private JTextField				textFieldFeedEndY;
	private JTextField				textFieldFeedEndZ;
	private JTextField				textFieldTapeWidth;
	private JLabel					lblPartPitch;
	private JTextField				textFieldPartPitch;
	private JPanel					panelTapeSettings;
	private JPanel					panelLocations;
	private LocationButtonsPanel	locationButtonsPanelFeedStart;
	private LocationButtonsPanel	locationButtonsPanelFeedEnd;
	private JLabel					lblFeedCount;
	private JTextField				textFieldFeedCount;
	private JButton					btnResetFeedCount;
	private JLabel					lblTapeType;
	private JComboBox				comboBoxTapeType;
	private JLabel					lblRotationInTape;
	private JTextField				textFieldLocationRotation;
	private JButton					btnAutoSetup;

	private Location		firstPartLocation;
	private Location		secondPartLocation;
	private List<Location>	part1HoleLocations;
	private Camera			autoSetupCamera;

	private Action autoSetup = new AbstractAction("Auto Setup")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				ReferenceStripFeederConfigurationWizard.this.autoSetupCamera = Configuration.get().getMachine().getDefaultHead().getDefaultCamera();
			} catch (Exception ex)
			{
				MessageBoxes.errorBox(ReferenceStripFeederConfigurationWizard.this.getTopLevelAncestor(), "Auto Setup Failure", ex);
				return;
			}

			ReferenceStripFeederConfigurationWizard.this.btnAutoSetup.setAction(ReferenceStripFeederConfigurationWizard.this.autoSetupCancel);

			CameraView cameraView = MainFrame.cameraPanel.getCameraView(ReferenceStripFeederConfigurationWizard.this.autoSetupCamera);
			cameraView.addActionListener(ReferenceStripFeederConfigurationWizard.this.autoSetupPart1Clicked);
			cameraView.setText("Click on the center of the first part in the tape.");
			cameraView.flash();

			final boolean showDetails = (e.getModifiers() & ActionEvent.ALT_MASK) != 0;

			cameraView.setCameraViewFilter(new CameraViewFilter()
			{
				@Override
				public BufferedImage filterCameraImage(Camera camera, BufferedImage image)
				{
					return ReferenceStripFeederConfigurationWizard.this.showHoles(camera, image, showDetails);
				}
			});
		}
	};

	private Action autoSetupCancel = new AbstractAction("Cancel Auto Setup")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			ReferenceStripFeederConfigurationWizard.this.btnAutoSetup.setAction(ReferenceStripFeederConfigurationWizard.this.autoSetup);
			CameraView cameraView = MainFrame.cameraPanel.getCameraView(ReferenceStripFeederConfigurationWizard.this.autoSetupCamera);
			cameraView.setText(null);
			cameraView.setCameraViewFilter(null);
			cameraView.removeActionListener(ReferenceStripFeederConfigurationWizard.this.autoSetupPart1Clicked);
			cameraView.removeActionListener(ReferenceStripFeederConfigurationWizard.this.autoSetupPart2Clicked);
		}
	};

	private CameraViewActionListener autoSetupPart1Clicked = new CameraViewActionListener()
	{
		@Override
		public void actionPerformed(final CameraViewActionEvent action)
		{
			ReferenceStripFeederConfigurationWizard.this.firstPartLocation = action.getLocation();
			final CameraView cameraView = MainFrame.cameraPanel.getCameraView(ReferenceStripFeederConfigurationWizard.this.autoSetupCamera);
			cameraView.removeActionListener(this);
			Configuration.get().getMachine().submit(new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					cameraView.setText("Checking first part...");
					ReferenceStripFeederConfigurationWizard.this.autoSetupCamera.moveTo(action.getLocation(), 1.0);
					ReferenceStripFeederConfigurationWizard.this.part1HoleLocations = ReferenceStripFeederConfigurationWizard.this
							.findHoles(ReferenceStripFeederConfigurationWizard.this.autoSetupCamera);

					cameraView.setText("Now click on the center of the second part in the tape.");
					cameraView.flash();

					cameraView.addActionListener(ReferenceStripFeederConfigurationWizard.this.autoSetupPart2Clicked);
					return null;
				}
			}, new FutureCallback<Void>()
			{
				@Override
				public void onFailure(final Throwable t)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							ReferenceStripFeederConfigurationWizard.this.autoSetupCancel.actionPerformed(null);
							MessageBoxes.errorBox(ReferenceStripFeederConfigurationWizard.this.getTopLevelAncestor(), "Auto Setup Failure", t);
						}
					});
				}

				@Override
				public void onSuccess(Void result)
				{
				}
			});
		}
	};

	private CameraViewActionListener autoSetupPart2Clicked = new CameraViewActionListener()
	{
		@Override
		public void actionPerformed(final CameraViewActionEvent action)
		{
			ReferenceStripFeederConfigurationWizard.this.secondPartLocation = action.getLocation();
			final CameraView cameraView = MainFrame.cameraPanel.getCameraView(ReferenceStripFeederConfigurationWizard.this.autoSetupCamera);
			cameraView.removeActionListener(this);
			Configuration.get().getMachine().submit(new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					cameraView.setText("Checking second part...");
					ReferenceStripFeederConfigurationWizard.this.autoSetupCamera.moveTo(action.getLocation(), 1.0);
					List<Location> part2HoleLocations = ReferenceStripFeederConfigurationWizard.this.findHoles(ReferenceStripFeederConfigurationWizard.this.autoSetupCamera);

					List<Location> referenceHoles = ReferenceStripFeederConfigurationWizard.this.deriveReferenceHoles(ReferenceStripFeederConfigurationWizard.this.part1HoleLocations,
							part2HoleLocations);
					final Location referenceHole1 = referenceHoles.get(0);
					final Location referenceHole2 = referenceHoles.get(1);

					ReferenceStripFeederConfigurationWizard.this.feeder.setReferenceHoleLocation(referenceHole1);
					ReferenceStripFeederConfigurationWizard.this.feeder.setLastHoleLocation(referenceHole2);

					Length partPitch = ReferenceStripFeederConfigurationWizard.this.firstPartLocation.getLinearLengthTo(ReferenceStripFeederConfigurationWizard.this.secondPartLocation);
					partPitch.setValue(2 * Math.round(partPitch.getValue() / 2));

					final Length partPitch_ = partPitch;
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							Helpers.copyLocationIntoTextFields(referenceHole1, ReferenceStripFeederConfigurationWizard.this.textFieldFeedStartX,
									ReferenceStripFeederConfigurationWizard.this.textFieldFeedStartY, ReferenceStripFeederConfigurationWizard.this.textFieldFeedStartZ);
							Helpers.copyLocationIntoTextFields(referenceHole2, ReferenceStripFeederConfigurationWizard.this.textFieldFeedEndX,
									ReferenceStripFeederConfigurationWizard.this.textFieldFeedEndY, ReferenceStripFeederConfigurationWizard.this.textFieldFeedEndZ);
							ReferenceStripFeederConfigurationWizard.this.textFieldPartPitch.setText(partPitch_.getValue() + "");
						}
					});

					ReferenceStripFeederConfigurationWizard.this.feeder.setFeedCount(1);
					ReferenceStripFeederConfigurationWizard.this.autoSetupCamera.moveTo(ReferenceStripFeederConfigurationWizard.this.feeder.getPickLocation(), 1.0);
					ReferenceStripFeederConfigurationWizard.this.feeder.setFeedCount(0);

					cameraView.setText("Setup complete!");
					Thread.sleep(1500);
					cameraView.setText(null);
					cameraView.setCameraViewFilter(null);
					ReferenceStripFeederConfigurationWizard.this.btnAutoSetup.setAction(ReferenceStripFeederConfigurationWizard.this.autoSetup);

					return null;
				}
			}, new FutureCallback<Void>()
			{
				@Override
				public void onFailure(final Throwable t)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							ReferenceStripFeederConfigurationWizard.this.autoSetupCancel.actionPerformed(null);
							MessageBoxes.errorBox(ReferenceStripFeederConfigurationWizard.this.getTopLevelAncestor(), "Auto Setup Failure", t);
						}
					});
				}

				@Override
				public void onSuccess(Void result)
				{
				}
			});
		}
	};

	private JCheckBox chckbxUseVision;

	private JLabel lblUseVision;

	public ReferenceStripFeederConfigurationWizard(ReferenceStripFeeder feeder)
	{
		this.feeder = feeder;

		this.panelPart = new JPanel();
		this.panelPart.setBorder(new TitledBorder(null, "Part", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(this.panelPart);
		this.panelPart.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

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
		this.panelPart.add(this.comboBoxPart, "2, 2, 3, 1, left, default");

		this.lblRotationInTape = new JLabel("Rotation In Tape");
		this.panelPart.add(this.lblRotationInTape, "2, 4, left, default");

		this.textFieldLocationRotation = new JTextField();
		this.panelPart.add(this.textFieldLocationRotation, "4, 4, fill, default");
		this.textFieldLocationRotation.setColumns(4);

		this.panelTapeSettings = new JPanel();
		this.contentPanel.add(this.panelTapeSettings);
		this.panelTapeSettings.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Tape Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.panelTapeSettings.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, },
				new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.btnAutoSetup = new JButton(this.autoSetup);
		this.panelTapeSettings.add(this.btnAutoSetup, "2, 2, 11, 1");

		this.lblTapeType = new JLabel("Tape Type");
		this.panelTapeSettings.add(this.lblTapeType, "2, 4, right, default");

		this.comboBoxTapeType = new JComboBox(TapeType.values());
		this.panelTapeSettings.add(this.comboBoxTapeType, "4, 4, fill, default");

		JLabel lblTapeWidth = new JLabel("Tape Width");
		this.panelTapeSettings.add(lblTapeWidth, "8, 4, right, default");

		this.textFieldTapeWidth = new JTextField();
		this.panelTapeSettings.add(this.textFieldTapeWidth, "10, 4");
		this.textFieldTapeWidth.setColumns(5);

		this.lblPartPitch = new JLabel("Part Pitch");
		this.panelTapeSettings.add(this.lblPartPitch, "2, 6, right, default");

		this.textFieldPartPitch = new JTextField();
		this.panelTapeSettings.add(this.textFieldPartPitch, "4, 6");
		this.textFieldPartPitch.setColumns(5);

		this.lblFeedCount = new JLabel("Feed Count");
		this.panelTapeSettings.add(this.lblFeedCount, "8, 6, right, default");

		this.textFieldFeedCount = new JTextField();
		this.panelTapeSettings.add(this.textFieldFeedCount, "10, 6");
		this.textFieldFeedCount.setColumns(10);

		this.btnResetFeedCount = new JButton(new AbstractAction("Reset")
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				ReferenceStripFeederConfigurationWizard.this.textFieldFeedCount.setText("0");
				ReferenceStripFeederConfigurationWizard.this.applyAction.actionPerformed(e);
			}
		});
		this.panelTapeSettings.add(this.btnResetFeedCount, "12, 6");

		this.lblUseVision = new JLabel("Use Vision?");
		this.panelTapeSettings.add(this.lblUseVision, "2, 8");

		this.chckbxUseVision = new JCheckBox("");
		this.panelTapeSettings.add(this.chckbxUseVision, "4, 8");

		this.panelLocations = new JPanel();
		this.contentPanel.add(this.panelLocations);
		this.panelLocations.setBorder(new TitledBorder(null, "Locations", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.panelLocations.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblX = new JLabel("X");
		this.panelLocations.add(lblX, "4, 2");

		JLabel lblY = new JLabel("Y");
		this.panelLocations.add(lblY, "6, 2");

		JLabel lblZ_1 = new JLabel("Z");
		this.panelLocations.add(lblZ_1, "8, 2");

		JLabel lblFeedStartLocation = new JLabel("Reference Hole Location");
		lblFeedStartLocation.setToolTipText("The location of the first tape hole past the first part in the direction of more parts.");
		this.panelLocations.add(lblFeedStartLocation, "2, 4, right, default");

		this.textFieldFeedStartX = new JTextField();
		this.panelLocations.add(this.textFieldFeedStartX, "4, 4");
		this.textFieldFeedStartX.setColumns(8);

		this.textFieldFeedStartY = new JTextField();
		this.panelLocations.add(this.textFieldFeedStartY, "6, 4");
		this.textFieldFeedStartY.setColumns(8);

		this.textFieldFeedStartZ = new JTextField();
		this.panelLocations.add(this.textFieldFeedStartZ, "8, 4");
		this.textFieldFeedStartZ.setColumns(8);

		this.locationButtonsPanelFeedStart = new LocationButtonsPanel(this.textFieldFeedStartX, this.textFieldFeedStartY, this.textFieldFeedStartZ, null);
		this.panelLocations.add(this.locationButtonsPanelFeedStart, "10, 4");

		JLabel lblFeedEndLocation = new JLabel("Next Hole Location");
		lblFeedEndLocation.setToolTipText("The location of another hole after the reference hole. This can be any hole along the tape as long as it's past the reference hole.");
		this.panelLocations.add(lblFeedEndLocation, "2, 6, right, default");

		this.textFieldFeedEndX = new JTextField();
		this.panelLocations.add(this.textFieldFeedEndX, "4, 6");
		this.textFieldFeedEndX.setColumns(8);

		this.textFieldFeedEndY = new JTextField();
		this.panelLocations.add(this.textFieldFeedEndY, "6, 6");
		this.textFieldFeedEndY.setColumns(8);

		this.textFieldFeedEndZ = new JTextField();
		this.panelLocations.add(this.textFieldFeedEndZ, "8, 6");
		this.textFieldFeedEndZ.setColumns(8);

		this.locationButtonsPanelFeedEnd = new LocationButtonsPanel(this.textFieldFeedEndX, this.textFieldFeedEndY, this.textFieldFeedEndZ, null);
		this.panelLocations.add(this.locationButtonsPanelFeedEnd, "10, 6");
	}

	@Override
	public void createBindings()
	{
		LengthConverter lengthConverter = new LengthConverter();
		IntegerConverter intConverter = new IntegerConverter();
		DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());

		MutableLocationProxy location = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.feeder, "location", location, "location");
		this.addWrappedBinding(location, "rotation", this.textFieldLocationRotation, "text", doubleConverter);

		this.addWrappedBinding(this.feeder, "part", this.comboBoxPart, "selectedItem");
		this.addWrappedBinding(this.feeder, "tapeType", this.comboBoxTapeType, "selectedItem");

		this.addWrappedBinding(this.feeder, "tapeWidth", this.textFieldTapeWidth, "text", lengthConverter);
		this.addWrappedBinding(this.feeder, "partPitch", this.textFieldPartPitch, "text", lengthConverter);
		this.addWrappedBinding(this.feeder, "feedCount", this.textFieldFeedCount, "text", intConverter);

		MutableLocationProxy feedStartLocation = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.feeder, "referenceHoleLocation", feedStartLocation, "location");
		this.addWrappedBinding(feedStartLocation, "lengthX", this.textFieldFeedStartX, "text", lengthConverter);
		this.addWrappedBinding(feedStartLocation, "lengthY", this.textFieldFeedStartY, "text", lengthConverter);
		this.addWrappedBinding(feedStartLocation, "lengthZ", this.textFieldFeedStartZ, "text", lengthConverter);

		MutableLocationProxy feedEndLocation = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.feeder, "lastHoleLocation", feedEndLocation, "location");
		this.addWrappedBinding(feedEndLocation, "lengthX", this.textFieldFeedEndX, "text", lengthConverter);
		this.addWrappedBinding(feedEndLocation, "lengthY", this.textFieldFeedEndY, "text", lengthConverter);
		this.addWrappedBinding(feedEndLocation, "lengthZ", this.textFieldFeedEndZ, "text", lengthConverter);

		this.addWrappedBinding(this.feeder, "visionEnabled", this.chckbxUseVision, "selected");

		ComponentDecorators.decorateWithAutoSelect(this.textFieldLocationRotation);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldTapeWidth);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldPartPitch);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldFeedCount);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedStartX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedStartY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedStartZ);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedEndX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedEndY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldFeedEndZ);
	}

	private List<Location> deriveReferenceHoles(List<Location> part1HoleLocations, List<Location> part2HoleLocations)
	{
		// We are only interested in the pair of holes closest to each part
		part1HoleLocations = part1HoleLocations.subList(0, Math.min(2, part1HoleLocations.size()));
		part2HoleLocations = part2HoleLocations.subList(0, Math.min(2, part2HoleLocations.size()));

		// Part 1's reference hole is the one closest to either of part 2's
		// holes.
		Location part1ReferenceHole = VisionUtils.sortLocationsByDistance(part2HoleLocations.get(0), part1HoleLocations).get(0);
		// Part 2's reference hole is the one farthest from part 1's reference
		// hole.
		Location part2ReferenceHole = Lists.reverse(VisionUtils.sortLocationsByDistance(part1ReferenceHole, part2HoleLocations)).get(0);

		List<Location> referenceHoles = new ArrayList<>();
		referenceHoles.add(part1ReferenceHole);
		referenceHoles.add(part2ReferenceHole);
		return referenceHoles;
	}

	private List<Location> findHoles(Camera camera)
	{
		List<Location> holeLocations = new ArrayList<>();
		new FluentCv().setCamera(camera).settleAndCapture().toGray().blurGaussian(this.feeder.getHoleBlurKernelSize())
				.findCirclesHough(this.feeder.getHoleDiameterMin(), this.feeder.getHoleDiameterMax(), this.feeder.getHolePitchMin())
				.filterCirclesByDistance(this.feeder.getHoleDistanceMin(), this.feeder.getHoleDistanceMax()).filterCirclesToLine(this.feeder.getHoleLineDistanceMax())
				.convertCirclesToLocations(holeLocations);
		return holeLocations;
	}

	/**
	 * Show candidate holes in the image. Red are any holes that are found. Blue
	 * is holes that passed the distance check but failed the line check. Green
	 * passed all checks and are good.
	 * 
	 * @param camera
	 * @param image
	 * @return
	 */
	private BufferedImage showHoles(Camera camera, BufferedImage image, boolean showDetails)
	{
		if (showDetails)
			return new FluentCv().setCamera(camera).toMat(image, "original").toGray().blurGaussian(this.feeder.getHoleBlurKernelSize())
					.findCirclesHough(this.feeder.getHoleDiameterMin(), this.feeder.getHoleDiameterMax(), this.feeder.getHolePitchMin(), "houghUnfiltered")
					.drawCircles("original", Color.red, "unfiltered").recall("houghUnfiltered")
					.filterCirclesByDistance(this.feeder.getHoleDistanceMin(), this.feeder.getHoleDistanceMax(), "houghDistanceFiltered").drawCircles("unfiltered", Color.blue, "distanceFiltered")
					.recall("houghDistanceFiltered").filterCirclesToLine(this.feeder.getHoleLineDistanceMax()).drawCircles("distanceFiltered", Color.green).toBufferedImage();
		else
			return new FluentCv().setCamera(camera).toMat(image, "original").toGray().blurGaussian(this.feeder.getHoleBlurKernelSize())
					.findCirclesHough(this.feeder.getHoleDiameterMin(), this.feeder.getHoleDiameterMax(), this.feeder.getHolePitchMin())
					.filterCirclesByDistance(this.feeder.getHoleDistanceMin(), this.feeder.getHoleDistanceMax()).filterCirclesToLine(this.feeder.getHoleLineDistanceMax())
					.drawCircles("original", Color.green).toBufferedImage();
	}
}
