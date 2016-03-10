package org.openpnp.spi.base;

import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.openpnp.gui.support.Icons;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Head;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.NozzleTip;
import org.openpnp.util.IdentifiableList;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public abstract class AbstractNozzle implements Nozzle
{
	@ElementList(required = false)
	protected IdentifiableList<NozzleTip> nozzleTips = new IdentifiableList<>();

	@Attribute
	protected String id;

	@Attribute(required = false)
	protected String name;

	protected Head head;

	public AbstractNozzle()
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
	public List<NozzleTip> getNozzleTips()
	{
		return Collections.unmodifiableList(this.nozzleTips);
	}

	@Override
	public Icon getPropertySheetHolderIcon()
	{
		return Icons.captureTool;
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
