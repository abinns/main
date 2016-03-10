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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.openpnp.ConfigurationListener;
import org.openpnp.JobProcessorListener;
import org.openpnp.gui.components.CameraPanel;
import org.openpnp.gui.importer.BoardImporter;
import org.openpnp.gui.importer.EagleBoardImporter;
import org.openpnp.gui.importer.EagleMountsmdUlpImporter;
import org.openpnp.gui.importer.KicadPosImporter;
import org.openpnp.gui.importer.NamedCSVImporter;
import org.openpnp.gui.importer.SolderPasteGerberImporter;
import org.openpnp.gui.support.HeadCellValue;
import org.openpnp.gui.support.LengthCellValue;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.OSXAdapter;
import org.openpnp.model.Configuration;
import org.openpnp.model.LengthUnit;
import org.openpnp.spi.JobProcessor;

/**
 * The main window of the application.
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame
{
	/**
	 * 
	 */
	private static final long	serialVersionUID			= 1L;
	private static final String	PREF_WINDOW_X				= "MainFrame.windowX";
	private static final int	PREF_WINDOW_X_DEF			= 0;
	private static final String	PREF_WINDOW_Y				= "MainFrame.windowY";
	private static final int	PREF_WINDOW_Y_DEF			= 0;
	private static final String	PREF_WINDOW_WIDTH			= "MainFrame.windowWidth";
	private static final int	PREF_WINDOW_WIDTH_DEF		= 1024;
	private static final String	PREF_WINDOW_HEIGHT			= "MainFrame.windowHeight";
	private static final int	PREF_WINDOW_HEIGHT_DEF		= 768;
	private static final String	PREF_DIVIDER_POSITION		= "MainFrame.dividerPosition";
	private static final int	PREF_DIVIDER_POSITION_DEF	= -1;

	// TODO: Really should switch to some kind of DI model, but this will do
	// for now.
	public static MainFrame mainFrame;

	public static MachineControlsPanel	machineControlsPanel;
	public static PartsPanel			partsPanel;
	public static PackagesPanel			packagesPanel;
	public static FeedersPanel			feedersPanel;
	public static JobPanel				jobPanel;
	public static CamerasPanel			camerasPanel;
	public static CameraPanel			cameraPanel;
	public static MachineSetupPanel		machineSetupPanel;
	/*
	 * TODO define accelerators and mnemonics
	 * openJobMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
	 * Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	 */
	private final Configuration			configuration;

	private JPanel			contentPane;
	private JLabel			lblStatus;
	private JTabbedPane		panelBottom;
	private JSplitPane		splitPaneTopBottom;
	private TitledBorder	panelInstructionsBorder;

	private Preferences prefs = Preferences.userNodeForPackage(MainFrame.class);

	private ActionListener	instructionsCancelActionListener;
	private ActionListener	instructionsProceedActionListener;

	private JMenu mnImport;

	private JobProcessorListener jobProcessorListener = new JobProcessorListener.Adapter()
	{
		@Override
		public void detailedStatusUpdated(String status)
		{
			MainFrame.this.lblStatus.setText(status);
		}
	};

	private ComponentListener componentListener = new ComponentAdapter()
	{
		@Override
		public void componentMoved(ComponentEvent e)
		{
			MainFrame.this.prefs.putInt(MainFrame.PREF_WINDOW_X, MainFrame.this.getLocation().x);
			MainFrame.this.prefs.putInt(MainFrame.PREF_WINDOW_Y, MainFrame.this.getLocation().y);
		}

		@Override
		public void componentResized(ComponentEvent e)
		{
			MainFrame.this.prefs.putInt(MainFrame.PREF_WINDOW_WIDTH, MainFrame.this.getSize().width);
			MainFrame.this.prefs.putInt(MainFrame.PREF_WINDOW_HEIGHT, MainFrame.this.getSize().height);
		}
	};

	private Action inchesUnitSelected = new AbstractAction(LengthUnit.Inches.name())
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			MainFrame.this.configuration.setSystemUnits(LengthUnit.Inches);
			MessageBoxes.errorBox(MainFrame.this, "Notice", "Please restart OpenPnP for the changes to take effect.");
		}
	};

	private Action millimetersUnitSelected = new AbstractAction(LengthUnit.Millimeters.name())
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			MainFrame.this.configuration.setSystemUnits(LengthUnit.Millimeters);
			MessageBoxes.errorBox(MainFrame.this, "Notice", "Please restart OpenPnP for the changes to take effect.");
		}
	};

	private Action quitAction = new AbstractAction("Exit")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			MainFrame.this.quit();
		}
	};

	private Action aboutAction = new AbstractAction("About")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			MainFrame.this.about();
		}
	};

	private JPanel panelInstructions;

	private JPanel panelInstructionActions;

	private JPanel panel_1;

	private JButton btnInstructionsNext;

	private JButton btnInstructionsCancel;

	private JTextPane lblInstructions;

	private JPanel panel_2;

	public MainFrame(Configuration configuration)
	{
		MainFrame.mainFrame = this;
		this.configuration = configuration;
		LengthCellValue.setConfiguration(configuration);
		HeadCellValue.setConfiguration(configuration);

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// Get handlers for Mac application menu in place.
		boolean macOsXMenus = this.registerForMacOSXEvents();

		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				MainFrame.this.quit();
			}
		});

		if (this.prefs.getInt(MainFrame.PREF_WINDOW_WIDTH, 50) < 50)
			this.prefs.putInt(MainFrame.PREF_WINDOW_WIDTH, MainFrame.PREF_WINDOW_WIDTH_DEF);

		if (this.prefs.getInt(MainFrame.PREF_WINDOW_HEIGHT, 50) < 50)
			this.prefs.putInt(MainFrame.PREF_WINDOW_HEIGHT, MainFrame.PREF_WINDOW_HEIGHT_DEF);

		this.setBounds(this.prefs.getInt(MainFrame.PREF_WINDOW_X, MainFrame.PREF_WINDOW_X_DEF), this.prefs.getInt(MainFrame.PREF_WINDOW_Y, MainFrame.PREF_WINDOW_Y_DEF),
				this.prefs.getInt(MainFrame.PREF_WINDOW_WIDTH, MainFrame.PREF_WINDOW_WIDTH_DEF), this.prefs.getInt(MainFrame.PREF_WINDOW_HEIGHT, MainFrame.PREF_WINDOW_HEIGHT_DEF));

		MainFrame.cameraPanel = new CameraPanel();
		MainFrame.machineControlsPanel = new MachineControlsPanel(configuration, this, MainFrame.cameraPanel);
		MainFrame.jobPanel = new JobPanel(configuration, this, MainFrame.machineControlsPanel);
		MainFrame.partsPanel = new PartsPanel(configuration, this);
		MainFrame.packagesPanel = new PackagesPanel(configuration, this);
		MainFrame.feedersPanel = new FeedersPanel(configuration, this);
		MainFrame.camerasPanel = new CamerasPanel(this, configuration);
		MainFrame.machineSetupPanel = new MachineSetupPanel();

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		// File
		//////////////////////////////////////////////////////////////////////
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mnFile.add(new JMenuItem(MainFrame.jobPanel.newJobAction));
		mnFile.add(new JMenuItem(MainFrame.jobPanel.openJobAction));

		mnFile.add(MainFrame.jobPanel.mnOpenRecent);

		mnFile.addSeparator();
		mnFile.add(new JMenuItem(MainFrame.jobPanel.saveJobAction));
		mnFile.add(new JMenuItem(MainFrame.jobPanel.saveJobAsAction));

		// File -> Import
		//////////////////////////////////////////////////////////////////////
		mnFile.addSeparator();
		this.mnImport = new JMenu("Import Board");
		mnFile.add(this.mnImport);

		if (!macOsXMenus)
		{
			mnFile.addSeparator();
			mnFile.add(new JMenuItem(this.quitAction));
		}

		// Edit
		//////////////////////////////////////////////////////////////////////
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		mnEdit.add(new JMenuItem(MainFrame.jobPanel.newBoardAction));
		mnEdit.add(new JMenuItem(MainFrame.jobPanel.addBoardAction));
		mnEdit.add(new JMenuItem(MainFrame.jobPanel.removeBoardAction));
		mnEdit.addSeparator();
		mnEdit.add(new JMenuItem(MainFrame.jobPanel.captureToolBoardLocationAction));

		// View
		//////////////////////////////////////////////////////////////////////
		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);

		ButtonGroup buttonGroup = new ButtonGroup();

		JMenu mnUnits = new JMenu("System Units");
		mnView.add(mnUnits);

		JMenuItem menuItem;
		menuItem = new JCheckBoxMenuItem(this.inchesUnitSelected);
		buttonGroup.add(menuItem);
		if (configuration.getSystemUnits() == LengthUnit.Inches)
			menuItem.setSelected(true);
		mnUnits.add(menuItem);
		menuItem = new JCheckBoxMenuItem(this.millimetersUnitSelected);
		buttonGroup.add(menuItem);
		if (configuration.getSystemUnits() == LengthUnit.Millimeters)
			menuItem.setSelected(true);
		mnUnits.add(menuItem);

		// Job Control
		//////////////////////////////////////////////////////////////////////
		JMenu mnJob = new JMenu("Job Control");
		menuBar.add(mnJob);

		mnJob.add(new JMenuItem(MainFrame.jobPanel.startPauseResumeJobAction));
		mnJob.add(new JMenuItem(MainFrame.jobPanel.stepJobAction));
		mnJob.add(new JMenuItem(MainFrame.jobPanel.stopJobAction));

		// Machine
		//////////////////////////////////////////////////////////////////////
		JMenu mnCommands = new JMenu("Machine");
		menuBar.add(mnCommands);

		mnCommands.add(new JMenuItem(MainFrame.machineControlsPanel.homeAction));
		mnCommands.add(new JMenuItem(MainFrame.machineControlsPanel.showHideJogControlsWindowAction));

		// Help
		/////////////////////////////////////////////////////////////////////
		if (!macOsXMenus)
		{
			JMenu mnHelp = new JMenu("Help");
			menuBar.add(mnHelp);

			mnHelp.add(new JMenuItem(this.aboutAction));
		}

		this.contentPane = new JPanel();
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setContentPane(this.contentPane);
		this.contentPane.setLayout(new BorderLayout(0, 0));

		this.splitPaneTopBottom = new JSplitPane();
		this.splitPaneTopBottom.setBorder(null);
		this.splitPaneTopBottom.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.splitPaneTopBottom.setContinuousLayout(true);
		this.contentPane.add(this.splitPaneTopBottom, BorderLayout.CENTER);

		JPanel panelTop = new JPanel();
		this.splitPaneTopBottom.setLeftComponent(panelTop);
		panelTop.setLayout(new BorderLayout(0, 0));

		JPanel panelLeftColumn = new JPanel();
		panelTop.add(panelLeftColumn, BorderLayout.WEST);
		FlowLayout flowLayout = (FlowLayout) panelLeftColumn.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);

		JPanel panel = new JPanel();
		panelLeftColumn.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		MainFrame.machineControlsPanel.setBorder(new TitledBorder(null, "Machine Controls", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		panel.add(MainFrame.machineControlsPanel);

		// Add global hotkeys for the arrow keys
		final Map<KeyStroke, Action> hotkeyActionMap = new HashMap<>();

		int mask = InputEvent.CTRL_DOWN_MASK;

		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, mask), MainFrame.machineControlsPanel.getJogControlsPanel().yPlusAction);
		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, mask), MainFrame.machineControlsPanel.getJogControlsPanel().yMinusAction);
		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, mask), MainFrame.machineControlsPanel.getJogControlsPanel().xMinusAction);
		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, mask), MainFrame.machineControlsPanel.getJogControlsPanel().xPlusAction);
		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_QUOTE, mask), MainFrame.machineControlsPanel.getJogControlsPanel().zPlusAction);
		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, mask), MainFrame.machineControlsPanel.getJogControlsPanel().zMinusAction);
		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, mask), MainFrame.machineControlsPanel.getJogControlsPanel().cPlusAction);
		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, mask), MainFrame.machineControlsPanel.getJogControlsPanel().cMinusAction);
		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, mask), MainFrame.machineControlsPanel.lowerIncrementAction);
		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, mask), MainFrame.machineControlsPanel.raiseIncrementAction);
		hotkeyActionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, mask), MainFrame.machineControlsPanel.showHideJogControlsWindowAction);

		Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueue()
		{
			@Override
			protected void dispatchEvent(AWTEvent event)
			{
				if (event instanceof KeyEvent)
				{
					KeyStroke ks = KeyStroke.getKeyStrokeForEvent((KeyEvent) event);
					Action action = hotkeyActionMap.get(ks);
					if (action != null && action.isEnabled())
					{
						action.actionPerformed(null);
						return;
					}
				}
				super.dispatchEvent(event);
			}
		});

		JPanel panelCameraAndInstructions = new JPanel(new BorderLayout());
		panelCameraAndInstructions.add(MainFrame.cameraPanel, BorderLayout.CENTER);

		panelTop.add(panelCameraAndInstructions, BorderLayout.CENTER);
		MainFrame.cameraPanel.setBorder(new TitledBorder(null, "Cameras", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		this.panelInstructions = new JPanel();
		this.panelInstructions.setVisible(false);
		this.panelInstructions.setBorder(this.panelInstructionsBorder = new TitledBorder(null, "Instructions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelCameraAndInstructions.add(this.panelInstructions, BorderLayout.SOUTH);
		this.panelInstructions.setLayout(new BorderLayout(0, 0));

		this.panelInstructionActions = new JPanel();
		this.panelInstructionActions.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		this.panelInstructions.add(this.panelInstructionActions, BorderLayout.EAST);
		this.panelInstructionActions.setLayout(new BorderLayout(0, 0));

		this.panel_2 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) this.panel_2.getLayout();
		flowLayout_2.setVgap(0);
		flowLayout_2.setHgap(0);
		this.panelInstructionActions.add(this.panel_2, BorderLayout.SOUTH);

		this.btnInstructionsCancel = new JButton("Cancel");
		this.btnInstructionsCancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (MainFrame.this.instructionsCancelActionListener != null)
					MainFrame.this.instructionsCancelActionListener.actionPerformed(arg0);
			}
		});
		this.panel_2.add(this.btnInstructionsCancel);

		this.btnInstructionsNext = new JButton("Next");
		this.btnInstructionsNext.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (MainFrame.this.instructionsProceedActionListener != null)
					MainFrame.this.instructionsProceedActionListener.actionPerformed(arg0);
			}
		});
		this.panel_2.add(this.btnInstructionsNext);

		this.panel_1 = new JPanel();
		this.panelInstructions.add(this.panel_1, BorderLayout.CENTER);
		this.panel_1.setLayout(new BorderLayout(0, 0));

		this.lblInstructions = new JTextPane();
		this.lblInstructions.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		this.lblInstructions.setBackground(UIManager.getColor("Panel.background"));
		this.lblInstructions.setContentType("text/html");
		this.lblInstructions.setEditable(false);
		this.panel_1.add(this.lblInstructions);

		this.panelBottom = new JTabbedPane(SwingConstants.TOP);
		this.splitPaneTopBottom.setRightComponent(this.panelBottom);

		this.lblStatus = new JLabel(" ");
		this.lblStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		this.contentPane.add(this.lblStatus, BorderLayout.SOUTH);

		this.splitPaneTopBottom.setDividerLocation(this.prefs.getInt(MainFrame.PREF_DIVIDER_POSITION, MainFrame.PREF_DIVIDER_POSITION_DEF));
		this.splitPaneTopBottom.addPropertyChangeListener("dividerLocation", new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				MainFrame.this.prefs.putInt(MainFrame.PREF_DIVIDER_POSITION, MainFrame.this.splitPaneTopBottom.getDividerLocation());
			}
		});

		this.panelBottom.addTab("Job", null, MainFrame.jobPanel, null);
		this.panelBottom.addTab("Parts", null, MainFrame.partsPanel, null);
		this.panelBottom.addTab("Packages", null, MainFrame.packagesPanel, null);
		this.panelBottom.addTab("Feeders", null, MainFrame.feedersPanel, null);
		this.panelBottom.addTab("Cameras", null, MainFrame.camerasPanel, null);
		this.panelBottom.addTab("Machine Setup", null, MainFrame.machineSetupPanel, null);

		this.registerBoardImporters();

		this.addComponentListener(this.componentListener);

		try
		{
			configuration.load();
		} catch (Exception e)
		{
			e.printStackTrace();
			MessageBoxes.errorBox(this, "Configuration Load Error",
					"There was a problem loading the configuration. The reason was:<br/><br/>" + e.getMessage() + "<br/><br/>"
							+ "Please check your configuration files and try again. They are located at: " + configuration.getConfigurationDirectory().getAbsolutePath() + "<br/><br/>"
							+ "If you would like to start with a fresh configuration, just delete the entire directory at the location above.<br/><br/>" + "OpenPnP will now exit.");
			System.exit(1);
		}

		configuration.addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationComplete(Configuration configuration) throws Exception
			{
				for (JobProcessor jobProcessor : configuration.getMachine().getJobProcessors().values())
					jobProcessor.addListener(MainFrame.this.jobProcessorListener);
			}
		});
	}

	public void about()
	{
		AboutDialog dialog = new AboutDialog(this);
		dialog.setSize(350, 350);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	public void hideInstructions()
	{
		this.panelInstructions.setVisible(false);
		this.doLayout();
	}

	public boolean quit()
	{
		try
		{
			Preferences.userRoot().flush();
		} catch (Exception e)
		{

		}

		// Save the configuration
		try
		{
			this.configuration.save();
		} catch (Exception e)
		{
			String message = "There was a problem saving the configuration. The reason was:\n\n" + e.getMessage() + "\n\nDo you want to quit without saving?";
			message = message.replaceAll("\n", "<br/>");
			message = message.replaceAll("\r", "");
			message = "<html><body width=\"400\">" + message + "</body></html>";
			int result = JOptionPane.showConfirmDialog(this, message, "Configuration Save Error", JOptionPane.YES_NO_OPTION);
			if (result != JOptionPane.YES_OPTION)
				return false;
		}
		if (!MainFrame.jobPanel.checkForModifications())
			return false;
		// Attempt to stop the machine on quit
		try
		{
			this.configuration.getMachine().setEnabled(false);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		// Attempt to stop the machine on quit
		try
		{
			this.configuration.getMachine().close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		System.exit(0);
		return true;
	}

	/**
	 * Register a BoardImporter with the system, causing it to gain a menu
	 * location in the File->Import menu.
	 * 
	 * @param importer
	 */
	public void registerBoardImporter(final Class<? extends BoardImporter> boardImporterClass)
	{
		final BoardImporter boardImporter;
		try
		{
			boardImporter = boardImporterClass.newInstance();
		} catch (Exception e)
		{
			throw new Error(e);
		}
		JMenuItem menuItem = new JMenuItem(new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				this.putValue(Action.NAME, boardImporter.getImporterName());
				this.putValue(Action.SHORT_DESCRIPTION, boardImporter.getImporterDescription());
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				MainFrame.jobPanel.importBoard(boardImporterClass);
			}
		});
		this.mnImport.add(menuItem);
	}

	private void registerBoardImporters()
	{
		this.registerBoardImporter(EagleBoardImporter.class);
		this.registerBoardImporter(EagleMountsmdUlpImporter.class);
		this.registerBoardImporter(KicadPosImporter.class);
		this.registerBoardImporter(NamedCSVImporter.class);
		this.registerBoardImporter(SolderPasteGerberImporter.class);
	}

	public boolean registerForMacOSXEvents()
	{
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
		{
			try
			{
				// Generate and register the OSXAdapter, passing it a hash of
				// all the methods we wish to
				// use as delegates for various
				// com.apple.eawt.ApplicationListener methods
				OSXAdapter.setQuitHandler(this, this.getClass().getDeclaredMethod("quit", (Class[]) null));
				OSXAdapter.setAboutHandler(this, this.getClass().getDeclaredMethod("about", (Class[]) null));
				// OSXAdapter.setPreferencesHandler(this, getClass()
				// .getDeclaredMethod("preferences", (Class[]) null));
				// OSXAdapter.setFileHandler(
				// this,
				// getClass().getDeclaredMethod("loadImageFile",
				// new Class[] { String.class }));
			} catch (Exception e)
			{
				System.err.println("Error while loading the OSXAdapter:");
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public void showInstructions(String title, String instructions, boolean showCancelButton, boolean showProceedButton, String proceedButtonText, ActionListener cancelActionListener,
			ActionListener proceedActionListener)
	{
		this.panelInstructionsBorder.setTitle(title);
		this.lblInstructions.setText(instructions);
		this.btnInstructionsCancel.setVisible(showCancelButton);
		this.btnInstructionsNext.setVisible(showProceedButton);
		this.btnInstructionsNext.setText(proceedButtonText);
		this.instructionsCancelActionListener = cancelActionListener;
		this.instructionsProceedActionListener = proceedActionListener;
		this.panelInstructions.setVisible(true);
		this.doLayout();
		this.panelInstructions.repaint();
	}

	public void showTab(String title)
	{
		int index = this.panelBottom.indexOfTab(title);
		this.panelBottom.setSelectedIndex(index);
	}
}
