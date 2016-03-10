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

package org.openpnp.machine.reference.camera.wizards;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.machine.reference.camera.ImageCamera;
import org.openpnp.machine.reference.wizards.ReferenceCameraConfigurationWizard;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class ImageCameraConfigurationWizard extends ReferenceCameraConfigurationWizard
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ImageCamera camera;

	private JPanel		panelGeneral;
	private JLabel		lblSourceUrl;
	private JTextField	textFieldSourceUrl;
	private JButton		btnBrowse;

	private Action browseAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.NAME, "Browse");
			this.putValue(Action.SHORT_DESCRIPTION, "Browse");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			FileDialog fileDialog = new FileDialog((Frame) ImageCameraConfigurationWizard.this.getTopLevelAncestor());
			fileDialog.setFilenameFilter(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					String[] extensions = new String[]
					{ ".png", ".jpg", ".gif", ".tif", ".tiff" };
					for (String extension : extensions)
						if (name.toLowerCase().endsWith(extension))
							return true;
					return false;
				}
			});
			fileDialog.setVisible(true);
			if (fileDialog.getFile() == null)
				return;
			File file = new File(new File(fileDialog.getDirectory()), fileDialog.getFile());
			ImageCameraConfigurationWizard.this.textFieldSourceUrl.setText(file.toURI().toString());
		}
	};

	public ImageCameraConfigurationWizard(ImageCamera camera)
	{
		super(camera);

		this.camera = camera;

		this.panelGeneral = new JPanel();
		this.contentPanel.add(this.panelGeneral);
		this.panelGeneral.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "General", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.panelGeneral.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, },
				new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.lblSourceUrl = new JLabel("Source URL");
		this.panelGeneral.add(this.lblSourceUrl, "2, 2, right, default");

		this.textFieldSourceUrl = new JTextField();
		this.panelGeneral.add(this.textFieldSourceUrl, "4, 2, fill, default");
		this.textFieldSourceUrl.setColumns(10);

		this.btnBrowse = new JButton(this.browseAction);
		this.panelGeneral.add(this.btnBrowse, "6, 2");
	}

	@Override
	public void createBindings()
	{
		super.createBindings();
		this.addWrappedBinding(this.camera, "sourceUri", this.textFieldSourceUrl, "text");
		ComponentDecorators.decorateWithAutoSelect(this.textFieldSourceUrl);
	}
}
