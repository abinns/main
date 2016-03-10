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

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

/**
 * A Job specifies a list of one or more BoardLocations.
 */
@Root(name = "openpnp-job")
public class Job extends AbstractModelObject implements PropertyChangeListener
{
	@ElementList
	private ArrayList<BoardLocation> boardLocations = new ArrayList<>();

	private transient File		file;
	private transient boolean	dirty;

	public Job()
	{
		this.addPropertyChangeListener(this);
	}

	public void addBoardLocation(BoardLocation boardLocation)
	{
		Object oldValue = this.boardLocations;
		this.boardLocations = new ArrayList<>(this.boardLocations);
		this.boardLocations.add(boardLocation);
		this.firePropertyChange("boardLocations", oldValue, this.boardLocations);
		boardLocation.addPropertyChangeListener(this);
	}

	@SuppressWarnings("unused")
	@Commit
	private void commit()
	{
		for (BoardLocation boardLocation : this.boardLocations)
			boardLocation.addPropertyChangeListener(this);
	}

	public List<BoardLocation> getBoardLocations()
	{
		return Collections.unmodifiableList(this.boardLocations);
	}

	public File getFile()
	{
		return this.file;
	}

	public boolean isDirty()
	{
		return this.dirty;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getSource() != Job.this || !evt.getPropertyName().equals("dirty"))
			this.setDirty(true);
	}

	public void removeBoardLocation(BoardLocation boardLocation)
	{
		Object oldValue = this.boardLocations;
		this.boardLocations = new ArrayList<>(this.boardLocations);
		this.boardLocations.remove(boardLocation);
		this.firePropertyChange("boardLocations", oldValue, this.boardLocations);
		boardLocation.removePropertyChangeListener(this);
	}

	public void setDirty(boolean dirty)
	{
		boolean oldValue = this.dirty;
		this.dirty = dirty;
		this.firePropertyChange("dirty", oldValue, dirty);
	}

	public void setFile(File file)
	{
		Object oldValue = this.file;
		this.file = file;
		this.firePropertyChange("file", oldValue, file);
	}
}
