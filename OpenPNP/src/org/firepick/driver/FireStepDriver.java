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

// This file is intended to support the FireStep motion controller, created by Karl Lew
// (karl@firepick.org).
// More information about the FireStep controller can be found at
// https://github.com/firepick1/firestep
// Note that this implementation currently only supports FirePick Delta, which has rotational delta
// kinematics.
// It should be trivial to add conditional hooks to enable or disable or switch kinematics for other
// configurations.
// - Neil Jansen (njansen1@gmail.com) 7/1/2014

package org.firepick.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import javax.swing.Action;

import org.firepick.driver.wizards.FireStepDriverWizard;
import org.firepick.kinematics.RotatableDeltaKinematicsCalculator;
import org.firepick.model.AngleTriplet;
import org.firepick.model.RawStepTriplet;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.ReferenceActuator;
import org.openpnp.machine.reference.ReferenceHead;
import org.openpnp.machine.reference.ReferenceHeadMountable;
import org.openpnp.machine.reference.ReferenceNozzle;
import org.openpnp.machine.reference.driver.AbstractSerialPortDriver;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.PropertySheetHolder;
import org.simpleframework.xml.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FireStepDriver extends AbstractSerialPortDriver implements Runnable
{
	private static final Logger	logger					= LoggerFactory.getLogger(FireStepDriver.class);
	private static final double	minimumRequiredVersion	= 1.0;

	// NOTE: This is ignored out because FireStep doesn't use feed rates per
	// se.. it just does
	// everything rather quickly and smoothly.
	@Attribute
	private double feedRateMmPerMinute;

	// @Attribute
	private double								nozzleStepsPerDegree	= 8.888888888;
	private boolean								nozzleEnabled			= false;
	private boolean								powerSupplyOn			= false;
	private RotatableDeltaKinematicsCalculator	deltaCalc				= new RotatableDeltaKinematicsCalculator();

	private double			x, y, z, c;
	private Thread			readerThread;
	private boolean			disconnectRequested;
	private Object			commandLock		= new Object();
	private boolean			connected;
	private String			connectedVersion;
	private Queue<String>	responseQueue	= new ConcurrentLinkedQueue<>();

	@Override
	public void actuate(ReferenceActuator actuator, boolean on) throws Exception
	{
		if (actuator.getIndex() == 0)
		{
			// TODO: Currently disabled... We don't have a pin to assign this to
		}
	}

	@Override
	public void actuate(ReferenceActuator actuator, double value) throws Exception
	{
		// dwell();
		// TODO Auto-generated method stub
	}

	@Override
	public synchronized void connect() throws Exception
	{
		super.connect();

		/**
		 * Connection process notes: On some platforms, as soon as we open the
		 * serial port it will reset Grbl and we'll start getting some data. On
		 * others, Grbl may already be running and we will get nothing on
		 * connect.
		 */

		List<String> responses;
		synchronized (this.commandLock)
		{
			// Start the reader thread with the commandLock held. This will
			// keep the thread from quickly parsing any responses messages
			// and notifying before we get a change to wait.
			this.readerThread = new Thread(this);
			this.readerThread.start();
			// Wait up to 3 seconds for FireStep to say Hi
			// If we get anything at this point it will have been the settings
			// dump that is sent after reset.
			responses = this.sendCommand(null, 3000);
		}

		this.connectedVersion = "";
		this.connected = true;
		this.processStatusResponses(responses);

		for (int i = 0; i < 5 && !this.connected; i++)
			this.sendJsonCommand("{'sys':''}", 100);

		if (!this.connected)
			throw new Error(String.format("Unable to receive connection response from FireStep. Check your port and baud rate, and that you are running at least version %f of Marlin",
					FireStepDriver.minimumRequiredVersion));

		// TODO: Commenting this out for now. Will implement version checks once
		// we get the
		// prototoype working.
		// if (connectedVersion < minimumRequiredVersion) {
		// throw new Error(String.format("This driver requires Marlin version
		// %.2f or higher. You
		// are running version %.2f", minimumRequiredVersion,
		// connectedVersion));
		// }

		// TODO: Allow configuration of modular tools
		this.setXyzMotorEnable(false); // Disable all motors
		this.setMotorDirection(true, true, false); // Set all motor directions
													 // to 'normal'
		this.setHomingSpeed(200); // Set the homing speed to something slower
									 // than default
		this.sendJsonCommand("{'ape':34}", 100); // Set the enable pin for axis
												 // 'a' to tool 4 (this is an
		// ugly hack and should go away)
		// Turn off the stepper drivers
		this.setEnabled(false);
	}

	@Override
	public synchronized void disconnect()
	{
		this.disconnectRequested = true;
		this.connected = false;

		try
		{
			if (this.readerThread != null && this.readerThread.isAlive())
				this.readerThread.join();
		} catch (Exception e)
		{
			FireStepDriver.logger.error("disconnect()", e);
		}

		try
		{
			super.disconnect();
		} catch (Exception e)
		{
			FireStepDriver.logger.error("disconnect()", e);
		}
		this.disconnectRequested = false;
	}

	private List<String> drainResponseQueue()
	{
		List<String> responses = new ArrayList<>();
		String response;
		while ((response = this.responseQueue.poll()) != null)
			responses.add(response);
		return responses;
	}

	private void enableEndEffectorRingLight(boolean enable) throws Exception
	{
		FireStepDriver.logger.debug(String.format("FireStep: End effector LED ring light: %s", enable ? "Turned ON" : "Turned OFF"));
		this.toggleDigitalPin(4, enable);
	}

	private void enablePowerSupply(boolean enable) throws Exception
	{
		FireStepDriver.logger.debug(String.format("FireStep: Power supply: %s", enable ? "Turned ON" : "Turned OFF"));
		this.toggleDigitalPin(28, enable);
		this.powerSupplyOn = enable;
	}

	private void enableUpLookingRingLight(boolean enable) throws Exception
	{
		FireStepDriver.logger.debug(String.format("FireStep: Up-looking LED ring light: %s", enable ? "Turned ON" : "Turned OFF"));
		this.toggleDigitalPin(5, enable);
	}

	private void enableVacuumPump(boolean enable) throws Exception
	{
		FireStepDriver.logger.debug(String.format("FireStep: Vacuum pump: %s", enable ? "Enabled" : "Disabled"));
		this.toggleDigitalPin(26, enable);
	}

	@Override
	public PropertySheetHolder[] getChildPropertySheetHolders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Wizard getConfigurationWizard()
	{
		// return null;
		return new FireStepDriverWizard(this);
	}

	@Override
	public Location getLocation(ReferenceHeadMountable hm)
	{
		// TODO: Request raw step positions from FireStep, do forward delta
		// kinematics, throw
		// exception if they don't match this class's Cartesian pos.
		return new Location(LengthUnit.Millimeters, this.x, this.y, this.z, this.c).add(hm.getHeadOffsets());
	}

	@Override
	public Action[] getPropertySheetHolderActions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPropertySheetHolderTitle()
	{
		return this.getClass().getSimpleName();
	}

	@Override
	public PropertySheet[] getPropertySheets()
	{
		return new PropertySheet[]
		{ new PropertySheetWizardAdapter(this.getConfigurationWizard()) };
	}

	@Override
	public void home(ReferenceHead head) throws Exception
	{
		RawStepTriplet rs = this.deltaCalc.getHomePosRaw();
		this.sendJsonCommand(String.format("{'hom':{'x':%d,'y':%d,'z':%d}}", rs.x, rs.y, rs.z), 10000);

		Location homLoc = this.deltaCalc.getHomePosCartesian();
		FireStepDriver.logger.debug(String.format("Home position: X=%.2f, Y=%.2f, Z=%.2f", homLoc.getX(), homLoc.getY(), homLoc.getZ()));
		this.x = homLoc.getX();
		this.y = homLoc.getY();
		this.z = homLoc.getZ();
		// TODO: Fire off head event to get the DRO to update to the new values
	}

	@Override
	public void moveTo(ReferenceHeadMountable hm, Location location, double speed) throws Exception
	{
		location = location.subtract(hm.getHeadOffsets());
		location = location.convertToUnits(LengthUnit.Millimeters);

		int rotSteps = 0;
		RawStepTriplet rs = new RawStepTriplet(0, 0, 0);
		boolean moveXyz = false;
		boolean moveRot = false;

		// Check if we've rotated
		if (Math.abs(location.getRotation() - this.c) >= 0.01)
		{
			moveRot = true;
			// Convert the rotation axis from degrees to steps
			rotSteps = (int) (location.getRotation() * this.nozzleStepsPerDegree + 0.5d);
			if (rotSteps >= 32000 || rotSteps <= -32000)
				throw new Error(String.format("FireStep: Rotation axis raw position cannot exceed +/- 32000 steps", rotSteps));
		}

		// Check if we've moved in XYZ
		Location currentLoc = new Location(LengthUnit.Millimeters, this.x, this.y, this.z, 0);
		if (Math.abs(location.getXyzDistanceTo(currentLoc)) >= 0.01)
		{
			moveXyz = true;
			FireStepDriver.logger.debug(String.format("moveTo Cartesian: X: %.3f, Y: %.3f, Z: %.3f", location.getX(), location.getY(), location.getZ()));

			// Calculate delta kinematics (returns angles)
			AngleTriplet angles = this.deltaCalc.calculateDelta(location);
			FireStepDriver.logger.debug(String.format("moveTo Delta: X: %.3f, Y: %.3f, Z: %.3f", angles.x, angles.y, angles.z));

			// Convert angles into raw steps
			rs = this.deltaCalc.getRawSteps(angles);
			FireStepDriver.logger.debug(String.format("moveTo RawSteps: X: %d, Y: %d, Z: %d", rs.x, rs.y, rs.z));
		}

		// Get feedrate in raw steps
		// Note that speed is defined by (maximum feed rate * speed) where speed
		// is greater than 0
		// and typically less than or equal to 1.
		// A speed of 0 means to move at the minimum possible speed.
		// TODO: Set feedrate based in raw steps, based off of
		// 'feedRateMmPerMinute' and 'speed'
		// 'mv' is maximum velocity (pulses/second), and the default is 12800.

		int rawFeedrate = 12800; // 12800 is FireStep's default feedrate
		rawFeedrate = (int) (rawFeedrate * speed); // Multiply rawFeedrate by
													 // speed, which
													 // should be 0 to 1
		if (moveXyz)
		{
			if (moveRot)
			{ // Cartesian move with rotation. Feedrate is (TBD)
				FireStepDriver.logger.debug(String.format("moveTo: Cartesian move with rotation, feedrate=%d steps/second", rawFeedrate));
				this.setRotMotorEnable(true);
				this.sendJsonCommand(String.format("{'mov':{'x':%d,'y':%d,'z':%d, 'a':%d,'mv':%d}}", rs.x, rs.y, rs.z, rotSteps, rawFeedrate), 10000);
			} else
			{ // Cartesian move with no rotation. Feedrate is just the cartesian
				 // feedrate
				FireStepDriver.logger.debug(String.format("moveTo: Cartesian move, feedrate=%d steps/second", rawFeedrate));
				this.sendJsonCommand(String.format("{'mov':{'x':%d,'y':%d,'z':%d,'mv':%d}}", rs.x, rs.y, rs.z, rawFeedrate), 10000);
			}
		} else if (moveRot)
		{ // Rotation, no Cartesian move. Feedrate is just the rotation feedrate
			this.setRotMotorEnable(true);
			FireStepDriver.logger.debug(String.format("moveTo: Rotation move, feedrate=%d steps/second", rawFeedrate));
			this.sendJsonCommand(String.format("{'mov':{'a':%d,'mv':%d}}", rotSteps, rawFeedrate), 10000);
		} else
			FireStepDriver.logger.debug("moveTo: No move, nothing to do");

		if (!Double.isNaN(location.getX()))
			this.x = location.getX();
		if (!Double.isNaN(location.getY()))
			this.y = location.getY();
		if (!Double.isNaN(location.getZ()))
			this.z = location.getZ();
		if (!Double.isNaN(location.getRotation()))
			this.c = location.getRotation();
	}

	@Override
	public void pick(ReferenceNozzle nozzle) throws Exception
	{
		this.setRotMotorEnable(true); // Enable the nozzle rotation
		this.enableVacuumPump(true); // Enable the pump
	}

	@Override
	public void place(ReferenceNozzle nozzle) throws Exception
	{
		this.enableVacuumPump(false);
		this.setRotMotorEnable(false);
	}

	private void processStatusResponses(List<String> responses)
	{
		for (String response : responses)
			if (response.startsWith("FireStep"))
			{
				FireStepDriver.logger.debug("echo: " + response);
				String[] versionComponents = response.split(" ");
				this.connectedVersion = versionComponents[1];
				this.connected = true;
				FireStepDriver.logger.debug(String.format("Connected to FireStep Version: %s", this.connectedVersion));
			} else
			{
				// TODO: Debug returned stuff here
			}
	}

	// Serial receive thread
	@Override
	public void run()
	{
		while (!this.disconnectRequested)
		{
			String line;
			try
			{
				line = this.readLine().trim();
			} catch (TimeoutException ex)
			{
				continue;
			} catch (IOException e)
			{
				FireStepDriver.logger.error("Read error", e);
				return;
			}
			line = line.trim();
			FireStepDriver.logger.debug(line);
			this.responseQueue.offer(line);
			// if (line.equals("ok") || line.startsWith("error: ")) {
			if (line.isEmpty() == false)
				// This is the end of processing for a command
				synchronized (this.commandLock)
				{
					this.commandLock.notify();
				}
		}
	}

	private List<String> sendCommand(String command, long timeout) throws Exception
	{
		synchronized (this.commandLock)
		{
			if (command != null)
			{
				FireStepDriver.logger.debug("sendCommand({}, {})", command, timeout);
				this.output.write(command.getBytes());
				this.output.write("\n".getBytes());
			}
			if (timeout == -1)
				this.commandLock.wait();
			else
				this.commandLock.wait(timeout);
		}
		List<String> responses = this.drainResponseQueue();
		return responses;
	}

	private void sendFireStepConfig(boolean xyz, boolean rot, String param, String value) throws Exception
	{
		if (xyz && rot)
			this.sendJsonCommand(String.format("{'x%s':%s,'y%s':%s,'z%s':%s,'a%s':%s}", param, value, param, value, param, value, param, value), 100);
		else if (xyz)
			this.sendJsonCommand(String.format("{'x%s':%s,'y%s':%s,'z%s':%s}", param, value, param, value, param, value), 100);
		else if (rot)
			this.sendJsonCommand(String.format("{'a%s':%s}", param, value), 100);
	}

	private void sendJsonCommand(String command, long timeout) throws Exception
	{
		List<String> responses = this.sendCommand(command.replaceAll("'", "\""), timeout);
		this.processStatusResponses(responses);
	}

	@Override
	public void setEnabled(boolean enabled) throws Exception
	{
		if (enabled)
		{
			if (!this.connected)
				try
				{
					this.connect();
				} catch (Exception e)
				{
					e.printStackTrace();
					throw e;
				}
			this.enableVacuumPump(false); // Turn the vacuum pump OFF
			this.enablePowerSupply(true); // Turn power supply ON
			if (this.powerSupplyOn)  // Exception should catch but guard just in
									 // case
			{
				Thread.sleep(500, 0); // Delay for a bit, wait for power supply
										 // to stabilize.
				this.setXyzMotorEnable(true); // Enable power for XYZ stepper
												 // motors
				this.enableEndEffectorRingLight(true); // Turn off down-looking
														 // LED ring light
				Thread.sleep(50, 0); // Delay for a bit, wait for power supply
									 // to stabilize.
				this.home(null); // home the machine
			}

		}  // if (enabled)
		else if (this.connected)
		{
			this.enableEndEffectorRingLight(false); // Turn off down-looking LED
													 // ring light
			this.enableUpLookingRingLight(false); // Turn off up-looking LED
													 // ring light
			if (this.powerSupplyOn)
			{
				this.home(null); // home the machine
				this.enableVacuumPump(false); // Turn the vacuum pump OFF
				this.setXyzMotorEnable(false); // Disable power for XYZ stepper
												 // motors
				this.enablePowerSupply(false); // Turn off the power supply
			}
		}
	}

	private void setHomingSpeed(int delay) throws Exception
	{
		this.sendJsonCommand(String.format("{'xsd':%d,'ysd':%d,'zsd':%d}", delay, delay, delay), 100); // Search
		// delay
		// (think
		// this
		// is
		// the
		// homing
		// speed)
	}

	private void setMotorDirection(boolean xyz, boolean rot, boolean enable) throws Exception
	{
		FireStepDriver.logger.debug(String.format("%s%s Stepper motor Direction set to %s", xyz ? "XYZ" : "", rot ? "A" : "", enable ? "enabled" : "disabled"));
		this.sendFireStepConfig(xyz, rot, "dh", enable ? "true" : "false");
	}

	private void setRotMotorEnable(boolean enable) throws Exception
	{
		FireStepDriver.logger.debug(String.format("Rotation Stepper motor Enable set to %s", enable ? "enabled" : "disabled"));
		if (enable)
		{
			if (this.nozzleEnabled)
			{
				// Already enabled, nothing to do
			} else
			{
				this.sendFireStepConfig(false, true, "en", "true"); // Enable
																	 // power
																	 // for XYZ
																	 // stepper
				// motors
				Thread.sleep(200, 0); // Delay for a bit, wait for stepper motor
										 // coils to stabilize.
			}
		} else if (this.nozzleEnabled)
			this.sendFireStepConfig(false, true, "en", "false"); // Enable power
																 // for XYZ
																 // stepper
		// motors
		else
		{
			// Already disabled, nothing to do
		}
		this.nozzleEnabled = enable; // Set state variable
	}

	private void setXyzMotorEnable(boolean enable) throws Exception
	{
		FireStepDriver.logger.debug(String.format("XYZ Stepper motor Enable set to %s", enable ? "enabled" : "disabled"));
		this.sendFireStepConfig(true, false, "en", enable ? "true" : "false");
	}

	private void toggleDigitalPin(int pin, boolean state) throws Exception
	{
		FireStepDriver.logger.debug(String.format("FireStep: Toggling digital pin %d to %s", pin, state ? "HIGH" : "LOW"));
		try
		{
			this.sendJsonCommand(String.format("{'iod%d':%s}", pin, state ? "true" : "false"), 100);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
