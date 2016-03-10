package org.openpnp.machine.reference.wizards;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.CameraView;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.components.LocationButtonsPanel;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.machine.reference.ReferenceCamera;
import org.openpnp.model.Configuration;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class ReferenceCameraConfigurationWizard extends AbstractConfigurationWizard
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ReferenceCamera referenceCamera;

	private JTextField				textFieldOffX;
	private JTextField				textFieldOffY;
	private JTextField				textFieldOffZ;
	private JPanel					panelOffsets;
	private JPanel					panelGeneral;
	private JLabel					lblRotation;
	private JTextField				textFieldRotation;
	private JPanel					panelLocation;
	private JLabel					lblX;
	private JLabel					lblY;
	private JLabel					lblZ;
	private JLabel					lblRotation_1;
	private JTextField				textFieldLocationX;
	private JTextField				textFieldLocationY;
	private JTextField				textFieldLocationZ;
	private JTextField				textFieldLocationRotation;
	private LocationButtonsPanel	locationButtonsPanel;
	private JCheckBox				chckbxFlipX;
	private JLabel					lblFlipX;
	private JLabel					lblFlipY;
	private JCheckBox				checkBoxFlipY;
	private JTextField				textFieldSafeZ;
	private JLabel					lblOffsetX;
	private JLabel					lblOffsetY;
	private JTextField				textFieldOffsetX;
	private JTextField				textFieldOffsetY;
	private JPanel					panelLensCalibration;
	private JLabel					lblApplyCalibration;
	private JCheckBox				calibrationEnabledChk;

	private Action startCalibration = new AbstractAction("Start Lens Calibration")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			MainFrame.cameraPanel.setSelectedCamera(ReferenceCameraConfigurationWizard.this.referenceCamera);

			ReferenceCameraConfigurationWizard.this.startLensCalibrationBtn.setAction(ReferenceCameraConfigurationWizard.this.cancelCalibration);

			CameraView cameraView = MainFrame.cameraPanel.getCameraView(ReferenceCameraConfigurationWizard.this.referenceCamera);
			String message = "Go to https://github.com/openpnp/openpnp/wiki/Camera-Lens-Calibration for detailed instructions.\n"
					+ "When you have your calibration card ready, hold it in front of the camera so that the entire card is visible.\n"
					+ "Each time the screen flashes an image is captured. After the flash you should move the card to a new orientation.";
			cameraView.setText(message);
			cameraView.flash();

			ReferenceCameraConfigurationWizard.this.referenceCamera.startCalibration((progressCurrent, progressMax, finished) -> {
				if (finished)
				{
					cameraView.setText(null);
					ReferenceCameraConfigurationWizard.this.startLensCalibrationBtn.setAction(ReferenceCameraConfigurationWizard.this.startCalibration);
				} else
					cameraView.setText(String.format("Captured %d of %d.\nMove the card to a new position and angle each time the screen flashes.", progressCurrent, progressMax));
				cameraView.flash();
			});
		}
	};

	private Action cancelCalibration = new AbstractAction("Cancel Lens Calibration")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			ReferenceCameraConfigurationWizard.this.startLensCalibrationBtn.setAction(ReferenceCameraConfigurationWizard.this.startCalibration);

			ReferenceCameraConfigurationWizard.this.referenceCamera.cancelCalibration();

			CameraView cameraView = MainFrame.cameraPanel.getCameraView(ReferenceCameraConfigurationWizard.this.referenceCamera);
			cameraView.setText(null);
			cameraView.flash();
		}
	};

	private JButton startLensCalibrationBtn;

	public ReferenceCameraConfigurationWizard(ReferenceCamera referenceCamera)
	{
		this.referenceCamera = referenceCamera;

		this.panelOffsets = new JPanel();
		this.contentPanel.add(this.panelOffsets);
		this.panelOffsets.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Offsets", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.panelOffsets.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel olblX = new JLabel("X");
		this.panelOffsets.add(olblX, "2, 2");

		JLabel olblY = new JLabel("Y");
		this.panelOffsets.add(olblY, "4, 2");

		JLabel olblZ = new JLabel("Z");
		this.panelOffsets.add(olblZ, "6, 2");

		this.textFieldOffX = new JTextField();
		this.panelOffsets.add(this.textFieldOffX, "2, 4");
		this.textFieldOffX.setColumns(8);

		this.textFieldOffY = new JTextField();
		this.panelOffsets.add(this.textFieldOffY, "4, 4");
		this.textFieldOffY.setColumns(8);

		this.textFieldOffZ = new JTextField();
		this.panelOffsets.add(this.textFieldOffZ, "6, 4");
		this.textFieldOffZ.setColumns(8);

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

		this.panelGeneral = new JPanel();
		this.panelGeneral.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Transformation", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.contentPanel.add(this.panelGeneral);
		this.panelGeneral.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.lblRotation = new JLabel("Rotation");
		this.panelGeneral.add(this.lblRotation, "2, 2, right, default");

		this.textFieldRotation = new JTextField();
		this.panelGeneral.add(this.textFieldRotation, "4, 2");
		this.textFieldRotation.setColumns(10);

		this.lblOffsetX = new JLabel("Offset X");
		this.panelGeneral.add(this.lblOffsetX, "2, 4, right, default");

		this.textFieldOffsetX = new JTextField();
		this.panelGeneral.add(this.textFieldOffsetX, "4, 4");
		this.textFieldOffsetX.setColumns(10);

		this.lblOffsetY = new JLabel("Offset Y");
		this.panelGeneral.add(this.lblOffsetY, "2, 6, right, default");

		this.textFieldOffsetY = new JTextField();
		this.panelGeneral.add(this.textFieldOffsetY, "4, 6");
		this.textFieldOffsetY.setColumns(10);

		this.lblFlipX = new JLabel("Flip Vertical");
		this.panelGeneral.add(this.lblFlipX, "2, 8, right, default");

		this.chckbxFlipX = new JCheckBox("");
		this.panelGeneral.add(this.chckbxFlipX, "4, 8");

		this.lblFlipY = new JLabel("Flip Horizontal");
		this.panelGeneral.add(this.lblFlipY, "2, 10, right, default");

		this.checkBoxFlipY = new JCheckBox("");
		this.panelGeneral.add(this.checkBoxFlipY, "4, 10");

		this.panelLocation = new JPanel();
		this.panelLocation.setBorder(new TitledBorder(null, "Location", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(this.panelLocation);
		this.panelLocation.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

		this.lblX = new JLabel("X");
		this.panelLocation.add(this.lblX, "2, 2");

		this.lblY = new JLabel("Y");
		this.panelLocation.add(this.lblY, "4, 2");

		this.lblZ = new JLabel("Z");
		this.panelLocation.add(this.lblZ, "6, 2");

		this.lblRotation_1 = new JLabel("Rotation");
		this.panelLocation.add(this.lblRotation_1, "8, 2");

		this.textFieldLocationX = new JTextField();
		this.panelLocation.add(this.textFieldLocationX, "2, 4, fill, default");
		this.textFieldLocationX.setColumns(8);

		this.textFieldLocationY = new JTextField();
		this.panelLocation.add(this.textFieldLocationY, "4, 4, fill, default");
		this.textFieldLocationY.setColumns(8);

		this.textFieldLocationZ = new JTextField();
		this.panelLocation.add(this.textFieldLocationZ, "6, 4, fill, default");
		this.textFieldLocationZ.setColumns(8);

		this.textFieldLocationRotation = new JTextField();
		this.panelLocation.add(this.textFieldLocationRotation, "8, 4, fill, default");
		this.textFieldLocationRotation.setColumns(8);

		this.panelLensCalibration = new JPanel();
		this.panelLensCalibration.setBorder(new TitledBorder(null, "Lens Calibration", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(this.panelLensCalibration);
		this.panelLensCalibration.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.startLensCalibrationBtn = new JButton(this.startCalibration);
		this.panelLensCalibration.add(this.startLensCalibrationBtn, "2, 2, 3, 1");

		this.lblApplyCalibration = new JLabel("Apply Calibration?");
		this.panelLensCalibration.add(this.lblApplyCalibration, "2, 4, right, default");

		this.calibrationEnabledChk = new JCheckBox("");
		this.panelLensCalibration.add(this.calibrationEnabledChk, "4, 4");

		try
		{
			// Causes WindowBuilder to fail, so just throw away the error.
			if (referenceCamera.getHead() == null)
			{
				this.locationButtonsPanel = new LocationButtonsPanel(this.textFieldLocationX, this.textFieldLocationY, this.textFieldLocationZ, this.textFieldLocationRotation);
				this.panelLocation.add(this.locationButtonsPanel, "10, 4, fill, fill");
				this.panelOffsets.setVisible(false);
			} else
				this.panelLocation.setVisible(false);
		} catch (Exception e)
		{

		}
	}

	@Override
	public void createBindings()
	{
		IntegerConverter intConverter = new IntegerConverter();
		DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());
		LengthConverter lengthConverter = new LengthConverter();

		if (this.referenceCamera.getHead() == null)
		{
			// fixed camera
			MutableLocationProxy headOffsets = new MutableLocationProxy();
			this.bind(UpdateStrategy.READ_WRITE, this.referenceCamera, "headOffsets", headOffsets, "location");
			this.addWrappedBinding(headOffsets, "lengthX", this.textFieldLocationX, "text", lengthConverter);
			this.addWrappedBinding(headOffsets, "lengthY", this.textFieldLocationY, "text", lengthConverter);
			this.addWrappedBinding(headOffsets, "lengthZ", this.textFieldLocationZ, "text", lengthConverter);
			this.addWrappedBinding(headOffsets, "rotation", this.textFieldLocationRotation, "text", doubleConverter);
		} else
		{
			// moving camera
			MutableLocationProxy headOffsets = new MutableLocationProxy();
			this.bind(UpdateStrategy.READ_WRITE, this.referenceCamera, "headOffsets", headOffsets, "location");
			this.addWrappedBinding(headOffsets, "lengthX", this.textFieldOffX, "text", lengthConverter);
			this.addWrappedBinding(headOffsets, "lengthY", this.textFieldOffY, "text", lengthConverter);
			this.addWrappedBinding(headOffsets, "lengthZ", this.textFieldOffZ, "text", lengthConverter);
		}

		this.addWrappedBinding(this.referenceCamera, "rotation", this.textFieldRotation, "text", doubleConverter);
		this.addWrappedBinding(this.referenceCamera, "offsetX", this.textFieldOffsetX, "text", intConverter);
		this.addWrappedBinding(this.referenceCamera, "offsetY", this.textFieldOffsetY, "text", intConverter);
		this.addWrappedBinding(this.referenceCamera, "flipX", this.chckbxFlipX, "selected");
		this.addWrappedBinding(this.referenceCamera, "flipY", this.checkBoxFlipY, "selected");
		this.addWrappedBinding(this.referenceCamera, "safeZ", this.textFieldSafeZ, "text", lengthConverter);

		this.bind(UpdateStrategy.READ_WRITE, this.referenceCamera.getCalibration(), "enabled", this.calibrationEnabledChk, "selected");
		// addWrappedBinding(referenceCamera.getCalibration(), "enabled",
		// calibrationEnabledChk,
		// "selected");

		ComponentDecorators.decorateWithAutoSelect(this.textFieldRotation);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldOffsetX);
		ComponentDecorators.decorateWithAutoSelect(this.textFieldOffsetY);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldOffX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldOffY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldOffZ);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldLocationX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldLocationY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldLocationZ);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldLocationRotation);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldSafeZ);
	}
}
