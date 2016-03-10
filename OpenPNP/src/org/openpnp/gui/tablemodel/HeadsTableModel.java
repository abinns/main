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
import org.openpnp.spi.Head;

public class HeadsTableModel extends AbstractTableModel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final private Configuration configuration;

	private String[]	columnNames	= new String[]
										{ "Name", "Type" };
	private List<Head>	heads;

	public HeadsTableModel(Configuration configuration)
	{
		this.configuration = configuration;
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationComplete(Configuration configuration) throws Exception
			{
				HeadsTableModel.this.heads = new ArrayList<>(configuration.getMachine().getHeads());
				HeadsTableModel.this.fireTableDataChanged();
			}
		});
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

	public Head getHead(int index)
	{
		return this.heads.get(index);
	}

	@Override
	public int getRowCount()
	{
		return this.heads == null ? 0 : this.heads.size();
	}

	@Override
	public Object getValueAt(int row, int col)
	{
		Head head = this.heads.get(row);
		switch (col)
		{
			case 0:
				return head.getName();
			case 1:
				return head.getClass().getSimpleName();
			default:
				return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}
}
