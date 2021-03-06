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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.openpnp.Main;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private final JPanel		contentPanel		= new JPanel();

	public AboutDialog(Frame frame)
	{
		super(frame, true);
		this.setTitle("About OpenPnP");
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setBounds(100, 100, 347, 360);
		this.getContentPane().setLayout(new BorderLayout());
		this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(this.contentPanel, BorderLayout.CENTER);
		this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.Y_AXIS));
		JLabel lblOpenpnp = new JLabel("OpenPnP");
		lblOpenpnp.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblOpenpnp.setFont(new Font("Lucida Grande", Font.BOLD, 32));
		this.contentPanel.add(lblOpenpnp);
		JLabel lblCopyright = new JLabel("Copyright © 2011, 2012, 2013 Jason von Nieda");
		lblCopyright.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblCopyright.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.contentPanel.add(lblCopyright);
		JLabel lblVersion = new JLabel("Version: " + Main.getVersion());
		lblVersion.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		lblVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.contentPanel.add(lblVersion);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				AboutDialog.this.setVisible(false);
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		this.getRootPane().setDefaultButton(okButton);
	}
}
