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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

/**
 * A Board describes the physical properties of a PCB and has a list of
 * Placements that will be used to specify pick and place operations.
 */
@Root(name = "openpnp-board")
public class Board extends AbstractModelObject implements PropertyChangeListener
{
	public enum Side
	{
		Bottom, Top
	}

	@Attribute
	private String name;

	@Element(required = false)
	private Outline outline;

	@Element(required = false)
	private Location dimensions = new Location(LengthUnit.Millimeters);

	@ElementList(required = false)
	private ArrayList<Fiducial> fiducials = new ArrayList<>();

	@ElementList
	private ArrayList<Placement> placements = new ArrayList<>();

	@ElementList(required = false)
	private ArrayList<BoardPad> solderPastePads = new ArrayList<>();

	private transient File		file;
	private transient boolean	dirty;

	public Board()
	{
		this(null);
	}

	public Board(File file)
	{
		this.setFile(file);
		this.setOutline(new Outline());
		this.addPropertyChangeListener(this);
	}

	public void addFiducial(Fiducial fiducial)
	{
		ArrayList<Fiducial> oldValue = this.fiducials;
		this.fiducials = new ArrayList<>(this.fiducials);
		this.fiducials.add(fiducial);
		this.firePropertyChange("fiducials", oldValue, this.fiducials);
	}

	public void addPlacement(Placement placement)
	{
		Object oldValue = this.placements;
		this.placements = new ArrayList<>(this.placements);
		this.placements.add(placement);
		this.firePropertyChange("placements", oldValue, this.placements);
		if (placement != null)
			placement.addPropertyChangeListener(this);
	}

	public void addSolderPastePad(BoardPad pad)
	{
		Object oldValue = this.solderPastePads;
		this.solderPastePads = new ArrayList<>(this.solderPastePads);
		this.solderPastePads.add(pad);
		this.firePropertyChange("solderPastePads", oldValue, this.solderPastePads);
		if (pad != null)
			pad.addPropertyChangeListener(this);
	}

	@SuppressWarnings("unused")
	@Commit
	private void commit()
	{
		for (Placement placement : this.placements)
			placement.addPropertyChangeListener(this);
		for (BoardPad pad : this.solderPastePads)
			pad.addPropertyChangeListener(this);
	}

	public Location getDimensions()
	{
		return this.dimensions;
	}

	public List<Fiducial> getFiducials()
	{
		return Collections.unmodifiableList(this.fiducials);
	}

	public File getFile()
	{
		return this.file;
	}

	public String getName()
	{
		return this.name;
	}

	public Outline getOutline()
	{
		return this.outline;
	}

	public List<Placement> getPlacements()
	{
		return Collections.unmodifiableList(this.placements);
	}

	public List<BoardPad> getSolderPastePads()
	{
		return Collections.unmodifiableList(this.solderPastePads);
	}

	public boolean isDirty()
	{
		return this.dirty;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getSource() != Board.this || !evt.getPropertyName().equals("dirty"))
			this.setDirty(true);
	}

	public void removeFiducial(Fiducial fiducial)
	{
		ArrayList<Fiducial> oldValue = this.fiducials;
		this.fiducials = new ArrayList<>(this.fiducials);
		this.fiducials.remove(fiducial);
		this.firePropertyChange("fiducials", oldValue, this.fiducials);
	}

	public void removePlacement(Placement placement)
	{
		Object oldValue = this.placements;
		this.placements = new ArrayList<>(this.placements);
		this.placements.remove(placement);
		this.firePropertyChange("placements", oldValue, this.placements);
		if (placement != null)
			placement.removePropertyChangeListener(this);
	}

	public void removeSolderPastePad(BoardPad pad)
	{
		Object oldValue = this.solderPastePads;
		this.solderPastePads = new ArrayList<>(this.solderPastePads);
		this.solderPastePads.remove(pad);
		this.firePropertyChange("solderPastePads", oldValue, this.solderPastePads);
		if (pad != null)
			pad.removePropertyChangeListener(this);
	}

	public void setDimensions(Location location)
	{
		Location oldValue = this.dimensions;
		this.dimensions = location;
		this.firePropertyChange("dimensions", oldValue, location);
	}

	public void setDirty(boolean dirty)
	{
		boolean oldValue = this.dirty;
		this.dirty = dirty;
		this.firePropertyChange("dirty", oldValue, dirty);
	}

	void setFile(File file)
	{
		Object oldValue = this.file;
		this.file = file;
		this.firePropertyChange("file", oldValue, file);
	}

	public void setName(String name)
	{
		Object oldValue = this.name;
		this.name = name;
		this.firePropertyChange("name", oldValue, name);
	}

	public void setOutline(Outline outline)
	{
		Outline oldValue = this.outline;
		this.outline = outline;
		this.firePropertyChange("outline", oldValue, outline);
	}
}
