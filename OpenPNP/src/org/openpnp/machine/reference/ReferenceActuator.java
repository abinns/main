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

package org.openpnp.machine.reference;

import javax.swing.Action;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.wizards.ReferenceActuatorConfigurationWizard;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.base.AbstractActuator;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceActuator extends AbstractActuator implements ReferenceHeadMountable
{
	protected final static Logger logger = LoggerFactory.getLogger(ReferenceActuator.class);

	@Element
	private Location headOffsets;

	@Attribute
	private int index;

	@Element(required = false)
	protected Length safeZ = new Length(0, LengthUnit.Millimeters);

	protected ReferenceMachine	machine;
	protected ReferenceDriver	driver;

	public ReferenceActuator()
	{
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationLoaded(Configuration configuration) throws Exception
			{
				ReferenceActuator.this.machine = (ReferenceMachine) configuration.getMachine();
				ReferenceActuator.this.driver = ReferenceActuator.this.machine.getDriver();
			}
		});
	}

	@Override
	public void actuate(boolean on) throws Exception
	{
		ReferenceActuator.logger.debug("{}.actuate({})", new Object[]
		{ this.getName(), on });
		this.driver.actuate(this, on);
		this.machine.fireMachineHeadActivity(this.head);
	}

	@Override
	public void actuate(double value) throws Exception
	{
		ReferenceActuator.logger.debug("{}.actuate({})", new Object[]
		{ this.getName(), value });
		this.driver.actuate(this, value);
		this.machine.fireMachineHeadActivity(this.head);
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
		return new ReferenceActuatorConfigurationWizard(this);
	}

	@Override
	public Location getHeadOffsets()
	{
		return this.headOffsets;
	}

	public int getIndex()
	{
		return this.index;
	}

	@Override
	public Location getLocation()
	{
		return this.driver.getLocation(this);
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
		return this.getClass().getSimpleName() + " " + this.getName();
	}

	@Override
	public PropertySheet[] getPropertySheets()
	{
		return new PropertySheet[]
		{ new PropertySheetWizardAdapter(this.getConfigurationWizard()) };
	}

	public Length getSafeZ()
	{
		return this.safeZ;
	}

	@Override
	public void moveTo(Location location, double speed) throws Exception
	{
		ReferenceActuator.logger.debug("{}.moveTo({}, {})", new Object[]
		{ this.getName(), location, speed });
		this.driver.moveTo(this, location, speed);
		this.machine.fireMachineHeadActivity(this.head);
	}

	@Override
	public void moveToSafeZ(double speed) throws Exception
	{
		ReferenceActuator.logger.debug("{}.moveToSafeZ({})", new Object[]
		{ this.getName(), speed });
		Length safeZ = this.safeZ.convertToUnits(this.getLocation().getUnits());
		Location l = new Location(this.getLocation().getUnits(), Double.NaN, Double.NaN, safeZ.getValue(), Double.NaN);
		this.driver.moveTo(this, l, speed);
		this.machine.fireMachineHeadActivity(this.head);
	}

	@Override
	public void setHeadOffsets(Location headOffsets)
	{
		this.headOffsets = headOffsets;
	}

	public void setSafeZ(Length safeZ)
	{
		this.safeZ = safeZ;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}
}
