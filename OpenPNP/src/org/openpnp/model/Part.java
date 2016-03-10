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

package org.openpnp.model;

import org.openpnp.ConfigurationListener;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.core.Persist;

/**
 * A Part is a single part that can be picked and placed. It has a graphical
 * outline, is retrieved from one or more Feeders and is placed at a Placement
 * as part of a Job. Parts can be used across many boards and should generally
 * represent a single part in the real world.
 */
public class Part extends AbstractModelObject implements Identifiable
{
	@Attribute
	private String	id;
	@Attribute(required = false)
	private String	name;

	@Attribute
	private LengthUnit	heightUnits	= LengthUnit.Millimeters;
	@Attribute
	private double		height;

	private Package packag;

	@Attribute
	private String packageId;

	@Attribute(required = false)
	private double speed = 1.0;

	@SuppressWarnings("unused")
	private Part()
	{
		this(null);
	}

	public Part(String id)
	{
		this.id = id;
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationLoaded(Configuration configuration) throws Exception
			{
				if (Part.this.getPackage() == null)
					Part.this.setPackage(configuration.getPackage(Part.this.packageId));
			}
		});
	}

	public Length getHeight()
	{
		return new Length(this.height, this.heightUnits);
	}

	@Override
	public String getId()
	{
		return this.id;
	}

	public String getName()
	{
		return this.name;
	}

	public Package getPackage()
	{
		return this.packag;
	}

	public double getSpeed()
	{
		return this.speed;
	}

	@Persist
	private void persist()
	{
		this.packageId = this.packag == null ? null : this.packag.getId();
	}

	public void setHeight(Length height)
	{
		Object oldValue = this.getHeight();
		if (height == null)
		{
			this.height = 0;
			this.heightUnits = null;
		} else
		{
			this.height = height.getValue();
			this.heightUnits = height.getUnits();
		}
		this.firePropertyChange("height", oldValue, this.getHeight());
	}

	public void setName(String name)
	{
		Object oldValue = this.name;
		this.name = name;
		this.firePropertyChange("name", oldValue, name);
	}

	public void setPackage(Package packag)
	{
		Object oldValue = this.packag;
		this.packag = packag;
		this.firePropertyChange("package", oldValue, packag);
	}

	public void setSpeed(double speed)
	{
		Object oldValue = this.speed;
		this.speed = speed;
		this.firePropertyChange("speed", oldValue, speed);
	}

	@Override
	public String toString()
	{
		return String.format("id %s, name %s, heightUnits %s, height %f, packageId (%s)", this.id, this.name, this.heightUnits, this.height, this.packageId);
	}
}
