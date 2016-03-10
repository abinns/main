package org.openpnp.spi.base;

import javax.swing.Icon;

import org.openpnp.model.Configuration;
import org.openpnp.spi.Actuator;
import org.openpnp.spi.Head;
import org.simpleframework.xml.Attribute;

public abstract class AbstractActuator implements Actuator
{
	@Attribute
	protected String id;

	@Attribute(required = false)
	protected String name;

	protected Head head;

	public AbstractActuator()
	{
		this.id = Configuration.createId();
		this.name = this.getClass().getSimpleName();
	}

	@Override
	public Head getHead()
	{
		return this.head;
	}

	@Override
	public String getId()
	{
		return this.id;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public Icon getPropertySheetHolderIcon()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHead(Head head)
	{
		this.head = head;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}
}
