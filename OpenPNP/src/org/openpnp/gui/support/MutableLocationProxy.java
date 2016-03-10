package org.openpnp.gui.support;

import org.openpnp.model.AbstractModelObject;
import org.openpnp.model.Length;
import org.openpnp.model.Location;

/**
 * A proxy class that allows bindings to mutate a Location field by field by
 * replacing the bound Location whenever a field is changed.
 */
public class MutableLocationProxy extends AbstractModelObject
{
	private Location location;

	public Length getLengthX()
	{
		return this.location.getLengthX();
	}

	public Length getLengthY()
	{
		return this.location.getLengthY();
	}

	public Length getLengthZ()
	{
		return this.location.getLengthZ();
	}

	public Location getLocation()
	{
		return this.location;
	}

	public double getRotation()
	{
		return this.location.getRotation();
	}

	public void setLengthX(Length l)
	{
		if (l.getUnits() != this.location.getUnits())
		{
			this.location = this.location.convertToUnits(l.getUnits());
			this.location = this.location.derive(l.getValue(), null, null, null);
			this.firePropertyChange("lengthX", null, this.getLengthX());
			this.firePropertyChange("lengthY", null, this.getLengthY());
			this.firePropertyChange("lengthZ", null, this.getLengthZ());
			this.firePropertyChange("location", null, this.getLocation());
		} else
		{
			this.location = this.location.derive(l.getValue(), null, null, null);
			this.firePropertyChange("lengthX", null, this.getLengthX());
			this.firePropertyChange("location", null, this.getLocation());
		}
	}

	public void setLengthY(Length l)
	{
		if (l.getUnits() != this.location.getUnits())
		{
			this.location = this.location.convertToUnits(l.getUnits());
			this.location = this.location.derive(null, l.getValue(), null, null);
			this.firePropertyChange("lengthX", null, this.getLengthX());
			this.firePropertyChange("lengthY", null, this.getLengthY());
			this.firePropertyChange("lengthZ", null, this.getLengthZ());
			this.firePropertyChange("location", null, this.getLocation());
		} else
		{
			this.location = this.location.derive(null, l.getValue(), null, null);
			this.firePropertyChange("lengthY", null, this.getLengthY());
			this.firePropertyChange("location", null, this.getLocation());
		}
	}

	public void setLengthZ(Length l)
	{
		if (l.getUnits() != this.location.getUnits())
		{
			this.location = this.location.convertToUnits(l.getUnits());
			this.location = this.location.derive(null, null, l.getValue(), null);
			this.firePropertyChange("lengthX", null, this.getLengthX());
			this.firePropertyChange("lengthY", null, this.getLengthY());
			this.firePropertyChange("lengthZ", null, this.getLengthZ());
			this.firePropertyChange("location", null, this.getLocation());
		} else
		{
			this.location = this.location.derive(null, null, l.getValue(), null);
			this.firePropertyChange("lengthZ", null, this.getLengthY());
			this.firePropertyChange("location", null, this.getLocation());
		}
	}

	public void setLocation(Location location)
	{
		this.location = location;
		this.firePropertyChange("location", null, this.getLocation());
	}

	public void setRotation(double rotation)
	{
		this.location = this.location.derive(null, null, null, rotation);
		this.firePropertyChange("rotation", null, this.getRotation());
		this.firePropertyChange("location", null, this.getLocation());
	}
}
