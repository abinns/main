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

import org.openpnp.model.Board.Side;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Version;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Persist;

/**
 * A Placement describes a location on a Board where a Part will be placed,
 * along with information about how to place it.
 * 
 * @author jason
 */
public class Placement extends AbstractModelObject implements Identifiable
{
	public enum Type
	{
		Place, Fiducial, Ignore
	}

	/**
	 * History: 1.0: Initial revision. 1.1: Replaced Boolean place with Type
	 * type. Deprecated place.
	 */
	@Version(revision = 1.1)
	private double version;

	@Attribute
	private String		id;
	@Element
	private Location	location;
	@Attribute
	private Side		side	= Side.Top;

	@Attribute(required = false)
	private String partId;

	@Deprecated
	@Attribute(required = false)
	private Boolean place;

	@Attribute
	private Type type;

	private Part part;

	@SuppressWarnings("unused")
	private Placement()
	{
		this(null);
	}

	public Placement(String id)
	{
		this.id = id;
		this.type = Type.Place;
		this.setLocation(new Location(LengthUnit.Millimeters));
	}

	@SuppressWarnings("unused")
	@Commit
	private void commit()
	{
		this.setLocation(this.location);
		if (this.getPart() == null)
			this.setPart(Configuration.get().getPart(this.partId));

		if (this.version == 1.0)
		{
			if (this.place != null && !this.place)
				this.type = Type.Ignore;
			this.place = null;
		}
	}

	@Override
	public String getId()
	{
		return this.id;
	}

	public Location getLocation()
	{
		return this.location;
	}

	public Part getPart()
	{
		return this.part;
	}

	public Side getSide()
	{
		return this.side;
	}

	public Type getType()
	{
		return this.type;
	}

	@SuppressWarnings("unused")
	@Persist
	private void persist()
	{
		this.partId = this.part == null ? null : this.part.getId();
	}

	public void setLocation(Location location)
	{
		Location oldValue = this.location;
		this.location = location;
		this.firePropertyChange("location", oldValue, location);
	}

	public void setPart(Part part)
	{
		Part oldValue = this.part;
		this.part = part;
		this.firePropertyChange("part", oldValue, part);
	}

	public void setSide(Side side)
	{
		Object oldValue = this.side;
		this.side = side;
		this.firePropertyChange("side", oldValue, side);
	}

	public void setType(Type type)
	{
		Object oldValue = this.type;
		this.type = type;
		this.firePropertyChange("type", oldValue, type);
	}

	@Override
	public String toString()
	{
		return String.format("id %s, location %s, side %s, part %s, type %s", this.id, this.location, this.side, this.part, this.type);
	}
}
