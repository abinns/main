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

package org.openpnp.gui.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.openpnp.gui.components.reticle.CrosshairReticle;
import org.openpnp.gui.components.reticle.FiducialReticle;
import org.openpnp.gui.components.reticle.Reticle;
import org.openpnp.gui.components.reticle.RulerReticle;
import org.openpnp.model.LengthUnit;

// TODO: For the time being, since setting a property on the reticle doesn't re-save it we are
// making a redundant call to setReticle on every property update. Fix that somehow.
@SuppressWarnings("serial")
public class CameraViewPopupMenu extends JPopupMenu
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private CameraView			cameraView;
	private JMenu				reticleMenu;
	private JMenu				reticleOptionsMenu;

	private Action showImageInfoAction = new AbstractAction("Show Image Info?")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			CameraViewPopupMenu.this.cameraView.setShowImageInfo(((JCheckBoxMenuItem) e.getSource()).isSelected());
		}
	};

	private Action noReticleAction = new AbstractAction("None")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			CameraViewPopupMenu.this.setReticleOptionsMenu(null);
			CameraViewPopupMenu.this.cameraView.setDefaultReticle(null);
		}
	};

	private Action crosshairReticleAction = new AbstractAction("Crosshair")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			CrosshairReticle reticle = new CrosshairReticle();
			JMenu optionsMenu = CameraViewPopupMenu.this.createCrosshairReticleOptionsMenu(reticle);
			CameraViewPopupMenu.this.setReticleOptionsMenu(optionsMenu);
			CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
		}
	};

	private Action rulerReticleAction = new AbstractAction("Ruler")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			RulerReticle reticle = new RulerReticle();
			JMenu optionsMenu = CameraViewPopupMenu.this.createRulerReticleOptionsMenu(reticle);
			CameraViewPopupMenu.this.setReticleOptionsMenu(optionsMenu);
			CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
		}
	};

	private Action fiducialReticleAction = new AbstractAction("Fiducial")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			FiducialReticle reticle = new FiducialReticle();
			JMenu optionsMenu = CameraViewPopupMenu.this.createFiducialReticleOptionsMenu(reticle);
			CameraViewPopupMenu.this.setReticleOptionsMenu(optionsMenu);
			CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
		}
	};

	private Action maxFpsAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int maximumFps = Integer.parseInt(e.getActionCommand());
			CameraViewPopupMenu.this.cameraView.setMaximumFps(maximumFps);
		}
	};

	public CameraViewPopupMenu(CameraView cameraView)
	{
		this.cameraView = cameraView;

		this.reticleMenu = this.createReticleMenu();
		JMenu maxFpsMenu = this.createMaxFpsMenu();

		this.add(this.reticleMenu);
		this.add(maxFpsMenu);

		JCheckBoxMenuItem chkShowImageInfo = new JCheckBoxMenuItem(this.showImageInfoAction);
		chkShowImageInfo.setSelected(cameraView.isShowImageInfo());
		this.add(chkShowImageInfo);

		if (cameraView.getDefaultReticle() != null)
			if (cameraView.getDefaultReticle() instanceof RulerReticle)
				this.setReticleOptionsMenu(this.createRulerReticleOptionsMenu((RulerReticle) cameraView.getDefaultReticle()));
			else if (cameraView.getDefaultReticle() instanceof FiducialReticle)
				this.setReticleOptionsMenu(this.createFiducialReticleOptionsMenu((FiducialReticle) cameraView.getDefaultReticle()));
			else if (cameraView.getDefaultReticle() instanceof CrosshairReticle)
				this.setReticleOptionsMenu(this.createCrosshairReticleOptionsMenu((CrosshairReticle) cameraView.getDefaultReticle()));
	}

	private JMenu createCrosshairReticleOptionsMenu(final CrosshairReticle reticle)
	{
		JMenu menu = new JMenu("Options");

		ButtonGroup buttonGroup = new ButtonGroup();

		JRadioButtonMenuItem menuItem;

		menuItem = new JRadioButtonMenuItem("Red");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.red)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.red);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Green");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.green)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.green);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Yellow");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.yellow)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.yellow);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Blue");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.blue)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.blue);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("White");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.white)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.white);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		menu.add(menuItem);

		return menu;
	}

	private JMenu createFiducialReticleOptionsMenu(final FiducialReticle reticle)
	{
		JMenu menu = new JMenu("Options");

		JMenu subMenu;
		JRadioButtonMenuItem menuItem;
		ButtonGroup buttonGroup;

		subMenu = new JMenu("Color");
		buttonGroup = new ButtonGroup();
		menuItem = new JRadioButtonMenuItem("Red");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.red)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.red);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Green");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.green)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.green);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Yellow");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.yellow)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.yellow);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Blue");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.blue)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.blue);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("White");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.white)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.white);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menu.add(subMenu);

		subMenu = new JMenu("Units");
		buttonGroup = new ButtonGroup();
		menuItem = new JRadioButtonMenuItem("Millimeters");
		buttonGroup.add(menuItem);
		if (reticle.getUnits() == LengthUnit.Millimeters)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnits(LengthUnit.Millimeters);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Inches");
		buttonGroup.add(menuItem);
		if (reticle.getUnits() == LengthUnit.Inches)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnits(LengthUnit.Inches);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menu.add(subMenu);

		subMenu = new JMenu("Shape");
		buttonGroup = new ButtonGroup();
		menuItem = new JRadioButtonMenuItem("Circle");
		buttonGroup.add(menuItem);
		if (reticle.getShape() == FiducialReticle.Shape.Circle)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setShape(FiducialReticle.Shape.Circle);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Square");
		buttonGroup.add(menuItem);
		if (reticle.getShape() == FiducialReticle.Shape.Square)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setShape(FiducialReticle.Shape.Square);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menu.add(subMenu);

		JCheckBoxMenuItem chkMenuItem = new JCheckBoxMenuItem("Filled");
		chkMenuItem.setSelected(reticle.isFilled());
		chkMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setFilled(((JCheckBoxMenuItem) e.getSource()).isSelected());
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		menu.add(chkMenuItem);

		JMenuItem inputMenuItem = new JMenuItem("Size");
		inputMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String result = JOptionPane.showInputDialog(CameraViewPopupMenu.this.cameraView, String.format("Enter the size in %s", reticle.getUnits().toString().toLowerCase()),
						reticle.getSize() + "");
				if (result != null)
				{
					reticle.setSize(Double.valueOf(result));
					CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
				}
			}
		});
		menu.add(inputMenuItem);

		return menu;
	}

	private JMenu createMaxFpsMenu()
	{
		ButtonGroup buttonGroup = new ButtonGroup();
		JMenu menu = new JMenu("Maximum FPS");
		JRadioButtonMenuItem menuItem;

		menuItem = new JRadioButtonMenuItem("1");
		menuItem.addActionListener(this.maxFpsAction);
		buttonGroup.add(menuItem);
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("5");
		menuItem.addActionListener(this.maxFpsAction);
		buttonGroup.add(menuItem);
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("10");
		menuItem.addActionListener(this.maxFpsAction);
		buttonGroup.add(menuItem);
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("15");
		menuItem.addActionListener(this.maxFpsAction);
		buttonGroup.add(menuItem);
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("24");
		menuItem.addActionListener(this.maxFpsAction);
		buttonGroup.add(menuItem);
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("30");
		menuItem.addActionListener(this.maxFpsAction);
		buttonGroup.add(menuItem);
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("45");
		menuItem.addActionListener(this.maxFpsAction);
		buttonGroup.add(menuItem);
		menu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("60");
		menuItem.addActionListener(this.maxFpsAction);
		buttonGroup.add(menuItem);
		menu.add(menuItem);

		return menu;
	}

	private JMenu createReticleMenu()
	{
		JMenu menu = new JMenu("Reticle");

		ButtonGroup buttonGroup = new ButtonGroup();

		JRadioButtonMenuItem menuItem;

		Reticle reticle = this.cameraView.getDefaultReticle();

		menuItem = new JRadioButtonMenuItem(this.noReticleAction);
		if (reticle == null)
			menuItem.setSelected(true);
		buttonGroup.add(menuItem);
		menu.add(menuItem);

		menuItem = new JRadioButtonMenuItem(this.crosshairReticleAction);
		if (reticle != null && reticle.getClass() == CrosshairReticle.class)
			menuItem.setSelected(true);
		buttonGroup.add(menuItem);
		menu.add(menuItem);

		menuItem = new JRadioButtonMenuItem(this.rulerReticleAction);
		if (reticle != null && reticle.getClass() == RulerReticle.class)
			menuItem.setSelected(true);
		buttonGroup.add(menuItem);
		menu.add(menuItem);

		menuItem = new JRadioButtonMenuItem(this.fiducialReticleAction);
		if (reticle != null && reticle.getClass() == FiducialReticle.class)
			menuItem.setSelected(true);
		buttonGroup.add(menuItem);
		menu.add(menuItem);

		return menu;
	}

	private JMenu createRulerReticleOptionsMenu(final RulerReticle reticle)
	{
		JMenu menu = new JMenu("Options");

		JMenu subMenu;
		JRadioButtonMenuItem menuItem;
		ButtonGroup buttonGroup;

		subMenu = new JMenu("Color");
		buttonGroup = new ButtonGroup();
		menuItem = new JRadioButtonMenuItem("Red");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.red)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.red);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Green");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.green)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.green);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Yellow");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.yellow)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.yellow);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Blue");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.blue)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.blue);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("White");
		buttonGroup.add(menuItem);
		if (reticle.getColor() == Color.white)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setColor(Color.white);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menu.add(subMenu);

		subMenu = new JMenu("Units");
		buttonGroup = new ButtonGroup();
		menuItem = new JRadioButtonMenuItem("Millimeters");
		buttonGroup.add(menuItem);
		if (reticle.getUnits() == LengthUnit.Millimeters)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnits(LengthUnit.Millimeters);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("Inches");
		buttonGroup.add(menuItem);
		if (reticle.getUnits() == LengthUnit.Inches)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnits(LengthUnit.Inches);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menu.add(subMenu);

		subMenu = new JMenu("Units Per Tick");
		buttonGroup = new ButtonGroup();
		menuItem = new JRadioButtonMenuItem("0.1");
		buttonGroup.add(menuItem);
		if (reticle.getUnitsPerTick() == 0.1)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnitsPerTick(0.1);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("0.25");
		buttonGroup.add(menuItem);
		if (reticle.getUnitsPerTick() == 0.25)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnitsPerTick(0.25);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("0.50");
		buttonGroup.add(menuItem);
		if (reticle.getUnitsPerTick() == 0.50)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnitsPerTick(0.50);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("1");
		buttonGroup.add(menuItem);
		if (reticle.getUnitsPerTick() == 1)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnitsPerTick(1);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("2");
		buttonGroup.add(menuItem);
		if (reticle.getUnitsPerTick() == 2)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnitsPerTick(2);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("5");
		buttonGroup.add(menuItem);
		if (reticle.getUnitsPerTick() == 5)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnitsPerTick(5);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menuItem = new JRadioButtonMenuItem("10");
		buttonGroup.add(menuItem);
		if (reticle.getUnitsPerTick() == 10)
			menuItem.setSelected(true);
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reticle.setUnitsPerTick(10);
				CameraViewPopupMenu.this.cameraView.setDefaultReticle(reticle);
			}
		});
		subMenu.add(menuItem);
		menu.add(subMenu);

		return menu;
	}

	private void setReticleOptionsMenu(JMenu menu)
	{
		if (this.reticleOptionsMenu != null)
		{
			this.reticleMenu.remove(this.reticleMenu.getMenuComponentCount() - 1);
			this.reticleMenu.remove(this.reticleMenu.getMenuComponentCount() - 1);
		}
		if (menu != null)
		{
			this.reticleMenu.addSeparator();
			this.reticleMenu.add(menu);
		}
		this.reticleOptionsMenu = menu;
	}
}
