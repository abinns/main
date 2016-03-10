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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;
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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.components.AutoSelectTextTable;
import org.openpnp.gui.components.ClassSelectionDialog;
import org.openpnp.gui.support.HeadCellValue;
import org.openpnp.gui.support.Helpers;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.support.WizardContainer;
import org.openpnp.gui.tablemodel.CamerasTableModel;
import org.openpnp.gui.wizards.CameraConfigurationWizard;
import org.openpnp.machine.reference.vision.OpenCvVisionProvider;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Camera.Looking;
import org.openpnp.spi.Head;
import org.openpnp.spi.VisionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class CamerasPanel extends JPanel implements WizardContainer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger logger = LoggerFactory.getLogger(CamerasPanel.class);

	private static final String	PREF_DIVIDER_POSITION		= "CamerasPanel.dividerPosition";
	private static final int	PREF_DIVIDER_POSITION_DEF	= -1;

	private final Frame			frame;
	private final Configuration	configuration;

	private JTable table;

	private CamerasTableModel					tableModel;
	private TableRowSorter<CamerasTableModel>	tableSorter;
	private JTextField							searchTextField;
	private JComboBox							headsComboBox;

	private Preferences prefs = Preferences.userNodeForPackage(CamerasPanel.class);

	public Action newCameraAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.add);
			this.putValue(Action.NAME, "New Camera...");
			this.putValue(Action.SHORT_DESCRIPTION, "Create a new camera.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			ClassSelectionDialog<Camera> dialog = new ClassSelectionDialog<>(JOptionPane.getFrameForComponent(CamerasPanel.this), "Select Camera...",
					"Please select a Camera implemention from the list below.", CamerasPanel.this.configuration.getMachine().getCompatibleCameraClasses());
			dialog.setVisible(true);
			Class<? extends Camera> cameraClass = dialog.getSelectedClass();
			if (cameraClass == null)
				return;
			try
			{
				Camera camera = cameraClass.newInstance();

				camera.setUnitsPerPixel(new Location(Configuration.get().getSystemUnits()));
				try
				{
					if (camera.getVisionProvider() == null)
						camera.setVisionProvider(new OpenCvVisionProvider());
				} catch (Exception e)
				{
					CamerasPanel.logger.debug("Couldn't set default vision provider. Meh.");
				}

				CamerasPanel.this.configuration.getMachine().addCamera(camera);

				MainFrame.cameraPanel.addCamera(camera);
				CamerasPanel.this.tableModel.refresh();
				Helpers.selectLastTableRow(CamerasPanel.this.table);
			} catch (Exception e)
			{
				MessageBoxes.errorBox(JOptionPane.getFrameForComponent(CamerasPanel.this), "Camera Error", e);
			}
		}
	};

	public Action deleteCameraAction = new AbstractAction("Delete Camera")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.delete);
			this.putValue(Action.NAME, "Delete Camera");
			this.putValue(Action.SHORT_DESCRIPTION, "Delete the currently selected camera.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			Camera camera = CamerasPanel.this.getSelectedCamera();
			int ret = JOptionPane.showConfirmDialog(CamerasPanel.this.getTopLevelAncestor(), "Are you sure you want to delete " + camera.getName() + "?", "Delete " + camera.getName() + "?",
					JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION)
			{
				if (camera.getHead() != null)
					camera.getHead().removeCamera(camera);
				else
					CamerasPanel.this.configuration.getMachine().removeCamera(camera);
				CamerasPanel.this.tableModel.refresh();
				MessageBoxes.errorBox(CamerasPanel.this.getTopLevelAncestor(), "Restart Required", camera.getName() + " has been removed. Please restart OpenPnP for the changes to take effect.");
			}
		}
	};

	private JPanel generalConfigPanel;

	private JPanel cameraSpecificConfigPanel;

	private JPanel visionProviderConfigPanel;

	public CamerasPanel(Frame frame, Configuration configuration)
	{
		this.frame = frame;
		this.configuration = configuration;

		this.setLayout(new BorderLayout(0, 0));
		this.tableModel = new CamerasTableModel(configuration);

		JPanel panel = new JPanel();
		this.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		panel.add(toolBar, BorderLayout.CENTER);

		JButton btnNewCamera = new JButton(this.newCameraAction);
		btnNewCamera.setHideActionText(true);
		toolBar.add(btnNewCamera);

		JButton btnDeleteCamera = new JButton(this.deleteCameraAction);
		btnDeleteCamera.setHideActionText(true);
		toolBar.add(btnDeleteCamera);

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
				CamerasPanel.this.search();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				CamerasPanel.this.search();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				CamerasPanel.this.search();
			}
		});
		panel_1.add(this.searchTextField);
		this.searchTextField.setColumns(15);

		JComboBox lookingComboBox = new JComboBox(Looking.values());
		this.headsComboBox = new JComboBox();

		this.table = new AutoSelectTextTable(this.tableModel);
		this.tableSorter = new TableRowSorter<>(this.tableModel);
		this.table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(lookingComboBox));
		this.table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(this.headsComboBox));

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(this.prefs.getInt(CamerasPanel.PREF_DIVIDER_POSITION, CamerasPanel.PREF_DIVIDER_POSITION_DEF));
		splitPane.addPropertyChangeListener("dividerLocation", new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				CamerasPanel.this.prefs.putInt(CamerasPanel.PREF_DIVIDER_POSITION, splitPane.getDividerLocation());
			}
		});

		this.add(splitPane, BorderLayout.CENTER);
		splitPane.setLeftComponent(new JScrollPane(this.table));
		this.table.setRowSorter(this.tableSorter);
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		splitPane.setRightComponent(tabbedPane);

		this.generalConfigPanel = new JPanel();
		tabbedPane.addTab("General Configuration", null, this.generalConfigPanel, null);
		this.generalConfigPanel.setLayout(new BorderLayout(0, 0));

		this.cameraSpecificConfigPanel = new JPanel();
		tabbedPane.addTab("Camera Specific", null, this.cameraSpecificConfigPanel, null);
		this.cameraSpecificConfigPanel.setLayout(new BorderLayout(0, 0));

		this.visionProviderConfigPanel = new JPanel();
		tabbedPane.addTab("Vision Provider", null, this.visionProviderConfigPanel, null);
		this.visionProviderConfigPanel.setLayout(new BorderLayout(0, 0));

		this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;
				int index = CamerasPanel.this.table.getSelectedRow();

				CamerasPanel.this.generalConfigPanel.removeAll();
				CamerasPanel.this.cameraSpecificConfigPanel.removeAll();
				CamerasPanel.this.visionProviderConfigPanel.removeAll();

				if (index != -1)
				{
					index = CamerasPanel.this.table.convertRowIndexToModel(index);
					Camera camera = CamerasPanel.this.tableModel.getCamera(index);
					Wizard generalConfigWizard = new CameraConfigurationWizard(camera);
					if (generalConfigWizard != null)
					{
						generalConfigWizard.setWizardContainer(CamerasPanel.this);
						JPanel panel = generalConfigWizard.getWizardPanel();
						CamerasPanel.this.generalConfigPanel.add(panel);
					}

					Wizard cameraSpecificConfigWizard = camera.getConfigurationWizard();
					if (cameraSpecificConfigWizard != null)
					{
						cameraSpecificConfigWizard.setWizardContainer(CamerasPanel.this);
						JPanel panel = cameraSpecificConfigWizard.getWizardPanel();
						CamerasPanel.this.cameraSpecificConfigPanel.add(panel);
					}

					VisionProvider visionProvider = camera.getVisionProvider();
					if (visionProvider != null)
					{
						Wizard visionProviderConfigWizard = visionProvider.getConfigurationWizard();
						if (visionProviderConfigWizard != null)
						{
							visionProviderConfigWizard.setWizardContainer(CamerasPanel.this);
							JPanel panel = visionProviderConfigWizard.getWizardPanel();
							CamerasPanel.this.visionProviderConfigPanel.add(panel);
						}
					}
				}

				CamerasPanel.this.revalidate();
				CamerasPanel.this.repaint();
			}
		});

		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationComplete(Configuration configuration) throws Exception
			{
				CamerasPanel.this.headsComboBox.removeAllItems();
				CamerasPanel.this.headsComboBox.addItem(new HeadCellValue((Head) null));
				for (Head head : configuration.getMachine().getHeads())
					CamerasPanel.this.headsComboBox.addItem(new HeadCellValue(head));
			}
		});
	}

	private Camera getSelectedCamera()
	{
		int index = this.table.getSelectedRow();

		if (index == -1)
			return null;

		index = this.table.convertRowIndexToModel(index);
		return this.tableModel.getCamera(index);
	}

	private void search()
	{
		RowFilter<CamerasTableModel, Object> rf = null;
		// If current expression doesn't parse, don't update.
		try
		{
			rf = RowFilter.regexFilter("(?i)" + this.searchTextField.getText().trim());
		} catch (PatternSyntaxException e)
		{
			CamerasPanel.logger.warn("Search failed", e);
			return;
		}
		this.tableSorter.setRowFilter(rf);
	}

	@Override
	public void wizardCancelled(Wizard wizard)
	{
	}

	@Override
	public void wizardCompleted(Wizard wizard)
	{
	}
}
