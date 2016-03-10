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

import java.util.ArrayList;

import javax.swing.Action;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.model.Configuration;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.base.AbstractHead;
import org.openpnp.spi.base.SimplePropertySheetHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceHead extends AbstractHead
{
	protected final static Logger logger = LoggerFactory.getLogger(ReferenceHead.class);

	protected ReferenceMachine	machine;
	protected ReferenceDriver	driver;

	public ReferenceHead()
	{
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationLoaded(Configuration configuration) throws Exception
			{
				ReferenceHead.this.machine = (ReferenceMachine) configuration.getMachine();
				ReferenceHead.this.driver = ReferenceHead.this.machine.getDriver();
			}
		});
	}

	@Override
	public PropertySheetHolder[] getChildPropertySheetHolders()
	{
		ArrayList<PropertySheetHolder> children = new ArrayList<>();
		children.add(new SimplePropertySheetHolder("Nozzles", this.getNozzles()));
		children.add(new SimplePropertySheetHolder("Cameras", this.getCameras()));
		children.add(new SimplePropertySheetHolder("Actuators", this.getActuators()));
		children.add(new SimplePropertySheetHolder("Paste Dispensers", this.getPasteDispensers()));
		return children.toArray(new PropertySheetHolder[]
		{});
	}

	@Override
	public Wizard getConfigurationWizard()
	{
		// This Wizard is out of date and none of it currently works.
		// return new ReferenceHeadConfigurationWizard(this);
		return null;
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

	@Override
	public void home() throws Exception
	{
		ReferenceHead.logger.debug("{}.home()", this.getName());
		this.driver.home(this);
		this.machine.fireMachineHeadActivity(this);
	}

	@Override
	public void moveToSafeZ(double speed) throws Exception
	{
		ReferenceHead.logger.debug("{}.moveToSafeZ({})", this.getName(), speed);
		super.moveToSafeZ(speed);
	}

	@Override
	public String toString()
	{
		return this.getName();
	}
}
