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
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.Wizard;
import org.openpnp.gui.support.WizardContainer;
import org.openpnp.model.Configuration;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.PropertySheetHolder.PropertySheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class MachineSetupPanel extends JPanel implements WizardContainer
{
	public class PropertySheetHolderTreeNode implements TreeNode
	{
		private final PropertySheetHolder						obj;
		private final TreeNode									parent;
		private final ArrayList<PropertySheetHolderTreeNode>	children	= new ArrayList<>();

		public PropertySheetHolderTreeNode(PropertySheetHolder obj, TreeNode parent)
		{
			this.obj = obj;
			this.parent = parent;
			PropertySheetHolder[] children = obj.getChildPropertySheetHolders();
			if (children != null)
				for (PropertySheetHolder child : children)
					this.children.add(new PropertySheetHolderTreeNode(child, this));
		}

		@Override
		public Enumeration children()
		{
			return Collections.enumeration(this.children);
		}

		@Override
		public boolean getAllowsChildren()
		{
			return this.children.size() > 0;
		}

		@Override
		public TreeNode getChildAt(int childIndex)
		{
			return this.children.get(childIndex);
		}

		@Override
		public int getChildCount()
		{
			return this.children.size();
		}

		@Override
		public int getIndex(TreeNode node)
		{
			return this.children.indexOf(node);
		}

		@Override
		public TreeNode getParent()
		{
			return this.parent;
		}

		public PropertySheetHolder getPropertySheetHolder()
		{
			return this.obj;
		}

		@Override
		public boolean isLeaf()
		{
			return this.children.size() < 1;
		}

		@Override
		public String toString()
		{
			return this.obj.getPropertySheetHolderTitle();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger	logger					= LoggerFactory.getLogger(MachineSetupPanel.class);
	private static final String	PREF_DIVIDER_POSITION	= "MachineSetupPanel.dividerPosition";

	private static final int PREF_DIVIDER_POSITION_DEF = -1;

	private JTextField	searchTextField;
	private Preferences	prefs	= Preferences.userNodeForPackage(MachineSetupPanel.class);
	private JTree		tree;
	private JTabbedPane	tabbedPane;

	private JToolBar toolBar;

	private TreeCellRenderer treeCellRenderer = new DefaultTreeCellRenderer()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		// http://stackoverflow.com/questions/20691946/set-icon-to-each-node-in-jtree
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			if (value instanceof PropertySheetHolderTreeNode)
			{
				PropertySheetHolderTreeNode node = (PropertySheetHolderTreeNode) value;
				PropertySheetHolder psh = node.getPropertySheetHolder();
				this.setIcon(psh.getPropertySheetHolderIcon());
			}
			return this;
		}
	};

	public MachineSetupPanel()
	{
		this.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		this.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		this.toolBar = new JToolBar();
		this.toolBar.setFloatable(false);
		panel.add(this.toolBar, BorderLayout.CENTER);

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
				MachineSetupPanel.this.search();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				MachineSetupPanel.this.search();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				MachineSetupPanel.this.search();
			}
		});
		panel_1.add(this.searchTextField);
		this.searchTextField.setColumns(15);

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setDividerLocation(this.prefs.getInt(MachineSetupPanel.PREF_DIVIDER_POSITION, MachineSetupPanel.PREF_DIVIDER_POSITION_DEF));
		splitPane.addPropertyChangeListener("dividerLocation", new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				MachineSetupPanel.this.prefs.putInt(MachineSetupPanel.PREF_DIVIDER_POSITION, splitPane.getDividerLocation());
			}
		});
		this.add(splitPane, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);

		this.tree = new JTree();
		this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.tree.setCellRenderer(this.treeCellRenderer);
		scrollPane.setViewportView(this.tree);

		this.tabbedPane = new JTabbedPane(SwingConstants.TOP);
		splitPane.setRightComponent(this.tabbedPane);

		this.tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				MachineSetupPanel.this.tabbedPane.removeAll();
				MachineSetupPanel.this.toolBar.removeAll();

				TreePath path = MachineSetupPanel.this.tree.getSelectionPath();
				for (Object o : path.getPath())
				{
					PropertySheetHolderTreeNode node = (PropertySheetHolderTreeNode) o;
					Action[] actions = node.obj.getPropertySheetHolderActions();
					if (actions != null)
					{
						if (MachineSetupPanel.this.toolBar.getComponentCount() > 0)
							MachineSetupPanel.this.toolBar.addSeparator();
						for (Action action : actions)
							MachineSetupPanel.this.toolBar.add(action);
					}
				}

				PropertySheetHolderTreeNode node = (PropertySheetHolderTreeNode) path.getLastPathComponent();
				if (node != null)
				{
					PropertySheet[] propertySheets = node.obj.getPropertySheets();
					if (propertySheets != null)
						for (PropertySheet propertySheet : propertySheets)
						{
							String title = propertySheet.getPropertySheetTitle();
							JPanel panel = propertySheet.getPropertySheetPanel();
							if (title == null)
								title = "Untitled";
							if (panel != null)
								MachineSetupPanel.this.tabbedPane.add(title, panel);
						}
				}

				MachineSetupPanel.this.revalidate();
				MachineSetupPanel.this.repaint();
			}
		});

		Configuration.get().addListener(new ConfigurationListener()
		{
			@Override
			public void configurationComplete(Configuration configuration) throws Exception
			{
				MachineSetupPanel.this.tree.setModel(new DefaultTreeModel(new PropertySheetHolderTreeNode(Configuration.get().getMachine(), null)));
				for (int i = 0; i < MachineSetupPanel.this.tree.getRowCount(); i++)
					MachineSetupPanel.this.tree.expandRow(i);
			}

			@Override
			public void configurationLoaded(Configuration configuration) throws Exception
			{
			}
		});
	}

	private void search()
	{
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
