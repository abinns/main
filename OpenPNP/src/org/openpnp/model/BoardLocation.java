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
import org.simpleframework.xml.core.Commit;

public class BoardLocation extends AbstractModelObject
{
	@Element
	private Location	location;
	@Attribute
	private Side		side	= Side.Top;
	private Board		board;

	@Attribute
	private String boardFile;

	@Attribute(required = false)
	private boolean checkFiducials;

	@Attribute(required = false)
	private boolean enabled = true;

	BoardLocation()
	{
		this.setLocation(new Location(LengthUnit.Millimeters));
	}

	public BoardLocation(Board board)
	{
		this();
		this.setBoard(board);
	}

	@SuppressWarnings("unused")
	@Commit
	private void commit()
	{
		this.setLocation(this.location);
		this.setBoard(this.board);
	}

	public Board getBoard()
	{
		return this.board;
	}

	String getBoardFile()
	{
		return this.boardFile;
	}

	public Location getLocation()
	{
		return this.location;
	}

	public Side getSide()
	{
		return this.side;
	}

	public boolean isCheckFiducials()
	{
		return this.checkFiducials;
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}

	public void setBoard(Board board)
	{
		Board oldValue = this.board;
		this.board = board;
		this.firePropertyChange("board", oldValue, board);
	}

	void setBoardFile(String boardFile)
	{
		this.boardFile = boardFile;
	}

	public void setCheckFiducials(boolean checkFiducials)
	{
		boolean oldValue = this.checkFiducials;
		this.checkFiducials = checkFiducials;
		this.firePropertyChange("checkFiducials", oldValue, checkFiducials);
	}

	public void setEnabled(boolean enabled)
	{
		boolean oldValue = this.enabled;
		this.enabled = enabled;
		this.firePropertyChange("enabled", oldValue, enabled);
	}

	public void setLocation(Location location)
	{
		Location oldValue = this.location;
		this.location = location;
		this.firePropertyChange("location", oldValue, location);
	}

	public void setSide(Side side)
	{
		Object oldValue = this.side;
		this.side = side;
		this.firePropertyChange("side", oldValue, side);
	}

	@Override
	public String toString()
	{
		return String.format("board (%s), location (%s), side (%s)", this.boardFile, this.location, this.side);
	}
}
