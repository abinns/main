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

package org.openpnp.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import org.openpnp.gui.components.AutoSelectTextTable;
import org.openpnp.gui.support.Helpers;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.IdentifiableListCellRenderer;
import org.openpnp.gui.support.IdentifiableTableCellRenderer;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.PackagesComboBoxModel;
import org.openpnp.gui.tablemodel.PartsTableModel;
import org.openpnp.model.Configuration;
import org.openpnp.model.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class PartsPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger logger = LoggerFactory.getLogger(PartsPanel.class);

	final private Configuration	configuration;
	final private Frame			frame;

	private PartsTableModel					partsTableModel;
	private TableRowSorter<PartsTableModel>	partsTableSorter;
	private JTextField						searchTextField;
	private JTable							partsTable;

	public final Action newPartAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.add);
			this.putValue(Action.NAME, "New Part...");
			this.putValue(Action.SHORT_DESCRIPTION, "Create a new part, specifying it's ID.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (Configuration.get().getPackages().size() == 0)
			{
				MessageBoxes.errorBox(PartsPanel.this.getTopLevelAncestor(), "Error",
						"There are currently no packages defined in the system. Please create at least one package before creating a part.");
				return;
			}

			String id;
			while ((id = JOptionPane.showInputDialog(PartsPanel.this.frame, "Please enter an ID for the new part.")) != null)
			{
				if (PartsPanel.this.configuration.getPart(id) != null)
				{
					MessageBoxes.errorBox(PartsPanel.this.frame, "Error", "Part ID " + id + " already exists.");
					continue;
				}
				Part part = new Part(id);

				part.setPackage(Configuration.get().getPackages().get(0));

				PartsPanel.this.configuration.addPart(part);
				PartsPanel.this.partsTableModel.fireTableDataChanged();
				Helpers.selectLastTableRow(PartsPanel.this.partsTable);
				break;
			}
		}
	};

	public final Action deletePartAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.delete);
			this.putValue(Action.NAME, "Delete Part");
			this.putValue(Action.SHORT_DESCRIPTION, "Delete the currently selected part.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			int ret = JOptionPane.showConfirmDialog(PartsPanel.this.getTopLevelAncestor(), "Are you sure you want to delete " + PartsPanel.this.getSelectedPart().getId() + "?",
					"Delete " + PartsPanel.this.getSelectedPart().getId() + "?", JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION)
				Configuration.get().removePart(PartsPanel.this.getSelectedPart());
		}
	};

	public PartsPanel(Configuration configuration, Frame frame)
	{
		this.configuration = configuration;
		this.frame = frame;

		this.setLayout(new BorderLayout(0, 0));
		this.partsTableModel = new PartsTableModel();
		this.partsTableSorter = new TableRowSorter<>(this.partsTableModel);

		JPanel panel_5 = new JPanel();
		this.add(panel_5, BorderLayout.NORTH);
		panel_5.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		panel_5.add(toolBar);

		JPanel panel_1 = new JPanel();
		panel_5.add(panel_1, BorderLayout.EAST);

		JLabel lblSearch = new JLabel("Search");
		panel_1.add(lblSearch);

		this.searchTextField = new JTextField();
		this.searchTextField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				PartsPanel.this.search();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				PartsPanel.this.search();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				PartsPanel.this.search();
			}
		});
		panel_1.add(this.searchTextField);
		this.searchTextField.setColumns(15);

		JComboBox packagesCombo = new JComboBox(new PackagesComboBoxModel());
		packagesCombo.setRenderer(new IdentifiableListCellRenderer<org.openpnp.model.Package>());

		this.partsTable = new AutoSelectTextTable(this.partsTableModel);
		this.partsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.partsTable.setDefaultEditor(org.openpnp.model.Package.class, new DefaultCellEditor(packagesCombo));
		this.partsTable.setDefaultRenderer(org.openpnp.model.Package.class, new IdentifiableTableCellRenderer<org.openpnp.model.Package>());

		this.add(new JScrollPane(this.partsTable), BorderLayout.CENTER);

		this.partsTable.setRowSorter(this.partsTableSorter);

		this.partsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;
				Part part = PartsPanel.this.getSelectedPart();

				PartsPanel.this.deletePartAction.setEnabled(part != null);
			}
		});

		this.deletePartAction.setEnabled(false);

		JButton btnNewPart = toolBar.add(this.newPartAction);
		btnNewPart.setToolTipText("");
		JButton btnDeletePart = toolBar.add(this.deletePartAction);
		btnDeletePart.setToolTipText("");
	}

	private Part getSelectedPart()
	{
		int index = this.partsTable.getSelectedRow();
		if (index == -1)
			return null;
		index = this.partsTable.convertRowIndexToModel(index);
		return this.partsTableModel.getPart(index);
	}

	private void search()
	{
		RowFilter<PartsTableModel, Object> rf = null;
		// If current expression doesn't parse, don't update.
		try
		{
			rf = RowFilter.regexFilter("(?i)" + this.searchTextField.getText().trim());
		} catch (PatternSyntaxException e)
		{
			PartsPanel.logger.warn("Search failed", e);
			return;
		}
		this.partsTableSorter.setRowFilter(rf);
	}
}
