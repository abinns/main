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

package org.openpnp.gui.tablemodel;

import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import org.openpnp.gui.support.LengthCellValue;
import org.openpnp.model.Configuration;
import org.openpnp.model.Footprint;
import org.openpnp.model.Footprint.Pad;
import org.openpnp.model.Length;

public class FootprintTableModel extends AbstractTableModel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String[] columnNames = new String[]
	{ "Name", "X", "Y", "Width", "Height", "ø", "% Round" };

	private Class[] columnTypes = new Class[]
	{ String.class, LengthCellValue.class, LengthCellValue.class, LengthCellValue.class, LengthCellValue.class, String.class, String.class };

	final private Footprint footprint;

	public FootprintTableModel(Footprint footprint)
	{
		this.footprint = footprint;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return this.columnTypes[columnIndex];
	}

	@Override
	public int getColumnCount()
	{
		return this.columnNames.length;
	}

	@Override
	public String getColumnName(int column)
	{
		return this.columnNames[column];
	}

	public Pad getPad(int index)
	{
		return this.footprint.getPads().get(index);
	}

	@Override
	public int getRowCount()
	{
		if (this.footprint == null)
			return 0;
		return this.footprint.getPads().size();
	}

	@Override
	public Object getValueAt(int row, int col)
	{
		Pad pad = this.footprint.getPads().get(row);
		switch (col)
		{
			case 0:
				return pad.getName();
			case 1:
				return new LengthCellValue(new Length(pad.getX(), this.footprint.getUnits()), true);
			case 2:
				return new LengthCellValue(new Length(pad.getY(), this.footprint.getUnits()), true);
			case 3:
				return new LengthCellValue(new Length(pad.getWidth(), this.footprint.getUnits()), true);
			case 4:
				return new LengthCellValue(new Length(pad.getHeight(), this.footprint.getUnits()), true);
			case 5:
				return String.format(Locale.US, Configuration.get().getLengthDisplayFormat(), pad.getRotation());
			case 6:
				return String.format(Locale.US, Configuration.get().getLengthDisplayFormat(), pad.getRoundness());
			default:
				return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		try
		{
			Pad pad = this.footprint.getPads().get(rowIndex);
			if (columnIndex == 0)
				pad.setName((String) aValue);
			else if (columnIndex == 1)
			{
				LengthCellValue value = (LengthCellValue) aValue;
				Length length = value.getLength();
				if (length.getUnits() == null)
					length.setUnits(this.footprint.getUnits());
				length = length.convertToUnits(this.footprint.getUnits());
				pad.setX(length.getValue());
			} else if (columnIndex == 2)
			{
				LengthCellValue value = (LengthCellValue) aValue;
				Length length = value.getLength();
				if (length.getUnits() == null)
					length.setUnits(this.footprint.getUnits());
				length = length.convertToUnits(this.footprint.getUnits());
				pad.setY(length.getValue());
			} else if (columnIndex == 3)
			{
				LengthCellValue value = (LengthCellValue) aValue;
				Length length = value.getLength();
				if (length.getUnits() == null)
					length.setUnits(this.footprint.getUnits());
				length = length.convertToUnits(this.footprint.getUnits());
				pad.setWidth(length.getValue());
			} else if (columnIndex == 4)
			{
				LengthCellValue value = (LengthCellValue) aValue;
				Length length = value.getLength();
				if (length.getUnits() == null)
					length.setUnits(this.footprint.getUnits());
				length = length.convertToUnits(this.footprint.getUnits());
				pad.setHeight(length.getValue());
			} else if (columnIndex == 5)
				pad.setRotation(Double.parseDouble(aValue.toString()));
			else if (columnIndex == 6)
			{
				double val = Double.parseDouble(aValue.toString());
				val = Math.max(val, 0);
				val = Math.min(val, 100);
				pad.setRoundness(val);
			}
		} catch (Exception e)
		{
			// TODO: dialog, bad input
		}
	}
}
