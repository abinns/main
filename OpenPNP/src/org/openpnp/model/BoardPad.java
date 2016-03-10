package org.openpnp.model;

import org.openpnp.model.Board.Side;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class BoardPad extends AbstractModelObject
{
	public enum Type
	{
		Paste, Ignore
	}

	@Attribute(required = false)
	private Type type = Type.Paste;

	@Attribute
	protected Side side = Side.Top;

	@Element
	protected Location location = new Location(LengthUnit.Millimeters);

	@Attribute(required = false)
	protected String name;

	@Element
	protected Pad pad;

	public BoardPad()
	{

	}

	public BoardPad(Pad pad, Location location)
	{
		this.setPad(pad);
		this.setLocation(location);
	}

	public Location getLocation()
	{
		return this.location;
	}

	public String getName()
	{
		return this.name;
	}

	public Pad getPad()
	{
		return this.pad;
	}

	public Side getSide()
	{
		return this.side;
	}

	public Type getType()
	{
		return this.type;
	}

	public void setLocation(Location location)
	{
		Location oldValue = this.location;
		this.location = location;
		this.firePropertyChange("location", oldValue, location);
	}

	public void setName(String name)
	{
		Object oldValue = this.name;
		this.name = name;
		this.firePropertyChange("name", oldValue, name);
	}

	public void setPad(Pad pad)
	{
		Object oldValue = pad;
		this.pad = pad;
		this.firePropertyChange("pad", oldValue, pad);
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
}
