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
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.openpnp.gui.components.AutoSelectTextTable;
import org.openpnp.gui.components.CameraView;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.components.reticle.FootprintReticle;
import org.openpnp.gui.components.reticle.Reticle;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.Helpers;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.tablemodel.FootprintTableModel;
import org.openpnp.model.Configuration;
import org.openpnp.model.Footprint;
import org.openpnp.model.Footprint.Pad;
import org.openpnp.model.LengthUnit;
import org.openpnp.spi.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class FootprintPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger logger = LoggerFactory.getLogger(FootprintPanel.class);

	private FootprintTableModel	tableModel;
	private JTable				table;

	final private Footprint footprint;

	public final Action newAction = new AbstractAction()
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
			String name;
			while ((name = JOptionPane.showInputDialog(FootprintPanel.this.getTopLevelAncestor(), "Please enter a name for the new pad.")) != null)
			{
				Pad pad = new Pad();
				pad.setName(name);
				FootprintPanel.this.footprint.addPad(pad);
				FootprintPanel.this.tableModel.fireTableDataChanged();
				Helpers.selectLastTableRow(FootprintPanel.this.table);
				break;
			}
		}
	};

	public final Action deleteAction = new AbstractAction()
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
			int ret = JOptionPane.showConfirmDialog(FootprintPanel.this.getTopLevelAncestor(), "Are you sure you want to delete " + FootprintPanel.this.getSelectedPad().getName() + "?",
					"Delete " + FootprintPanel.this.getSelectedPad().getName() + "?", JOptionPane.YES_NO_OPTION);
			if (ret == JOptionPane.YES_OPTION)
				FootprintPanel.this.footprint.removePad(FootprintPanel.this.getSelectedPad());
		}
	};

	private JTextField bodyWidthTf;

	private JTextField bodyHeightTf;

	private JComboBox unitsCombo;

	public FootprintPanel(Footprint footprint)
	{
		this.footprint = footprint;

		this.setLayout(new BorderLayout(0, 0));
		this.tableModel = new FootprintTableModel(footprint);

		this.deleteAction.setEnabled(false);

		JPanel propertiesPanel = new JPanel();
		this.add(propertiesPanel, BorderLayout.NORTH);
		propertiesPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		propertiesPanel.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblUnits = new JLabel("Units");
		propertiesPanel.add(lblUnits, "2, 2, right, default");

		this.unitsCombo = new JComboBox(LengthUnit.values());
		propertiesPanel.add(this.unitsCombo, "4, 2, left, default");

		JLabel lblBodyWidth = new JLabel("Body Width");
		propertiesPanel.add(lblBodyWidth, "2, 4, right, default");

		this.bodyWidthTf = new JTextField();
		propertiesPanel.add(this.bodyWidthTf, "4, 4, left, default");
		this.bodyWidthTf.setColumns(10);

		JLabel lblBodyHeight = new JLabel("Body Height");
		propertiesPanel.add(lblBodyHeight, "2, 6, right, default");

		this.bodyHeightTf = new JTextField();
		propertiesPanel.add(this.bodyHeightTf, "4, 6, left, default");
		this.bodyHeightTf.setColumns(10);

		JPanel tablePanel = new JPanel();
		this.add(tablePanel, BorderLayout.CENTER);
		tablePanel.setBorder(new TitledBorder(null, "Pads", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		this.table = new AutoSelectTextTable(this.tableModel);
		this.table.setAutoCreateRowSorter(true);
		this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;

				Pad pad = FootprintPanel.this.getSelectedPad();

				FootprintPanel.this.deleteAction.setEnabled(pad != null);
			}
		});
		tablePanel.setLayout(new BorderLayout(0, 0));

		JPanel toolbarPanel = new JPanel();
		tablePanel.add(toolbarPanel, BorderLayout.NORTH);
		toolbarPanel.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolbarPanel.add(toolBar);

		JButton btnNew = toolBar.add(this.newAction);
		JButton btnDelete = toolBar.add(this.deleteAction);

		JScrollPane tableScrollPane = new JScrollPane(this.table);
		tablePanel.add(tableScrollPane);

		this.showReticle();
		this.initDataBindings();
	}

	private Pad getSelectedPad()
	{
		int index = this.table.getSelectedRow();
		if (index == -1)
			return null;
		index = this.table.convertRowIndexToModel(index);
		return this.tableModel.getPad(index);
	}

	protected void initDataBindings()
	{
		DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());

		BeanProperty<Footprint, LengthUnit> footprintBeanProperty = BeanProperty.create("units");
		BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
		AutoBinding<Footprint, LengthUnit, JComboBox, Object> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this.footprint, footprintBeanProperty, this.unitsCombo,
				jComboBoxBeanProperty);
		autoBinding.bind();
		//
		BeanProperty<Footprint, Double> footprintBeanProperty_1 = BeanProperty.create("bodyWidth");
		BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
		AutoBinding<Footprint, Double, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this.footprint, footprintBeanProperty_1, this.bodyWidthTf,
				jTextFieldBeanProperty);
		autoBinding_1.setConverter(doubleConverter);
		autoBinding_1.bind();
		//
		BeanProperty<Footprint, Double> footprintBeanProperty_2 = BeanProperty.create("bodyHeight");
		BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
		AutoBinding<Footprint, Double, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this.footprint, footprintBeanProperty_2, this.bodyHeightTf,
				jTextFieldBeanProperty_1);
		autoBinding_2.setConverter(doubleConverter);
		autoBinding_2.bind();

		ComponentDecorators.decorateWithAutoSelect(this.bodyWidthTf);
		ComponentDecorators.decorateWithAutoSelect(this.bodyHeightTf);
	}

	private void showReticle()
	{
		try
		{
			Camera camera = Configuration.get().getMachine().getDefaultHead().getDefaultCamera();
			CameraView cameraView = MainFrame.cameraPanel.getCameraView(camera);
			if (cameraView == null)
				return;
			cameraView.removeReticle(FootprintPanel.class.getName());
			Reticle reticle = new FootprintReticle(this.footprint);
			cameraView.setReticle(FootprintPanel.class.getName(), reticle);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
