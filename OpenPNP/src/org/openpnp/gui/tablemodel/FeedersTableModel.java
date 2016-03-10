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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.openpnp.ConfigurationListener;
import org.openpnp.model.Configuration;
import org.openpnp.model.Part;
import org.openpnp.spi.Feeder;

public class FeedersTableModel extends AbstractTableModel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final private Configuration configuration;

	private String[]		columnNames	= new String[]
											{ "Name", "Type", "Part", "Enabled" };
	private List<Feeder>	feeders;

	public FeedersTableModel(Configuration configuration)
	{
		this.configuration = configuration;
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationComplete(Configuration configuration) throws Exception
			{
				FeedersTableModel.this.refresh();
			}
		});
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		if (columnIndex == 3)
			return Boolean.class;
		return super.getColumnClass(columnIndex);
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

	public Feeder getFeeder(int index)
	{
		return this.feeders.get(index);
	}

	@Override
	public int getRowCount()
	{
		return this.feeders == null ? 0 : this.feeders.size();
	}

	@Override
	public Object getValueAt(int row, int col)
	{
		switch (col)
		{
			case 0:
				return this.feeders.get(row).getName();
			case 1:
				return this.feeders.get(row).getClass().getSimpleName();
			case 2:
			{
				Part part = this.feeders.get(row).getPart();
				if (part == null)
					return null;
				return part.getId();
			}
			case 3:
				return this.feeders.get(row).isEnabled();
			default:
				return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex == 0 || columnIndex == 3;
	}

	public void refresh()
	{
		this.feeders = new ArrayList<>(this.configuration.getMachine().getFeeders());
		this.fireTableDataChanged();
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		try
		{
			Feeder feeder = this.feeders.get(rowIndex);
			if (columnIndex == 0)
				feeder.setName((String) aValue);
			else if (columnIndex == 3)
				feeder.setEnabled((Boolean) aValue);
		} catch (Exception e)
		{
			// TODO: dialog, bad input
		}
	}
}
