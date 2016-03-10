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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.components.CameraPanel;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.NozzleItem;
import org.openpnp.model.Configuration;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Head;
import org.openpnp.spi.HeadMountable;
import org.openpnp.spi.JobProcessor;
import org.openpnp.spi.Machine;
import org.openpnp.spi.MachineListener;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.PasteDispenser;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class MachineControlsPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private final JFrame		frame;
	private final CameraPanel	cameraPanel;
	private final Configuration	configuration;

	private Nozzle selectedNozzle;

	private JTextField	textFieldX;
	private JTextField	textFieldY;
	private JTextField	textFieldC;
	private JTextField	textFieldZ;
	private JButton		btnStartStop;
	private JSlider		sliderIncrements;
	private JComboBox	comboBoxNozzles;

	private Color	startColor		= Color.green;
	private Color	stopColor		= new Color(178, 34, 34);
	private Color	droNormalColor	= new Color(0xBDFFBE);
	private Color	droEditingColor	= new Color(0xF0F0A1);
	private Color	droWarningColor	= new Color(0xFF5C5C);
	private Color	droSavedColor	= new Color(0x90cce0);

	private JogControlsPanel	jogControlsPanel;
	private JDialog				jogControlsWindow;

	private volatile double savedX = Double.NaN, savedY = Double.NaN, savedZ = Double.NaN, savedC = Double.NaN;

	private FocusTraversalPolicy focusPolicy = new FocusTraversalPolicy()
	{
		@Override
		public Component getComponentAfter(Container aContainer, Component aComponent)
		{
			return MachineControlsPanel.this.sliderIncrements;
		}

		@Override
		public Component getComponentBefore(Container aContainer, Component aComponent)
		{
			return MachineControlsPanel.this.sliderIncrements;
		}

		@Override
		public Component getDefaultComponent(Container aContainer)
		{
			return MachineControlsPanel.this.sliderIncrements;
		}

		@Override
		public Component getFirstComponent(Container aContainer)
		{
			return MachineControlsPanel.this.sliderIncrements;
		}

		@Override
		public Component getInitialComponent(Window window)
		{
			return MachineControlsPanel.this.sliderIncrements;
		}

		@Override
		public Component getLastComponent(Container aContainer)
		{
			return MachineControlsPanel.this.sliderIncrements;
		}
	};

	@SuppressWarnings("serial")
	private Action stopMachineAction = new AbstractAction("STOP")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			this.setEnabled(false);
			new Thread(() -> {
				try
				{
					Configuration.get().getMachine().setEnabled(false);
					this.setEnabled(true);
				} catch (Exception t)
				{
					MessageBoxes.errorBox(MachineControlsPanel.this, "Stop Failed", t.getMessage());
					this.setEnabled(true);
				}
			}).start();
		}
	};

	@SuppressWarnings("serial")
	private Action startMachineAction = new AbstractAction("START")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			this.setEnabled(false);
			new Thread(() -> {
				try
				{
					Configuration.get().getMachine().setEnabled(true);
					this.setEnabled(true);
				} catch (Exception t)
				{
					MessageBoxes.errorBox(MachineControlsPanel.this, "Start Failed", t.getMessage());
					this.setEnabled(true);
				}
			}).start();
		}
	};

	@SuppressWarnings("serial")
	public Action homeAction = new AbstractAction("Home", Icons.home)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				MachineControlsPanel.this.selectedNozzle.getHead().home();
			});
		}
	};

	public Action showHideJogControlsWindowAction = new AbstractAction("Show Jog Controls")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (MachineControlsPanel.this.jogControlsWindow.isVisible())
			{
				// Hide
				MachineControlsPanel.this.jogControlsWindow.setVisible(false);
				this.putValue(Action.NAME, "Show Jog Controls");
			} else
			{
				// Show
				MachineControlsPanel.this.jogControlsWindow.setVisible(true);
				MachineControlsPanel.this.jogControlsWindow.pack();
				int x = (int) MachineControlsPanel.this.getLocationOnScreen().getX();
				int y = (int) MachineControlsPanel.this.getLocationOnScreen().getY();
				x += MachineControlsPanel.this.getSize().getWidth() / 2 - MachineControlsPanel.this.jogControlsWindow.getSize().getWidth() / 2;
				y += MachineControlsPanel.this.getSize().getHeight();
				MachineControlsPanel.this.jogControlsWindow.setLocation(x, y);
				this.putValue(Action.NAME, "Hide Jog Controls");
			}
		}
	};

	@SuppressWarnings("serial")
	public Action raiseIncrementAction = new AbstractAction("Raise Jog Increment")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			MachineControlsPanel.this.sliderIncrements.setValue(Math.min(MachineControlsPanel.this.sliderIncrements.getMaximum(), MachineControlsPanel.this.sliderIncrements.getValue() + 1));
		}
	};

	@SuppressWarnings("serial")
	public Action lowerIncrementAction = new AbstractAction("Lower Jog Increment")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			MachineControlsPanel.this.sliderIncrements.setValue(Math.max(MachineControlsPanel.this.sliderIncrements.getMinimum(), MachineControlsPanel.this.sliderIncrements.getValue() - 1));
		}
	};

	@SuppressWarnings("serial")
	public Action targetToolAction = new AbstractAction(null, Icons.centerTool)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				HeadMountable tool = MachineControlsPanel.this.getSelectedTool();
				Camera camera = tool.getHead().getDefaultCamera();
				MovableUtils.moveToLocationAtSafeZ(tool, camera.getLocation(), 1.0);
			});
		}
	};

	@SuppressWarnings("serial")
	public Action targetCameraAction = new AbstractAction(null, Icons.centerCamera)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				HeadMountable tool = MachineControlsPanel.this.getSelectedTool();
				Camera camera = tool.getHead().getDefaultCamera();
				MovableUtils.moveToLocationAtSafeZ(camera, tool.getLocation(), 1.0);
			});
		}
	};

	@SuppressWarnings("serial")
	public Action saveXAction = new AbstractAction(null)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (Double.isNaN(MachineControlsPanel.this.savedX))
			{
				MachineControlsPanel.this.textFieldX.setBackground(MachineControlsPanel.this.droSavedColor);
				MachineControlsPanel.this.savedX = MachineControlsPanel.this.getCurrentLocation().getX();
			} else
			{
				MachineControlsPanel.this.textFieldX.setBackground(MachineControlsPanel.this.droNormalColor);
				MachineControlsPanel.this.savedX = Double.NaN;
			}
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					MachineControlsPanel.this.updateDros();
				}
			});
		}
	};

	@SuppressWarnings("serial")
	public Action saveYAction = new AbstractAction(null)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (Double.isNaN(MachineControlsPanel.this.savedY))
			{
				MachineControlsPanel.this.textFieldY.setBackground(MachineControlsPanel.this.droSavedColor);
				MachineControlsPanel.this.savedY = MachineControlsPanel.this.getCurrentLocation().getY();
			} else
			{
				MachineControlsPanel.this.textFieldY.setBackground(MachineControlsPanel.this.droNormalColor);
				MachineControlsPanel.this.savedY = Double.NaN;
			}
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					MachineControlsPanel.this.updateDros();
				}
			});
		}
	};

	@SuppressWarnings("serial")
	public Action saveZAction = new AbstractAction(null)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (Double.isNaN(MachineControlsPanel.this.savedZ))
			{
				MachineControlsPanel.this.textFieldZ.setBackground(MachineControlsPanel.this.droSavedColor);
				MachineControlsPanel.this.savedZ = MachineControlsPanel.this.getCurrentLocation().getZ();
			} else
			{
				MachineControlsPanel.this.textFieldZ.setBackground(MachineControlsPanel.this.droNormalColor);
				MachineControlsPanel.this.savedZ = Double.NaN;
			}
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					MachineControlsPanel.this.updateDros();
				}
			});
		}
	};

	@SuppressWarnings("serial")
	public Action saveCAction = new AbstractAction(null)
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (Double.isNaN(MachineControlsPanel.this.savedC))
			{
				MachineControlsPanel.this.textFieldC.setBackground(MachineControlsPanel.this.droSavedColor);
				MachineControlsPanel.this.savedC = MachineControlsPanel.this.getCurrentLocation().getRotation();
			} else
			{
				MachineControlsPanel.this.textFieldC.setBackground(MachineControlsPanel.this.droNormalColor);
				MachineControlsPanel.this.savedC = Double.NaN;
			}
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					MachineControlsPanel.this.updateDros();
				}
			});
		}
	};

	private MachineListener machineListener = new MachineListener.Adapter()
	{
		@Override
		public void machineDisabled(Machine machine, String reason)
		{
			MachineControlsPanel.this.btnStartStop.setAction(machine.isEnabled() ? MachineControlsPanel.this.stopMachineAction : MachineControlsPanel.this.startMachineAction);
			MachineControlsPanel.this.btnStartStop.setForeground(machine.isEnabled() ? MachineControlsPanel.this.stopColor : MachineControlsPanel.this.startColor);
			MachineControlsPanel.this.setEnabled(false);
		}

		@Override
		public void machineDisableFailed(Machine machine, String reason)
		{
			MachineControlsPanel.this.btnStartStop.setAction(machine.isEnabled() ? MachineControlsPanel.this.stopMachineAction : MachineControlsPanel.this.startMachineAction);
			MachineControlsPanel.this.btnStartStop.setForeground(machine.isEnabled() ? MachineControlsPanel.this.stopColor : MachineControlsPanel.this.startColor);
		}

		@Override
		public void machineEnabled(Machine machine)
		{
			MachineControlsPanel.this.btnStartStop.setAction(machine.isEnabled() ? MachineControlsPanel.this.stopMachineAction : MachineControlsPanel.this.startMachineAction);
			MachineControlsPanel.this.btnStartStop.setForeground(machine.isEnabled() ? MachineControlsPanel.this.stopColor : MachineControlsPanel.this.startColor);
			MachineControlsPanel.this.setEnabled(true);
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					MachineControlsPanel.this.updateDros();
				}
			});
		}

		@Override
		public void machineEnableFailed(Machine machine, String reason)
		{
			MachineControlsPanel.this.btnStartStop.setAction(machine.isEnabled() ? MachineControlsPanel.this.stopMachineAction : MachineControlsPanel.this.startMachineAction);
			MachineControlsPanel.this.btnStartStop.setForeground(machine.isEnabled() ? MachineControlsPanel.this.stopColor : MachineControlsPanel.this.startColor);
		}

		@Override
		public void machineHeadActivity(Machine machine, Head head)
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					MachineControlsPanel.this.updateDros();
				}
			});
		}
	};

	private ConfigurationListener configurationListener = new ConfigurationListener.Adapter()
	{
		@Override
		public void configurationComplete(Configuration configuration)
		{
			Machine machine = configuration.getMachine();
			if (machine != null)
				machine.removeListener(MachineControlsPanel.this.machineListener);

			for (Head head : machine.getHeads())
				for (Nozzle nozzle : head.getNozzles())
					MachineControlsPanel.this.comboBoxNozzles.addItem(new NozzleItem(nozzle));
			MachineControlsPanel.this.setSelectedNozzle(((NozzleItem) MachineControlsPanel.this.comboBoxNozzles.getItemAt(0)).getNozzle());

			MachineControlsPanel.this.setUnits(configuration.getSystemUnits());
			machine.addListener(MachineControlsPanel.this.machineListener);

			MachineControlsPanel.this.btnStartStop.setAction(machine.isEnabled() ? MachineControlsPanel.this.stopMachineAction : MachineControlsPanel.this.startMachineAction);
			MachineControlsPanel.this.btnStartStop.setForeground(machine.isEnabled() ? MachineControlsPanel.this.stopColor : MachineControlsPanel.this.startColor);

			MachineControlsPanel.this.setEnabled(machine.isEnabled());
		}
	};

	/**
	 * Create the panel.
	 */
	public MachineControlsPanel(Configuration configuration, JFrame frame, CameraPanel cameraPanel)
	{
		this.frame = frame;
		this.cameraPanel = cameraPanel;
		this.configuration = configuration;

		this.jogControlsPanel = new JogControlsPanel(configuration, this, frame);

		this.createUi();

		configuration.addListener(this.configurationListener);

		this.jogControlsWindow = new JDialog(frame, "Jog Controls");
		this.jogControlsWindow.setResizable(false);
		this.jogControlsWindow.getContentPane().setLayout(new BorderLayout());
		this.jogControlsWindow.getContentPane().add(this.jogControlsPanel);
	}

	private void createUi()
	{
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		ButtonGroup buttonGroup = new ButtonGroup();

		JPanel panel = new JPanel();
		this.add(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		this.comboBoxNozzles = new JComboBox();
		this.comboBoxNozzles.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				MachineControlsPanel.this.setSelectedNozzle(((NozzleItem) MachineControlsPanel.this.comboBoxNozzles.getSelectedItem()).getNozzle());
			}
		});
		panel.add(this.comboBoxNozzles, "2, 2, fill, default");

		JPanel panelDrosParent = new JPanel();
		this.add(panelDrosParent);
		panelDrosParent.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JPanel panelDros = new JPanel();
		panelDrosParent.add(panelDros);
		panelDros.setLayout(new BoxLayout(panelDros, BoxLayout.Y_AXIS));

		JPanel panelDrosFirstLine = new JPanel();
		panelDros.add(panelDrosFirstLine);
		panelDrosFirstLine.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel lblX = new JLabel("X");
		lblX.setFont(new Font("Lucida Grande", Font.BOLD, 24));
		panelDrosFirstLine.add(lblX);

		this.textFieldX = new JTextField();
		this.textFieldX.setEditable(false);
		this.textFieldX.setFocusTraversalKeysEnabled(false);
		this.textFieldX.setSelectionColor(this.droEditingColor);
		this.textFieldX.setDisabledTextColor(Color.BLACK);
		this.textFieldX.setBackground(this.droNormalColor);
		this.textFieldX.setFont(new Font("Lucida Grande", Font.BOLD, 24));
		this.textFieldX.setText("0000.0000");
		this.textFieldX.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
					MachineControlsPanel.this.savedX = Double.NaN;
				MachineControlsPanel.this.saveXAction.actionPerformed(null);
			}
		});
		panelDrosFirstLine.add(this.textFieldX);
		this.textFieldX.setColumns(6);

		Component horizontalStrut = Box.createHorizontalStrut(15);
		panelDrosFirstLine.add(horizontalStrut);

		JLabel lblY = new JLabel("Y");
		lblY.setFont(new Font("Lucida Grande", Font.BOLD, 24));
		panelDrosFirstLine.add(lblY);

		this.textFieldY = new JTextField();
		this.textFieldY.setEditable(false);
		this.textFieldY.setFocusTraversalKeysEnabled(false);
		this.textFieldY.setSelectionColor(this.droEditingColor);
		this.textFieldY.setDisabledTextColor(Color.BLACK);
		this.textFieldY.setBackground(this.droNormalColor);
		this.textFieldY.setFont(new Font("Lucida Grande", Font.BOLD, 24));
		this.textFieldY.setText("0000.0000");
		this.textFieldY.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
					MachineControlsPanel.this.savedY = Double.NaN;
				MachineControlsPanel.this.saveYAction.actionPerformed(null);
			}
		});
		panelDrosFirstLine.add(this.textFieldY);
		this.textFieldY.setColumns(6);

		JButton btnTargetTool = new JButton(this.targetToolAction);
		panelDrosFirstLine.add(btnTargetTool);
		btnTargetTool.setToolTipText("Position the tool at the camera's current location.");

		JPanel panelDrosSecondLine = new JPanel();
		panelDros.add(panelDrosSecondLine);
		panelDrosSecondLine.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel lblC = new JLabel("C");
		lblC.setFont(new Font("Lucida Grande", Font.BOLD, 24));
		panelDrosSecondLine.add(lblC);

		this.textFieldC = new JTextField();
		this.textFieldC.setEditable(false);
		this.textFieldC.setFocusTraversalKeysEnabled(false);
		this.textFieldC.setSelectionColor(this.droEditingColor);
		this.textFieldC.setDisabledTextColor(Color.BLACK);
		this.textFieldC.setBackground(this.droNormalColor);
		this.textFieldC.setText("0000.0000");
		this.textFieldC.setFont(new Font("Lucida Grande", Font.BOLD, 24));
		this.textFieldC.setColumns(6);
		this.textFieldC.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
					MachineControlsPanel.this.savedC = Double.NaN;
				MachineControlsPanel.this.saveCAction.actionPerformed(null);
			}
		});
		panelDrosSecondLine.add(this.textFieldC);

		Component horizontalStrut_1 = Box.createHorizontalStrut(15);
		panelDrosSecondLine.add(horizontalStrut_1);

		JLabel lblZ = new JLabel("Z");
		lblZ.setFont(new Font("Lucida Grande", Font.BOLD, 24));
		panelDrosSecondLine.add(lblZ);

		this.textFieldZ = new JTextField();
		this.textFieldZ.setEditable(false);
		this.textFieldZ.setFocusTraversalKeysEnabled(false);
		this.textFieldZ.setSelectionColor(this.droEditingColor);
		this.textFieldZ.setDisabledTextColor(Color.BLACK);
		this.textFieldZ.setBackground(this.droNormalColor);
		this.textFieldZ.setText("0000.0000");
		this.textFieldZ.setFont(new Font("Lucida Grande", Font.BOLD, 24));
		this.textFieldZ.setColumns(6);
		this.textFieldZ.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
					MachineControlsPanel.this.savedZ = Double.NaN;
				MachineControlsPanel.this.saveZAction.actionPerformed(null);
			}
		});
		panelDrosSecondLine.add(this.textFieldZ);

		JButton btnTargetCamera = new JButton(this.targetCameraAction);
		panelDrosSecondLine.add(btnTargetCamera);
		btnTargetCamera.setToolTipText("Position the camera at the tool's current location.");

		JPanel panelIncrements = new JPanel();
		this.add(panelIncrements);

		this.sliderIncrements = new JSlider();
		panelIncrements.add(this.sliderIncrements);
		this.sliderIncrements.setMajorTickSpacing(1);
		this.sliderIncrements.setValue(1);
		this.sliderIncrements.setSnapToTicks(true);
		this.sliderIncrements.setPaintLabels(true);
		this.sliderIncrements.setPaintTicks(true);
		this.sliderIncrements.setMinimum(1);
		this.sliderIncrements.setMaximum(5);

		JPanel panelStartStop = new JPanel();
		this.add(panelStartStop);
		panelStartStop.setLayout(new BorderLayout(0, 0));

		this.btnStartStop = new JButton(this.startMachineAction);
		this.btnStartStop.setFocusable(true);
		this.btnStartStop.setForeground(this.startColor);
		panelStartStop.add(this.btnStartStop);
		this.btnStartStop.setFont(new Font("Lucida Grande", Font.BOLD, 48));
		this.btnStartStop.setPreferredSize(new Dimension(160, 70));

		this.setFocusTraversalPolicy(this.focusPolicy);
		this.setFocusTraversalPolicyProvider(true);
	}

	public Location getCurrentLocation()
	{
		if (this.selectedNozzle == null)
			return null;

		Location l = this.selectedNozzle.getLocation();
		l = l.convertToUnits(this.configuration.getSystemUnits());

		return l;
	}

	public JogControlsPanel getJogControlsPanel()
	{
		return this.jogControlsPanel;
	}

	public double getJogIncrement()
	{
		if (this.configuration.getSystemUnits() == LengthUnit.Millimeters)
			return 0.01 * Math.pow(10, this.sliderIncrements.getValue() - 1);
		else if (this.configuration.getSystemUnits() == LengthUnit.Inches)
			return 0.001 * Math.pow(10, this.sliderIncrements.getValue() - 1);
		else
			throw new Error("getJogIncrement() not implemented for " + this.configuration.getSystemUnits());
	}

	public Nozzle getSelectedNozzle()
	{
		return this.selectedNozzle;
	}

	public PasteDispenser getSelectedPasteDispenser()
	{
		try
		{
			// TODO: We don't actually have a way to select a dispenser yet, so
			// until we do we just return the first one.
			return Configuration.get().getMachine().getDefaultHead().getDefaultPasteDispenser();
		} catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Returns the selected Nozzle or PasteDispenser depending on which type of
	 * Job is selected.
	 * 
	 * @return
	 */
	public HeadMountable getSelectedTool()
	{
		JobProcessor.Type jobProcessorType = MainFrame.jobPanel.getSelectedJobProcessorType();
		if (jobProcessorType == JobProcessor.Type.PickAndPlace)
			return this.getSelectedNozzle();
		else if (jobProcessorType == JobProcessor.Type.SolderPaste)
			return this.getSelectedPasteDispenser();
		else
			throw new Error("Unknown tool type: " + jobProcessorType);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		this.homeAction.setEnabled(enabled);
		this.jogControlsPanel.setEnabled(enabled);
		this.targetCameraAction.setEnabled(enabled);
		this.targetToolAction.setEnabled(enabled);
	}

	public void setSelectedNozzle(Nozzle nozzle)
	{
		this.selectedNozzle = nozzle;
		this.comboBoxNozzles.setSelectedItem(this.selectedNozzle);
		this.updateDros();
	}

	private void setUnits(LengthUnit units)
	{
		if (units == LengthUnit.Millimeters)
		{
			Hashtable<Integer, JLabel> incrementsLabels = new Hashtable<>();
			incrementsLabels.put(1, new JLabel("0.01"));
			incrementsLabels.put(2, new JLabel("0.1"));
			incrementsLabels.put(3, new JLabel("1.0"));
			incrementsLabels.put(4, new JLabel("10"));
			incrementsLabels.put(5, new JLabel("100"));
			this.sliderIncrements.setLabelTable(incrementsLabels);
		} else if (units == LengthUnit.Inches)
		{
			Hashtable<Integer, JLabel> incrementsLabels = new Hashtable<>();
			incrementsLabels.put(1, new JLabel("0.001"));
			incrementsLabels.put(2, new JLabel("0.01"));
			incrementsLabels.put(3, new JLabel("0.1"));
			incrementsLabels.put(4, new JLabel("1.0"));
			incrementsLabels.put(5, new JLabel("10.0"));
			this.sliderIncrements.setLabelTable(incrementsLabels);
		} else
			throw new Error("setUnits() not implemented for " + units);
		this.updateDros();
	}

	public void updateDros()
	{
		Location l = this.getCurrentLocation();
		if (l == null)
			return;

		double x, y, z, c;

		x = l.getX();
		y = l.getY();
		z = l.getZ();
		c = l.getRotation();

		double savedX = this.savedX;
		if (!Double.isNaN(savedX))
			x -= savedX;

		double savedY = this.savedY;
		if (!Double.isNaN(savedY))
			y -= savedY;

		double savedZ = this.savedZ;
		if (!Double.isNaN(savedZ))
			z -= savedZ;

		double savedC = this.savedC;
		if (!Double.isNaN(savedC))
			c -= savedC;

		this.textFieldX.setText(String.format(Locale.US, this.configuration.getLengthDisplayFormat(), x));
		this.textFieldY.setText(String.format(Locale.US, this.configuration.getLengthDisplayFormat(), y));
		this.textFieldZ.setText(String.format(Locale.US, this.configuration.getLengthDisplayFormat(), z));
		this.textFieldC.setText(String.format(Locale.US, this.configuration.getLengthDisplayFormat(), c));
	}
}
