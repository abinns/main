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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import org.openpnp.gui.support.LengthCellValue;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.Package;
import org.openpnp.model.Part;

@SuppressWarnings("serial")
public class PartsTableModel extends AbstractTableModel implements PropertyChangeListener
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private String[]			columnNames			= new String[]
														{ "Id", "Description", "Height", "Package", "Speed (0 - 1)" };
	private Class[]				columnTypes			= new Class[]
														{ String.class, String.class, LengthCellValue.class, Package.class, String.class };
	private List<Part>			parts;

	public PartsTableModel()
	{
		Configuration.get().addPropertyChangeListener("parts", this);
		this.parts = new ArrayList<>(Configuration.get().getParts());
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

	public Part getPart(int index)
	{
		return this.parts.get(index);
	}

	@Override
	public int getRowCount()
	{
		return this.parts == null ? 0 : this.parts.size();
	}

	@Override
	public Object getValueAt(int row, int col)
	{
		Part part = this.parts.get(row);
		switch (col)
		{
			case 0:
				return part.getId();
			case 1:
				return part.getName();
			case 2:
				return new LengthCellValue(part.getHeight(), true);
			case 3:
				return part.getPackage();
			case 4:
				return String.format(Locale.US, Configuration.get().getLengthDisplayFormat(), part.getSpeed());
			default:
				return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex != 0;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0)
	{
		this.parts = new ArrayList<>(Configuration.get().getParts());
		this.fireTableDataChanged();
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		try
		{
			Part part = this.parts.get(rowIndex);
			if (columnIndex == 1)
				part.setName((String) aValue);
			else if (columnIndex == 2)
			{
				LengthCellValue value = (LengthCellValue) aValue;
				value.setDisplayNativeUnits(true);
				Length length = value.getLength();
				Length oldLength = part.getHeight();
				if (length.getUnits() == null)
				{
					if (oldLength != null)
						length.setUnits(oldLength.getUnits());
					if (length.getUnits() == null)
						length.setUnits(Configuration.get().getSystemUnits());
				}
				part.setHeight(length);
			} else if (columnIndex == 3)
				part.setPackage((Package) aValue);
			else if (columnIndex == 4)
			{
				double val = Double.parseDouble(aValue.toString());
				val = Math.max(0, val);
				val = Math.min(1, val);
				part.setSpeed(val);
			}
		} catch (Exception e)
		{
			// TODO: dialog, bad input
		}
	}
}
