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

package org.openpnp.gui.components;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openpnp.gui.MainFrame;
import org.openpnp.gui.support.Helpers;
import org.openpnp.gui.support.Icons;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.Location;
import org.openpnp.spi.Actuator;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Head;
import org.openpnp.spi.HeadMountable;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;

/**
 * A JPanel of 4 small buttons that assist in setting locations. The buttons are
 * Capture Camera Coordinates, Capture Tool Coordinates, Move Camera to
 * Coordinates and Move Tool to Coordinates. If the actuatorId property is set,
 * this causes the component to use the specified Actuator in place of the tool.
 */
@SuppressWarnings("serial")
public class LocationButtonsPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private JTextField			textFieldX, textFieldY, textFieldZ, textFieldC;
	private String				actuatorName;

	private JButton	buttonCenterTool;
	private JButton	buttonCaptureCamera;
	private JButton	buttonCaptureTool;

	private Action captureCameraCoordinatesAction = new AbstractAction("Get Camera Coordinates", Icons.captureCamera)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SHORT_DESCRIPTION, "Capture the location that the camera is centered on.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				Location l = LocationButtonsPanel.this.getCamera().getLocation();
				Helpers.copyLocationIntoTextFields(l, LocationButtonsPanel.this.textFieldX, LocationButtonsPanel.this.textFieldY, null, LocationButtonsPanel.this.textFieldC);
			});
		}
	};

	private Action captureToolCoordinatesAction = new AbstractAction("Get Tool Coordinates", Icons.captureTool)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SHORT_DESCRIPTION, "Capture the location that the tool is centered on.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				Location l = LocationButtonsPanel.this.getTool().getLocation();
				Helpers.copyLocationIntoTextFields(l, LocationButtonsPanel.this.textFieldX, LocationButtonsPanel.this.textFieldY, LocationButtonsPanel.this.textFieldZ,
						LocationButtonsPanel.this.textFieldC);
			});
		}
	};

	private Action captureActuatorCoordinatesAction = new AbstractAction("Get Actuator Coordinates", Icons.capturePin)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SHORT_DESCRIPTION, "Capture the location that the actuator is centered on.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				Actuator actuator = LocationButtonsPanel.this.getActuator();
				if (actuator == null)
					return;
				Helpers.copyLocationIntoTextFields(actuator.getLocation(), LocationButtonsPanel.this.textFieldX, LocationButtonsPanel.this.textFieldY, LocationButtonsPanel.this.textFieldZ,
						LocationButtonsPanel.this.textFieldC);
			});

		}
	};

	private Action positionCameraAction = new AbstractAction("Position Camera", Icons.centerCamera)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SHORT_DESCRIPTION, "Position the camera over the center of the location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				Camera camera = LocationButtonsPanel.this.getCamera();
				Location location = LocationButtonsPanel.this.getParsedLocation();
				MovableUtils.moveToLocationAtSafeZ(camera, location, 1.0);
			});
		}
	};

	private Action positionToolAction = new AbstractAction("Position Tool", Icons.centerTool)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SHORT_DESCRIPTION, "Position the tool over the center of the location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				HeadMountable tool = LocationButtonsPanel.this.getTool();
				Location location = LocationButtonsPanel.this.getParsedLocation();
				MovableUtils.moveToLocationAtSafeZ(tool, location, 1.0);
			});
		}
	};

	private Action positionToolNoSafeZAction = new AbstractAction("Position Tool (Without Safe Z)", Icons.centerToolNoSafeZ)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SHORT_DESCRIPTION, "Position the tool over the center of the location without first moving to Safe Z.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				HeadMountable tool = LocationButtonsPanel.this.getTool();
				Location location = LocationButtonsPanel.this.getParsedLocation();
				tool.moveTo(location, 1.0);
			});
		}
	};

	private Action positionActuatorAction = new AbstractAction("Position Actuator", Icons.centerPin)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SHORT_DESCRIPTION, "Position the actuator over the center of the location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				Actuator actuator = LocationButtonsPanel.this.getActuator();
				Location location = LocationButtonsPanel.this.getParsedLocation();
				MovableUtils.moveToLocationAtSafeZ(actuator, location, 1.0);
			});
		}
	};

	private JButton buttonCenterToolNoSafeZ;

	public LocationButtonsPanel(JTextField textFieldX, JTextField textFieldY, JTextField textFieldZ, JTextField textFieldC)
	{
		FlowLayout flowLayout = (FlowLayout) this.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(2);
		this.textFieldX = textFieldX;
		this.textFieldY = textFieldY;
		this.textFieldZ = textFieldZ;
		this.textFieldC = textFieldC;

		this.buttonCaptureCamera = new JButton(this.captureCameraCoordinatesAction);
		this.buttonCaptureCamera.setHideActionText(true);
		this.add(this.buttonCaptureCamera);

		this.buttonCaptureTool = new JButton(this.captureToolCoordinatesAction);
		this.buttonCaptureTool.setHideActionText(true);
		this.add(this.buttonCaptureTool);

		JButton buttonCenterCamera = new JButton(this.positionCameraAction);
		buttonCenterCamera.setHideActionText(true);
		this.add(buttonCenterCamera);

		this.buttonCenterTool = new JButton(this.positionToolAction);
		this.buttonCenterTool.setHideActionText(true);
		this.add(this.buttonCenterTool);

		this.buttonCenterToolNoSafeZ = new JButton(this.positionToolNoSafeZAction);
		this.buttonCenterToolNoSafeZ.setHideActionText(true);

		this.setActuatorName(null);
	}

	/**
	 * Get the Actuator with the name provided by setActuatorName() that is on
	 * the same Head as the tool from getTool().
	 * 
	 * @return
	 * @throws Exception
	 */
	public Actuator getActuator() throws Exception
	{
		if (this.actuatorName == null)
			return null;
		HeadMountable tool = this.getTool();
		Head head = tool.getHead();
		Actuator actuator = head.getActuator(this.actuatorName);
		if (actuator == null)
			throw new Exception(String.format("No Actuator with name %s on Head %s", this.actuatorName, head.getName()));
		return actuator;
	}

	public String getActuatorName()
	{
		return this.actuatorName;
	}

	public Camera getCamera() throws Exception
	{
		return this.getTool().getHead().getDefaultCamera();
	}

	private Location getParsedLocation()
	{
		double x = 0, y = 0, z = 0, rotation = 0;
		if (this.textFieldX != null)
			x = Length.parse(this.textFieldX.getText()).getValue();
		if (this.textFieldY != null)
			y = Length.parse(this.textFieldY.getText()).getValue();
		if (this.textFieldZ != null)
			z = Length.parse(this.textFieldZ.getText()).getValue();
		if (this.textFieldC != null)
			rotation = Double.parseDouble(this.textFieldC.getText());
		return new Location(Configuration.get().getSystemUnits(), x, y, z, rotation);
	}

	public HeadMountable getTool() throws Exception
	{
		return MainFrame.machineControlsPanel.getSelectedNozzle();
	}

	public void setActuatorName(String actuatorName)
	{
		this.actuatorName = actuatorName;
		if (actuatorName == null || actuatorName.trim().length() == 0)
		{
			this.buttonCaptureTool.setAction(this.captureToolCoordinatesAction);
			this.buttonCenterTool.setAction(this.positionToolAction);
		} else
		{
			this.buttonCaptureTool.setAction(this.captureActuatorCoordinatesAction);
			this.buttonCenterTool.setAction(this.positionActuatorAction);
		}
	}

	public void setShowPositionToolNoSafeZ(boolean b)
	{
		if (b)
			this.add(this.buttonCenterToolNoSafeZ);
		else
			this.remove(this.buttonCenterToolNoSafeZ);
	}
}
