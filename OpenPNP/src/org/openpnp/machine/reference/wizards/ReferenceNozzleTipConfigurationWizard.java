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

package org.openpnp.machine.reference.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.ConfigurationListener;
import org.openpnp.gui.components.AutoSelectTextTable;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.components.LocationButtonsPanel;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.machine.reference.ReferenceNozzleTip;
import org.openpnp.model.Configuration;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class ReferenceNozzleTipConfigurationWizard extends AbstractConfigurationWizard
{
	public class PackagesTableModel extends AbstractTableModel
	{
		/**
		 * 
		 */
		private static final long				serialVersionUID	= 1L;
		private String[]						columnNames			= new String[]
																		{ "Package Id", "Compatible?" };
		private List<org.openpnp.model.Package>	packages;

		public PackagesTableModel()
		{
			Configuration.get().addListener(new ConfigurationListener.Adapter()
			{
				@Override
				public void configurationComplete(Configuration configuration) throws Exception
				{
					PackagesTableModel.this.refresh();
				}
			});
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			if (columnIndex == 1)
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

		public org.openpnp.model.Package getPackage(int index)
		{
			return this.packages.get(index);
		}

		@Override
		public int getRowCount()
		{
			return this.packages == null ? 0 : this.packages.size();
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			switch (col)
			{
				case 0:
					return this.packages.get(row).getId();
				case 1:
					return ReferenceNozzleTipConfigurationWizard.this.compatiblePackages.contains(this.packages.get(row));
				default:
					return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex == 1;
		}

		public void refresh()
		{
			this.packages = new ArrayList<>(Configuration.get().getPackages());
			this.fireTableDataChanged();
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			try
			{
				org.openpnp.model.Package pkg = this.packages.get(rowIndex);
				if (columnIndex == 1)
				{
					if ((Boolean) aValue)
						ReferenceNozzleTipConfigurationWizard.this.compatiblePackages.add(pkg);
					else
						ReferenceNozzleTipConfigurationWizard.this.compatiblePackages.remove(pkg);
					ReferenceNozzleTipConfigurationWizard.this.notifyChange();
				}
			} catch (Exception e)
			{
				// TODO: dialog, bad input
			}
		}
	}

	/**
	 * 
	 */
	private static final long			serialVersionUID	= 1L;
	private final ReferenceNozzleTip	nozzleTip;
	private JPanel						panelChanger;
	private JLabel						lblX_1;
	private JLabel						lblY_1;
	private JLabel						lblZ_1;
	private LocationButtonsPanel		changerStartLocationButtonsPanel;
	private JLabel						lblStartLocation;
	private JTextField					textFieldChangerStartX;
	private JTextField					textFieldChangerStartY;
	private JTextField					textFieldChangerStartZ;
	private JLabel						lblMiddleLocation;
	private JTextField					textFieldChangerMidX;
	private JTextField					textFieldChangerMidY;
	private JTextField					textFieldChangerMidZ;
	private JLabel						lblEndLocation;
	private JTextField					textFieldChangerEndX;
	private JTextField					textFieldChangerEndY;
	private JTextField					textFieldChangerEndZ;
	private LocationButtonsPanel		changerMidLocationButtonsPanel;
	private LocationButtonsPanel		changerEndLocationButtonsPanel;
	private JPanel						panelPackageCompat;
	private JCheckBox					chckbxAllowIncompatiblePackages;
	private JScrollPane					scrollPane;
	private JTable						table;

	private PackagesTableModel tableModel;

	private Set<org.openpnp.model.Package> compatiblePackages = new HashSet<>();

	public ReferenceNozzleTipConfigurationWizard(ReferenceNozzleTip nozzleTip)
	{
		this.nozzleTip = nozzleTip;

		this.panelPackageCompat = new JPanel();
		this.panelPackageCompat.setBorder(new TitledBorder(null, "Package Compatibility", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(this.panelPackageCompat);
		this.panelPackageCompat.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("max(100dlu;min)"), }));

		this.chckbxAllowIncompatiblePackages = new JCheckBox("Allow Incompatible Packages?");
		this.panelPackageCompat.add(this.chckbxAllowIncompatiblePackages, "2, 2");

		this.scrollPane = new JScrollPane();
		this.panelPackageCompat.add(this.scrollPane, "2, 4, fill, default");

		this.table = new AutoSelectTextTable(this.tableModel = new PackagesTableModel());
		this.scrollPane.setViewportView(this.table);

		this.panelChanger = new JPanel();
		this.panelChanger.setBorder(new TitledBorder(null, "Nozzle Tip Changer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		this.contentPanel.add(this.panelChanger);
		this.panelChanger.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.lblX_1 = new JLabel("X");
		this.panelChanger.add(this.lblX_1, "4, 2");

		this.lblY_1 = new JLabel("Y");
		this.panelChanger.add(this.lblY_1, "6, 2");

		this.lblZ_1 = new JLabel("Z");
		this.panelChanger.add(this.lblZ_1, "8, 2");

		this.lblStartLocation = new JLabel("Start Location");
		this.panelChanger.add(this.lblStartLocation, "2, 4, right, default");

		this.textFieldChangerStartX = new JTextField();
		this.panelChanger.add(this.textFieldChangerStartX, "4, 4, fill, default");
		this.textFieldChangerStartX.setColumns(5);

		this.textFieldChangerStartY = new JTextField();
		this.panelChanger.add(this.textFieldChangerStartY, "6, 4, fill, default");
		this.textFieldChangerStartY.setColumns(5);

		this.textFieldChangerStartZ = new JTextField();
		this.panelChanger.add(this.textFieldChangerStartZ, "8, 4, fill, default");
		this.textFieldChangerStartZ.setColumns(5);

		this.changerStartLocationButtonsPanel = new LocationButtonsPanel(this.textFieldChangerStartX, this.textFieldChangerStartY, this.textFieldChangerStartZ, (JTextField) null);
		this.changerStartLocationButtonsPanel.setShowPositionToolNoSafeZ(true);
		this.panelChanger.add(this.changerStartLocationButtonsPanel, "10, 4, fill, default");

		this.lblMiddleLocation = new JLabel("Middle Location");
		this.panelChanger.add(this.lblMiddleLocation, "2, 6, right, default");

		this.textFieldChangerMidX = new JTextField();
		this.panelChanger.add(this.textFieldChangerMidX, "4, 6, fill, default");
		this.textFieldChangerMidX.setColumns(5);

		this.textFieldChangerMidY = new JTextField();
		this.panelChanger.add(this.textFieldChangerMidY, "6, 6, fill, default");
		this.textFieldChangerMidY.setColumns(5);

		this.textFieldChangerMidZ = new JTextField();
		this.panelChanger.add(this.textFieldChangerMidZ, "8, 6, fill, default");
		this.textFieldChangerMidZ.setColumns(5);

		this.changerMidLocationButtonsPanel = new LocationButtonsPanel(this.textFieldChangerMidX, this.textFieldChangerMidY, this.textFieldChangerMidZ, (JTextField) null);
		this.changerMidLocationButtonsPanel.setShowPositionToolNoSafeZ(true);
		this.panelChanger.add(this.changerMidLocationButtonsPanel, "10, 6, fill, default");

		this.lblEndLocation = new JLabel("End Location");
		this.panelChanger.add(this.lblEndLocation, "2, 8, right, default");

		this.textFieldChangerEndX = new JTextField();
		this.panelChanger.add(this.textFieldChangerEndX, "4, 8, fill, default");
		this.textFieldChangerEndX.setColumns(5);

		this.textFieldChangerEndY = new JTextField();
		this.panelChanger.add(this.textFieldChangerEndY, "6, 8, fill, default");
		this.textFieldChangerEndY.setColumns(5);

		this.textFieldChangerEndZ = new JTextField();
		this.panelChanger.add(this.textFieldChangerEndZ, "8, 8, fill, default");
		this.textFieldChangerEndZ.setColumns(5);

		this.changerEndLocationButtonsPanel = new LocationButtonsPanel(this.textFieldChangerEndX, this.textFieldChangerEndY, this.textFieldChangerEndZ, (JTextField) null);
		this.changerEndLocationButtonsPanel.setShowPositionToolNoSafeZ(true);
		this.panelChanger.add(this.changerEndLocationButtonsPanel, "10, 8, fill, default");
	}

	@Override
	public void createBindings()
	{
		LengthConverter lengthConverter = new LengthConverter();

		this.addWrappedBinding(this.nozzleTip, "allowIncompatiblePackages", this.chckbxAllowIncompatiblePackages, "selected");

		MutableLocationProxy changerStartLocation = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.nozzleTip, "changerStartLocation", changerStartLocation, "location");
		this.addWrappedBinding(changerStartLocation, "lengthX", this.textFieldChangerStartX, "text", lengthConverter);
		this.addWrappedBinding(changerStartLocation, "lengthY", this.textFieldChangerStartY, "text", lengthConverter);
		this.addWrappedBinding(changerStartLocation, "lengthZ", this.textFieldChangerStartZ, "text", lengthConverter);

		MutableLocationProxy changerMidLocation = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.nozzleTip, "changerMidLocation", changerMidLocation, "location");
		this.addWrappedBinding(changerMidLocation, "lengthX", this.textFieldChangerMidX, "text", lengthConverter);
		this.addWrappedBinding(changerMidLocation, "lengthY", this.textFieldChangerMidY, "text", lengthConverter);
		this.addWrappedBinding(changerMidLocation, "lengthZ", this.textFieldChangerMidZ, "text", lengthConverter);

		MutableLocationProxy changerEndLocation = new MutableLocationProxy();
		this.bind(UpdateStrategy.READ_WRITE, this.nozzleTip, "changerEndLocation", changerEndLocation, "location");
		this.addWrappedBinding(changerEndLocation, "lengthX", this.textFieldChangerEndX, "text", lengthConverter);
		this.addWrappedBinding(changerEndLocation, "lengthY", this.textFieldChangerEndY, "text", lengthConverter);
		this.addWrappedBinding(changerEndLocation, "lengthZ", this.textFieldChangerEndZ, "text", lengthConverter);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldChangerStartX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldChangerStartY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldChangerStartZ);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldChangerMidX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldChangerMidY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldChangerMidZ);

		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldChangerEndX);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldChangerEndY);
		ComponentDecorators.decorateWithAutoSelectAndLengthConversion(this.textFieldChangerEndZ);
	}

	@Override
	protected void loadFromModel()
	{
		this.compatiblePackages.clear();
		this.compatiblePackages.addAll(this.nozzleTip.getCompatiblePackages());
		this.tableModel.refresh();
		super.loadFromModel();
	}

	@Override
	protected void saveToModel()
	{
		this.nozzleTip.setCompatiblePackages(this.compatiblePackages);
		super.saveToModel();
	}
}
