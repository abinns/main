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

package org.openpnp.machine.reference.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;

import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.ReferenceActuator;
import org.openpnp.machine.reference.ReferenceHead;
import org.openpnp.machine.reference.ReferenceHeadMountable;
import org.openpnp.machine.reference.ReferenceNozzle;
import org.openpnp.machine.reference.driver.wizards.AbstractSerialPortDriverConfigurationWizard;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.PropertySheetHolder;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarlinDriver extends AbstractSerialPortDriver implements Runnable
{
	private static final Logger	logger						= LoggerFactory.getLogger(MarlinDriver.class);
	private static final long	minimumRequiredBuildNumber	= 20140822;

	@Attribute(required = false)
	protected double feedRateMmPerMinute = 5000;

	@Element(required = false)
	protected String pickGcode = "M106 S255";

	@Element(required = false)
	protected String placeGcode = "M107";

	@Element(required = false)
	protected String actuatorOnGcode = "M8";

	@Element(required = false)
	protected String actuatorOffGcode = "M9";

	@Element(required = false)
	protected String enableGcode = "M17";

	@Element(required = false)
	protected String disableGcode = "M18";

	protected double		x, y, z, c;
	private Thread			readerThread;
	private boolean			disconnectRequested;
	private Object			commandLock		= new Object();
	private boolean			connected;
	private long			connectedBuildNumber;
	private Queue<String>	responseQueue	= new ConcurrentLinkedQueue<>();

	public MarlinDriver()
	{
	}

	@Override
	public void actuate(ReferenceActuator actuator, boolean on) throws Exception
	{
		if (actuator.getIndex() == 0)
		{
			this.sendCommand(on ? this.actuatorOnGcode : this.actuatorOffGcode);
			this.dwell();
		}
	}

	@Override
	public void actuate(ReferenceActuator actuator, double value) throws Exception
	{
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
			// Wait up to 3 seconds for Grbl to say Hi
			// If we get anything at this point it will have been the settings
			// dump that is sent after reset.
			responses = this.sendCommand(null, 3000);
		}

		this.processConnectionResponses(responses);

		for (int i = 0; i < 5 && !this.connected; i++)
		{
			responses = this.sendCommand("M115", 5000);
			this.processConnectionResponses(responses);
		}

		if (!this.connected)
			throw new Exception(String.format("Unable to receive connection response from Grbl. Check your port and baud rate, and that you are running at least build %d of Grbl",
					MarlinDriver.minimumRequiredBuildNumber));

		// if (connectedBuildNumber < minimumRequiredBuildNumber) {
		// throw new Error(String.format("This driver requires Grbl build %d or
		// higher. You are
		// running build %d", minimumRequiredBuildNumber,
		// connectedBuildNumber));
		// }

		// We are connected to at least the minimum required version now
		// So perform some setup

		// Turn off the stepper drivers
		this.setEnabled(false);

		this.sendCommand("G21");
		this.sendCommand("G90");
		this.sendCommand("M82");
		this.sendCommand("M84 S0");
		this.getCurrentPosition();
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
			MarlinDriver.logger.error("disconnect()", e);
		}

		try
		{
			super.disconnect();
		} catch (Exception e)
		{
			MarlinDriver.logger.error("disconnect()", e);
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

	/**
	 * Causes Grbl to block until all commands are complete.
	 * 
	 * @throws Exception
	 */
	protected void dwell() throws Exception
	{
		this.sendCommand("G4 P0");
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
		return new AbstractSerialPortDriverConfigurationWizard(this);
	}

	protected void getCurrentPosition() throws Exception
	{
		List<String> responses = this.sendCommand("M114");
		for (String response : responses)
			if (response.startsWith("X:"))
			{
				String[] comps = response.split(" ");
				this.x = Double.parseDouble(comps[0].split(":")[1]);
				this.y = Double.parseDouble(comps[1].split(":")[1]);
				this.z = Double.parseDouble(comps[2].split(":")[1]);
				this.c = Double.parseDouble(comps[3].split(":")[1]);
			}
	}

	@Override
	public Location getLocation(ReferenceHeadMountable hm)
	{
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
		this.sendCommand("G28");
		this.getCurrentPosition();
	}

	@Override
	public void moveTo(ReferenceHeadMountable hm, Location location, double speed) throws Exception
	{
		location = location.subtract(hm.getHeadOffsets());

		location = location.convertToUnits(LengthUnit.Millimeters);

		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		double c = location.getRotation();

		StringBuffer sb = new StringBuffer();
		if (!Double.isNaN(x) && x != this.x)
			sb.append(String.format(Locale.US, "X%2.2f ", x));
		if (!Double.isNaN(y) && y != this.y)
			sb.append(String.format(Locale.US, "Y%2.2f ", y));
		if (!Double.isNaN(z) && z != this.z)
			sb.append(String.format(Locale.US, "Z%2.2f ", z));
		if (!Double.isNaN(c) && c != this.c)
			sb.append(String.format(Locale.US, "E%2.2f ", c));
		if (sb.length() > 0)
		{
			sb.append(String.format(Locale.US, "F%2.2f", this.feedRateMmPerMinute));
			this.sendCommand("G1 " + sb.toString());
			this.dwell();
		}
		if (!Double.isNaN(x))
			this.x = x;
		if (!Double.isNaN(y))
			this.y = y;
		if (!Double.isNaN(z))
			this.z = z;
		if (!Double.isNaN(c))
			this.c = c;
	}

	@Override
	public void pick(ReferenceNozzle nozzle) throws Exception
	{
		this.sendCommand(this.pickGcode);
		this.dwell();
	}

	@Override
	public void place(ReferenceNozzle nozzle) throws Exception
	{
		this.sendCommand(this.placeGcode);
		this.dwell();
	}

	private void processConnectionResponses(List<String> responses)
	{
		for (String response : responses)
		{
			Matcher matcher = Pattern.compile(".*Marlin.*").matcher(response);
			if (matcher.matches())
			{
				// String majorVersion = matcher.group(1);
				// String minorVersion = matcher.group(2);
				// String buildNumber = matcher.group(3);
				// connectedBuildNumber = Long.parseLong(buildNumber);
				this.connected = true;
				// logger.debug(String.format("Connected to Grbl Version %s.%s,
				// build: %d",
				// majorVersion, minorVersion, connectedBuildNumber));
				MarlinDriver.logger.debug(String.format("Connected to Marlin"));
			}
		}
	}

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
				MarlinDriver.logger.error("Read error", e);
				return;
			}
			line = line.trim();
			MarlinDriver.logger.debug("<< " + line);
			this.responseQueue.offer(line);
			if (line.equals("ok") || line.startsWith("error: "))
				// This is the end of processing for a command
				synchronized (this.commandLock)
				{
					this.commandLock.notify();
				}
		}
	}

	protected List<String> sendCommand(String command) throws Exception
	{
		return this.sendCommand(command, -1);
	}

	protected List<String> sendCommand(String command, long timeout) throws Exception
	{
		synchronized (this.commandLock)
		{
			if (command != null)
			{
				MarlinDriver.logger.debug("sendCommand({}, {})", command, timeout);
				MarlinDriver.logger.debug(">> " + command);
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

	@Override
	public void setEnabled(boolean enabled) throws Exception
	{
		if (enabled && !this.connected)
			this.connect();
		if (this.connected)
			this.sendCommand(enabled ? this.enableGcode : this.disableGcode);
	}
}
