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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import org.openpnp.gui.components.AutoSelectTextTable;
import org.openpnp.gui.components.ClassSelectionDialog;
import org.openpnp.gui.support.ActionGroup;
import org.openpnp.gui.support.Helpers;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.support.WizardContainer;
import org.openpnp.gui.tablemodel.FeedersTableModel;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.model.Part;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Feeder;
import org.openpnp.spi.Nozzle;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class FeedersPanel extends JPanel implements WizardContainer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger logger = LoggerFactory.getLogger(FeedersPanel.class);

	private static final String	PREF_DIVIDER_POSITION		= "FeedersPanel.dividerPosition";
	private static final int	PREF_DIVIDER_POSITION_DEF	= -1;

	private final Configuration	configuration;
	private final MainFrame		mainFrame;

	private JTable table;

	private FeedersTableModel					tableModel;
	private TableRowSorter<FeedersTableModel>	tableSorter;
	private JTextField							searchTextField;
	private JPanel								configurationPanel;

	private ActionGroup feederSelectedActionGroup;

	private Preferences prefs = Preferences.userNodeForPackage(FeedersPanel.class);

	public Action newFeederAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.add);
			this.putValue(Action.NAME, "New Feeder...");
			this.putValue(Action.SHORT_DESCRIPTION, "Create a new feeder.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			FeedersPanel.this.newFeeder(null);
		}
	};

	public Action deleteFeederAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.delete);
			this.putValue(Action.NAME, "Delete Feeder");
			this.putValue(Action.SHORT_DESCRIPTION, "Delete the selected feeder.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			int ret = JOptionPane.showConfirmDialog(FeedersPanel.this.getTopLevelAncestor(), "Are you sure you want to delete " + FeedersPanel.this.getSelectedFeeder().getName() + "?",
					"Delete " + FeedersPanel.this.getSelectedFeeder().getName() + "?", JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION)
			{
				FeedersPanel.this.configuration.getMachine().removeFeeder(FeedersPanel.this.getSelectedFeeder());
				FeedersPanel.this.tableModel.refresh();
			}
		}
	};

	public Action feedFeederAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.feed);
			this.putValue(Action.NAME, "Feed");
			this.putValue(Action.SHORT_DESCRIPTION, "Command the selected feeder to perform a feed operation.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					Feeder feeder = FeedersPanel.this.getSelectedFeeder();
					Nozzle nozzle = MainFrame.machineControlsPanel.getSelectedNozzle();

					try
					{
						nozzle.moveToSafeZ(1.0);
						feeder.feed(nozzle);
						Location pickLocation = feeder.getPickLocation();
						MovableUtils.moveToLocationAtSafeZ(nozzle, pickLocation, 1.0);
					} catch (Exception e)
					{
						MessageBoxes.errorBox(FeedersPanel.this, "Feed Error", e);
					}
				}
			}.start();
		}
	};

	public Action pickFeederAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.load);
			this.putValue(Action.NAME, "Pick");
			this.putValue(Action.SHORT_DESCRIPTION, "Perform a feed and pick on the selected feeder.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					Feeder feeder = FeedersPanel.this.getSelectedFeeder();
					Nozzle nozzle = MainFrame.machineControlsPanel.getSelectedNozzle();

					try
					{
						nozzle.moveToSafeZ(1.0);
						feeder.feed(nozzle);
						Location pickLocation = feeder.getPickLocation();
						MovableUtils.moveToLocationAtSafeZ(nozzle, pickLocation, 1.0);
						nozzle.pick();
						nozzle.moveToSafeZ(1.0);
					} catch (Exception e)
					{
						MessageBoxes.errorBox(FeedersPanel.this, "Feed Error", e);
					}
				}
			}.start();
		}
	};

	public Action moveCameraToPickLocation = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.centerCamera);
			this.putValue(Action.NAME, "Move Camera");
			this.putValue(Action.SHORT_DESCRIPTION, "Move the camera to the selected feeder's current pick location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				Feeder feeder = FeedersPanel.this.getSelectedFeeder();
				Camera camera = MainFrame.machineControlsPanel.getSelectedTool().getHead().getDefaultCamera();
				Location pickLocation = feeder.getPickLocation();
				MovableUtils.moveToLocationAtSafeZ(camera, pickLocation, 1.0);
			});
		}
	};

	public Action moveToolToPickLocation = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.centerTool);
			this.putValue(Action.NAME, "Move Tool");
			this.putValue(Action.SHORT_DESCRIPTION, "Move the tool to the selected feeder's current pick location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					Feeder feeder = FeedersPanel.this.getSelectedFeeder();
					Nozzle nozzle = MainFrame.machineControlsPanel.getSelectedNozzle();

					try
					{
						Location pickLocation = feeder.getPickLocation();
						MovableUtils.moveToLocationAtSafeZ(nozzle, pickLocation, 1.0);
					} catch (Exception e)
					{
						MessageBoxes.errorBox(FeedersPanel.this, "Movement Error", e);
					}
				}
			}.start();
		}
	};

	public FeedersPanel(Configuration configuration, MainFrame mainFrame)
	{
		this.configuration = configuration;
		this.mainFrame = mainFrame;

		this.setLayout(new BorderLayout(0, 0));
		this.tableModel = new FeedersTableModel(configuration);

		JPanel panel = new JPanel();
		this.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		panel.add(toolBar, BorderLayout.CENTER);

		JButton btnNewFeeder = new JButton(this.newFeederAction);
		btnNewFeeder.setHideActionText(true);
		toolBar.add(btnNewFeeder);

		JButton btnDeleteFeeder = new JButton(this.deleteFeederAction);
		btnDeleteFeeder.setHideActionText(true);
		toolBar.add(btnDeleteFeeder);

		toolBar.addSeparator();
		toolBar.add(this.feedFeederAction);
		toolBar.add(this.moveCameraToPickLocation);
		toolBar.add(this.moveToolToPickLocation);
		toolBar.add(this.pickFeederAction);

		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.EAST);

		JLabel lblSearch = new JLabel("Search");
		panel_1.add(lblSearch);

		this.searchTextField = new JTextField();
		this.searchTextField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				FeedersPanel.this.search();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				FeedersPanel.this.search();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				FeedersPanel.this.search();
			}
		});
		panel_1.add(this.searchTextField);
		this.searchTextField.setColumns(15);
		this.table = new AutoSelectTextTable(this.tableModel);
		this.tableSorter = new TableRowSorter<>(this.tableModel);

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(this.prefs.getInt(FeedersPanel.PREF_DIVIDER_POSITION, FeedersPanel.PREF_DIVIDER_POSITION_DEF));
		splitPane.addPropertyChangeListener("dividerLocation", new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				FeedersPanel.this.prefs.putInt(FeedersPanel.PREF_DIVIDER_POSITION, splitPane.getDividerLocation());
			}
		});
		this.add(splitPane, BorderLayout.CENTER);

		this.table.setRowSorter(this.tableSorter);
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		this.configurationPanel = new JPanel();
		this.configurationPanel.setBorder(new TitledBorder(null, "Configuration", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		this.feederSelectedActionGroup = new ActionGroup(this.deleteFeederAction, this.feedFeederAction, this.pickFeederAction, this.moveCameraToPickLocation, this.moveToolToPickLocation);

		this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;

				Feeder feeder = FeedersPanel.this.getSelectedFeeder();

				FeedersPanel.this.feederSelectedActionGroup.setEnabled(feeder != null);

				FeedersPanel.this.configurationPanel.removeAll();
				if (feeder != null)
				{
					Wizard wizard = feeder.getConfigurationWizard();
					if (wizard != null)
					{
						wizard.setWizardContainer(FeedersPanel.this);
						JPanel panel = wizard.getWizardPanel();
						FeedersPanel.this.configurationPanel.add(panel);
					}
				}
				FeedersPanel.this.revalidate();
				FeedersPanel.this.repaint();
			}
		});

		this.feederSelectedActionGroup.setEnabled(false);

		splitPane.setLeftComponent(new JScrollPane(this.table));
		splitPane.setRightComponent(this.configurationPanel);
		this.configurationPanel.setLayout(new BorderLayout(0, 0));
	}

	private Feeder findFeeder(Part part)
	{
		for (int i = 0; i < this.tableModel.getRowCount(); i++)
			if (this.tableModel.getFeeder(i).getPart() == part)
				return this.tableModel.getFeeder(i);
		return null;
	}

	private Feeder getSelectedFeeder()
	{
		int index = this.table.getSelectedRow();

		if (index == -1)
			return null;

		index = this.table.convertRowIndexToModel(index);
		return this.tableModel.getFeeder(index);
	}

	private void newFeeder(Part part)
	{
		if (Configuration.get().getParts().size() == 0)
		{
			MessageBoxes.errorBox(this.getTopLevelAncestor(), "Error", "There are currently no parts defined in the system. Please create at least one part before creating a feeder.");
			return;
		}

		String title;
		if (part == null)
			title = "Select Feeder...";
		else
			title = "Select Feeder for " + part.getId() + "...";
		ClassSelectionDialog<Feeder> dialog = new ClassSelectionDialog<>(JOptionPane.getFrameForComponent(FeedersPanel.this), title, "Please select a Feeder implemention from the list below.",
				this.configuration.getMachine().getCompatibleFeederClasses());
		dialog.setVisible(true);
		Class<? extends Feeder> feederClass = dialog.getSelectedClass();
		if (feederClass == null)
			return;
		try
		{
			Feeder feeder = feederClass.newInstance();

			feeder.setPart(part == null ? Configuration.get().getParts().get(0) : part);

			this.configuration.getMachine().addFeeder(feeder);
			this.tableModel.refresh();
			Helpers.selectLastTableRow(this.table);
		} catch (Exception e)
		{
			MessageBoxes.errorBox(JOptionPane.getFrameForComponent(FeedersPanel.this), "Feeder Error", e);
		}
	}

	private void search()
	{
		RowFilter<FeedersTableModel, Object> rf = null;
		// If current expression doesn't parse, don't update.
		try
		{
			rf = RowFilter.regexFilter("(?i)" + this.searchTextField.getText().trim());
		} catch (PatternSyntaxException e)
		{
			FeedersPanel.logger.warn("Search failed", e);
			return;
		}
		this.tableSorter.setRowFilter(rf);
	}

	/**
	 * Activate the Feeders tab and show the Feeder for the specified Part. If
	 * none exists, prompt the user to create a new one.
	 * 
	 * @param feeder
	 */
	public void showFeederForPart(Part part)
	{
		this.mainFrame.showTab("Feeders");

		Feeder feeder = this.findFeeder(part);
		if (feeder == null)
			this.newFeeder(part);
		else
		{
			this.table.getSelectionModel().clearSelection();
			for (int i = 0; i < this.tableModel.getRowCount(); i++)
				if (this.tableModel.getFeeder(i).getPart() == part)
				{
					this.table.getSelectionModel().setSelectionInterval(0, i);
					break;
				}
		}
	}

	@Override
	public void wizardCancelled(Wizard wizard)
	{
	}

	@Override
	public void wizardCompleted(Wizard wizard)
	{
		// Repaint the table so that any changed fields get updated.
		this.table.repaint();
	}
}
