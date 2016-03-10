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

package org.openpnp.gui.importer;

import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.model.Board;
import org.openpnp.model.Board.Side;
import org.openpnp.model.Configuration;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Package;
import org.openpnp.model.Part;
import org.openpnp.model.Placement;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class EagleMountsmdUlpImporter implements BoardImporter
{
	class Dlg extends JDialog
	{
		private class SwingAction extends AbstractAction
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public SwingAction()
			{
				this.putValue(Action.NAME, "Browse");
				this.putValue(Action.SHORT_DESCRIPTION, "Browse");
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				FileDialog fileDialog = new FileDialog(Dlg.this);
				fileDialog.setFilenameFilter(new FilenameFilter()
				{
					@Override
					public boolean accept(File dir, String name)
					{
						return name.toLowerCase().endsWith(".mnt");
					}
				});
				fileDialog.setVisible(true);
				if (fileDialog.getFile() == null)
					return;
				File file = new File(new File(fileDialog.getDirectory()), fileDialog.getFile());
				Dlg.this.textFieldTopFile.setText(file.getAbsolutePath());
			}
		}

		private class SwingAction_1 extends AbstractAction
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public SwingAction_1()
			{
				this.putValue(Action.NAME, "Browse");
				this.putValue(Action.SHORT_DESCRIPTION, "Browse");
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				FileDialog fileDialog = new FileDialog(Dlg.this);
				fileDialog.setFilenameFilter(new FilenameFilter()
				{
					@Override
					public boolean accept(File dir, String name)
					{
						return name.toLowerCase().endsWith(".mnb");
					}
				});
				fileDialog.setVisible(true);
				if (fileDialog.getFile() == null)
					return;
				File file = new File(new File(fileDialog.getDirectory()), fileDialog.getFile());
				Dlg.this.textFieldBottomFile.setText(file.getAbsolutePath());
			}
		}

		private class SwingAction_2 extends AbstractAction
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public SwingAction_2()
			{
				this.putValue(Action.NAME, "Import");
				this.putValue(Action.SHORT_DESCRIPTION, "Import");
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				EagleMountsmdUlpImporter.this.topFile = new File(Dlg.this.textFieldTopFile.getText());
				EagleMountsmdUlpImporter.this.bottomFile = new File(Dlg.this.textFieldBottomFile.getText());
				EagleMountsmdUlpImporter.this.board = new Board();
				List<Placement> placements = new ArrayList<>();
				try
				{
					if (EagleMountsmdUlpImporter.this.topFile.exists())
						placements.addAll(EagleMountsmdUlpImporter.parseFile(EagleMountsmdUlpImporter.this.topFile, Side.Top, Dlg.this.chckbxCreateMissingParts.isSelected()));
					if (EagleMountsmdUlpImporter.this.bottomFile.exists())
						placements.addAll(EagleMountsmdUlpImporter.parseFile(EagleMountsmdUlpImporter.this.bottomFile, Side.Bottom, Dlg.this.chckbxCreateMissingParts.isSelected()));
				} catch (Exception e1)
				{
					MessageBoxes.errorBox(Dlg.this, "Import Error", e1);
					return;
				}
				for (Placement placement : placements)
					EagleMountsmdUlpImporter.this.board.addPlacement(placement);
				Dlg.this.setVisible(false);
			}
		}

		private class SwingAction_3 extends AbstractAction
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public SwingAction_3()
			{
				this.putValue(Action.NAME, "Cancel");
				this.putValue(Action.SHORT_DESCRIPTION, "Cancel");
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Dlg.this.setVisible(false);
			}
		}

		/**
		 * 
		 */
		private static final long	serialVersionUID	= 1L;
		private JTextField			textFieldTopFile;
		private JTextField			textFieldBottomFile;
		private final Action		browseTopFileAction	= new SwingAction();

		private final Action browseBottomFileAction = new SwingAction_1();

		private final Action importAction = new SwingAction_2();

		private final Action cancelAction = new SwingAction_3();

		private JCheckBox chckbxCreateMissingParts;

		public Dlg(Frame parent)
		{
			super(parent, EagleMountsmdUlpImporter.DESCRIPTION, true);
			this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Files", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			this.getContentPane().add(panel);
			panel.setLayout(new FormLayout(new ColumnSpec[]
			{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, },
					new RowSpec[]
			{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

			JLabel lblTopFilemnt = new JLabel("Top File (.mnt)");
			panel.add(lblTopFilemnt, "2, 2, right, default");

			this.textFieldTopFile = new JTextField();
			panel.add(this.textFieldTopFile, "4, 2, fill, default");
			this.textFieldTopFile.setColumns(10);

			JButton btnBrowse = new JButton("Browse");
			btnBrowse.setAction(this.browseTopFileAction);
			panel.add(btnBrowse, "6, 2");

			JLabel lblBottomFilemnb = new JLabel("Bottom File (.mnb)");
			panel.add(lblBottomFilemnb, "2, 4, right, default");

			this.textFieldBottomFile = new JTextField();
			panel.add(this.textFieldBottomFile, "4, 4, fill, default");
			this.textFieldBottomFile.setColumns(10);

			JButton btnBrowse_1 = new JButton("Browse");
			btnBrowse_1.setAction(this.browseBottomFileAction);
			panel.add(btnBrowse_1, "6, 4");

			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			this.getContentPane().add(panel_1);
			panel_1.setLayout(new FormLayout(new ColumnSpec[]
			{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
			{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

			this.chckbxCreateMissingParts = new JCheckBox("Create Missing Parts");
			this.chckbxCreateMissingParts.setSelected(true);
			panel_1.add(this.chckbxCreateMissingParts, "2, 2");

			JSeparator separator = new JSeparator();
			this.getContentPane().add(separator);

			JPanel panel_2 = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
			flowLayout.setAlignment(FlowLayout.RIGHT);
			this.getContentPane().add(panel_2);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.setAction(this.cancelAction);
			panel_2.add(btnCancel);

			JButton btnImport = new JButton("Import");
			btnImport.setAction(this.importAction);
			panel_2.add(btnImport);

			this.setSize(400, 400);
			this.setLocationRelativeTo(parent);

			JRootPane rootPane = this.getRootPane();
			KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
			InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			inputMap.put(stroke, "ESCAPE");
			rootPane.getActionMap().put("ESCAPE", this.cancelAction);
		}
	}

	private final static String NAME = "EAGLE mountsmd.ulp";

	private final static String DESCRIPTION = "Import files generated by EAGLE's mountsmd.ulp.";

	private static List<Placement> parseFile(File file, Side side, boolean createMissingParts) throws Exception
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		ArrayList<Placement> placements = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null)
		{
			line = line.trim();
			if (line.length() == 0)
				continue;

			// C1 41.91 34.93 180 0.1uF C0805
			// T10 21.59 14.22 90 SOT23-BEC
			// printf("%s %5.2f %5.2f %3.0f %s %s\n",
			Pattern pattern = Pattern.compile("(\\S+)\\s+(-?\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+)\\s+(-?\\d{1,3})\\s(.*)\\s(.*)");
			Matcher matcher = pattern.matcher(line);
			matcher.matches();
			Placement placement = new Placement(matcher.group(1));
			placement.setLocation(new Location(LengthUnit.Millimeters, Double.parseDouble(matcher.group(2)), Double.parseDouble(matcher.group(3)), 0, Double.parseDouble(matcher.group(4))));
			Configuration cfg = Configuration.get();
			if (cfg != null && createMissingParts)
			{
				String value = matcher.group(5);
				String packageId = matcher.group(6);

				String partId = packageId;
				if (value.trim().length() > 0)
					partId += "-" + value;
				Part part = cfg.getPart(partId);
				if (part == null)
				{
					part = new Part(partId);
					Package pkg = cfg.getPackage(packageId);
					if (pkg == null)
					{
						pkg = new Package(packageId);
						cfg.addPackage(pkg);
					}
					part.setPackage(pkg);

					cfg.addPart(part);
				}
				placement.setPart(part);

			}

			placement.setSide(side);
			placements.add(placement);
		}
		reader.close();
		return placements;
	}

	private Board board;

	private File topFile, bottomFile;

	@Override
	public String getImporterDescription()
	{
		return EagleMountsmdUlpImporter.DESCRIPTION;
	}

	@Override
	public String getImporterName()
	{
		return EagleMountsmdUlpImporter.NAME;
	}

	@Override
	public Board importBoard(Frame parent) throws Exception
	{
		Dlg dlg = new Dlg(parent);
		dlg.setVisible(true);
		return this.board;
	}
}
