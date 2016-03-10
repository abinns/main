package org.openpnp.machine.openbuilds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.Action;

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

public class OpenBuildsDriver extends AbstractSerialPortDriver implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(OpenBuildsDriver.class);

	@Attribute(required = false)
	protected double feedRateMmPerMinute = 5000;

	@Attribute(required = false)
	private double zCamRadius = 24;

	@Attribute(required = false)
	private double zCamWheelRadius = 9.5;

	@Attribute(required = false)
	private double zGap = 2;

	@Attribute(required = false)
	private boolean homeZ = false;

	protected double					x, y, zA, c, c2;
	private Thread						readerThread;
	private boolean						disconnectRequested;
	private Object						commandLock		= new Object();
	private boolean						connected;
	private LinkedBlockingQueue<String>	responseQueue	= new LinkedBlockingQueue<>();
	private boolean						n1Picked, n2Picked;

	@Override
	public void actuate(ReferenceActuator actuator, boolean on) throws Exception
	{
		// if (actuator.getIndex() == 0) {
		// sendCommand(on ? actuatorOnGcode : actuatorOffGcode);
		// dwell();
		// }
	}

	@Override
	public void actuate(ReferenceActuator actuator, double value) throws Exception
	{
	}

	@Override
	public synchronized void connect() throws Exception
	{
		super.connect();

		/**
		 * Connection process notes: On some platforms, as soon as we open the
		 * serial port it will reset the controller and we'll start getting some
		 * data. On others, it may already be running and we will get nothing on
		 * connect.
		 */

		this.connected = false;
		List<String> responses;
		this.readerThread = new Thread(this);
		this.readerThread.start();

		try
		{
			do
				// Consume any buffered incoming data, including startup
				// messages
				responses = this.sendCommand(null, 200);
			while (!responses.isEmpty());
		} catch (Exception e)
		{
			// ignore timeouts
		}

		// Send a request to force Smoothie to respond and clear any buffers.
		// On my machine, at least, this causes Smoothie to re-send it's
		// startup message and I can't figure out why, but this works
		// around it.
		responses = this.sendCommand("M114", 5000);
		// Continue to read responses until we get the one that is the
		// result of the M114 command. When we see that we're connected.
		long t = System.currentTimeMillis();
		while (System.currentTimeMillis() - t < 5000)
		{
			for (String response : responses)
				if (response.contains("X:"))
				{
					this.connected = true;
					break;
				}
			if (this.connected)
				break;
			responses = this.sendCommand(null, 200);
		}

		if (!this.connected)
			throw new Exception(String.format("Unable to receive connection response. Check your port and baud rate"));

		// We are connected to at least the minimum required version now
		// So perform some setup

		// Turn off the stepper drivers
		this.setEnabled(false);

		// Set mm coordinate mode
		this.sendCommand("G21");
		// Set absolute positioning mode
		this.sendCommand("G90");
		// Set absolute mode for extruder
		this.sendCommand("M82");
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
			OpenBuildsDriver.logger.error("disconnect()", e);
		}

		try
		{
			super.disconnect();
		} catch (Exception e)
		{
			OpenBuildsDriver.logger.error("disconnect()", e);
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
	 * Block until all movement is complete.
	 * 
	 * @throws Exception
	 */
	protected void dwell() throws Exception
	{
		this.sendCommand("M400");
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
		return new OpenBuildsDriverWizard(this);
	}

	protected void getCurrentPosition() throws Exception
	{
		List<String> responses;
		this.sendCommand("T0");
		responses = this.sendCommand("M114");
		for (String response : responses)
			if (response.contains("X:"))
			{
				String[] comps = response.split(" ");
				for (String comp : comps)
					if (comp.startsWith("X:"))
						this.x = Double.parseDouble(comp.split(":")[1]);
					else if (comp.startsWith("Y:"))
						this.y = Double.parseDouble(comp.split(":")[1]);
					else if (comp.startsWith("Z:"))
						this.zA = Double.parseDouble(comp.split(":")[1]);
					else if (comp.startsWith("E:"))
						this.c = Double.parseDouble(comp.split(":")[1]);
			}
		this.sendCommand("T1");
		responses = this.sendCommand("M114");
		for (String response : responses)
			if (response.contains("X:"))
			{
				String[] comps = response.split(" ");
				for (String comp : comps)
					if (comp.startsWith("E:"))
						this.c2 = Double.parseDouble(comp.split(":")[1]);
			}
		this.sendCommand("T0");
		OpenBuildsDriver.logger.debug("Current Position is {}, {}, {}, {}, {}", new Object[]
		{ this.x, this.y, this.zA, this.c, this.c2 });
	}

	@Override
	public Location getLocation(ReferenceHeadMountable hm)
	{
		if (hm instanceof ReferenceNozzle)
		{
			ReferenceNozzle nozzle = (ReferenceNozzle) hm;
			double z = Math.sin(Math.toRadians(this.zA)) * this.zCamRadius;
			if (((ReferenceNozzle) hm).getName().equals("N2"))
				z = -z;
			z += this.zCamWheelRadius + this.zGap;
			int tool = nozzle == null || nozzle.getName().equals("N1") ? 0 : 1;
			return new Location(LengthUnit.Millimeters, this.x, this.y, z, tool == 0 ? this.c : this.c2).add(hm.getHeadOffsets());
		} else
			return new Location(LengthUnit.Millimeters, this.x, this.y, this.zA, this.c).add(hm.getHeadOffsets());
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
		if (this.homeZ)
		{
			// Home Z
			this.sendCommand("G28 Z0", 10 * 1000);
			// Move Z to 0
			this.sendCommand("G0 Z0");
		} else
		{
			// We "home" Z by turning off the steppers, allowing the
			// spring to pull the nozzle back up to home.
			this.sendCommand("M84");
			// And call that zero
			this.sendCommand("G92 Z0");
			// And wait a tick just to let things settle down
			Thread.sleep(250);
		}
		// Home X and Y
		this.sendCommand("G28 X0 Y0", 60 * 1000);
		// Zero out the two "extruders"
		this.sendCommand("T1");
		this.sendCommand("G92 E0");
		this.sendCommand("T0");
		this.sendCommand("G92 E0");
		// Update position
		this.getCurrentPosition();
	}

	private void led(boolean on) throws Exception
	{
		this.sendCommand(on ? "M810" : "M811");
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

		ReferenceNozzle nozzle = null;
		if (hm instanceof ReferenceNozzle)
			nozzle = (ReferenceNozzle) hm;

		/*
		 * Only move Z if it's a Nozzle.
		 */
		if (nozzle == null)
			z = Double.NaN;

		StringBuffer sb = new StringBuffer();
		if (!Double.isNaN(x) && x != this.x)
		{
			sb.append(String.format(Locale.US, "X%2.2f ", x));
			this.x = x;
		}
		if (!Double.isNaN(y) && y != this.y)
		{
			sb.append(String.format(Locale.US, "Y%2.2f ", y));
			this.y = y;
		}
		int tool = nozzle == null || nozzle.getName().equals("N1") ? 0 : 1;
		if (!Double.isNaN(c) && c != (tool == 0 ? this.c : this.c2))
		{
			// If there is an E move we need to set the tool before
			// performing any commands otherwise we may move the wrong tool.
			this.sendCommand(String.format(Locale.US, "T%d", tool));
			if (sb.length() == 0)
			{
				// If the move won't contain an X or Y component but will
				// have an E component we need to send the E component as a
				// solo move because Smoothie won't move only E and Z at
				// the same time.
				this.sendCommand(String.format(Locale.US, "G0 E%2.2f F%2.2f", c, this.feedRateMmPerMinute));
				this.dwell();
			} else
				sb.append(String.format(Locale.US, "E%2.2f ", c));
			if (tool == 0)
				this.c = c;
			else
				this.c2 = c;
		}

		if (!Double.isNaN(z))
		{
			double a = Math.toDegrees(Math.asin((z - this.zCamWheelRadius - this.zGap) / this.zCamRadius));
			OpenBuildsDriver.logger.debug("nozzle {} {} {}", new Object[]
			{ z, this.zCamRadius, a });
			if (nozzle.getName().equals("N2"))
				a = -a;
			if (a != this.zA)
			{
				sb.append(String.format(Locale.US, "Z%2.2f ", a));
				this.zA = a;
			}
		}

		if (sb.length() > 0)
		{
			sb.append(String.format(Locale.US, "F%2.2f", this.feedRateMmPerMinute));
			this.sendCommand("G0 " + sb.toString());
			this.dwell();
		}
	}

	private void n1Exhaust(boolean on) throws Exception
	{
		this.sendCommand(on ? "M802" : "M803");
	}

	private void n1Vacuum(boolean on) throws Exception
	{
		this.sendCommand(on ? "M800" : "M801");
	}

	private void n2Exhaust(boolean on) throws Exception
	{
		this.sendCommand(on ? "M806" : "M807");
	}

	private void n2Vacuum(boolean on) throws Exception
	{
		this.sendCommand(on ? "M804" : "M805");
	}

	@Override
	public void pick(ReferenceNozzle nozzle) throws Exception
	{
		if (nozzle.getName().equals("N1"))
		{
			this.pump(true);
			this.n1Exhaust(false);
			this.n1Vacuum(true);
			this.n1Picked = true;
		} else
		{
			this.pump(true);
			this.n2Exhaust(false);
			this.n2Vacuum(true);
			this.n2Picked = true;
		}
	}

	@Override
	public void place(ReferenceNozzle nozzle) throws Exception
	{
		if (nozzle.getName().equals("N1"))
		{
			this.n1Picked = false;
			if (!this.n1Picked && !this.n2Picked)
				this.pump(false);
			this.n1Vacuum(false);
			this.n1Exhaust(true);
			Thread.sleep(500);
			this.n1Exhaust(false);
		} else
		{
			this.n2Picked = false;
			if (!this.n1Picked && !this.n2Picked)
				this.pump(false);
			this.n2Vacuum(false);
			this.n2Exhaust(true);
			Thread.sleep(500);
			this.n2Exhaust(false);
		}
	}

	private void pump(boolean on) throws Exception
	{
		this.sendCommand(on ? "M808" : "M809");
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
				OpenBuildsDriver.logger.error("Read error", e);
				return;
			}
			line = line.trim();
			OpenBuildsDriver.logger.debug("<< " + line);
			this.responseQueue.offer(line);
			if (line.startsWith("ok") || line.startsWith("error: "))
				// This is the end of processing for a command
				synchronized (this.commandLock)
				{
					this.commandLock.notify();
				}
		}
	}

	protected List<String> sendCommand(String command) throws Exception
	{
		return this.sendCommand(command, 5000);
	}

	protected List<String> sendCommand(String command, long timeout) throws Exception
	{
		List<String> responses = new ArrayList<>();

		// Read any responses that might be queued up so that when we wait
		// for a response to a command we actually wait for the one we expect.
		this.responseQueue.drainTo(responses);

		// Send the command, if one was specified
		if (command != null)
		{
			OpenBuildsDriver.logger.debug("sendCommand({}, {})", command, timeout);
			OpenBuildsDriver.logger.debug(">> " + command);
			this.output.write(command.getBytes());
			this.output.write("\n".getBytes());
		}

		String response = null;
		if (timeout == -1)
			// Wait forever for a response to return from the reader.
			response = this.responseQueue.take();
		else
		{
			// Wait up to timeout milliseconds for a response to return from
			// the reader.
			response = this.responseQueue.poll(timeout, TimeUnit.MILLISECONDS);
			if (response == null)
				throw new Exception("Timeout waiting for response to " + command);
		}
		// And if we got one, add it to the list of responses we'll return.
		responses.add(response);

		// Read any additional responses that came in after the initial one.
		this.responseQueue.drainTo(responses);

		OpenBuildsDriver.logger.debug("{} => {}", command, responses);
		return responses;
	}

	@Override
	public void setEnabled(boolean enabled) throws Exception
	{
		if (enabled && !this.connected)
			this.connect();
		if (this.connected)
			if (enabled)
			{
				this.n1Vacuum(false);
				this.n1Exhaust(false);
				this.n2Vacuum(false);
				this.n2Exhaust(false);
				this.led(true);
			} else
			{
				this.sendCommand("M84");
				this.n1Vacuum(false);
				this.n1Exhaust(false);
				this.n2Vacuum(false);
				this.n2Exhaust(false);
				this.led(false);
				this.pump(false);

			}
	}
}
