package org.openpnp.spi.base;

import javax.swing.Icon;

import org.openpnp.model.Configuration;
import org.openpnp.spi.NozzleTip;
import org.simpleframework.xml.Attribute;

public abstract class AbstractNozzleTip implements NozzleTip
{
	@Attribute
	protected String id;

	@Attribute(required = false)
	protected String name;

	public AbstractNozzleTip()
	{
		this.id = Configuration.createId();
		this.name = this.getClass().getSimpleName();
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
		return null;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}
}
