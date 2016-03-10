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
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openpnp.ConfigurationListener;
import org.openpnp.JobProcessorDelegate;
import org.openpnp.JobProcessorListener;
import org.openpnp.gui.components.AutoSelectTextTable;
import org.openpnp.gui.importer.BoardImporter;
import org.openpnp.gui.processes.TwoPlacementBoardLocationProcess;
import org.openpnp.gui.support.ActionGroup;
import org.openpnp.gui.support.Helpers;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.tablemodel.BoardLocationsTableModel;
import org.openpnp.model.Board;
import org.openpnp.model.Board.Side;
import org.openpnp.model.BoardLocation;
import org.openpnp.model.BoardPad;
import org.openpnp.model.Configuration;
import org.openpnp.model.Job;
import org.openpnp.model.Location;
import org.openpnp.model.Part;
import org.openpnp.model.Placement;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Feeder;
import org.openpnp.spi.HeadMountable;
import org.openpnp.spi.JobProcessor;
import org.openpnp.spi.JobProcessor.JobError;
import org.openpnp.spi.JobProcessor.JobState;
import org.openpnp.spi.JobProcessor.PickRetryAction;
import org.openpnp.spi.Machine;
import org.openpnp.spi.MachineListener;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;
import org.openpnp.vision.FiducialLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class JobPanel extends JPanel
{
	public class OpenRecentJobAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 1L;
		private final File			file;

		public OpenRecentJobAction(File file)
		{
			this.file = file;
			this.putValue(Action.NAME, file.getName());
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (!JobPanel.this.checkForModifications())
				return;
			try
			{
				Job job = JobPanel.this.configuration.loadJob(this.file);
				JobPanel.this.jobProcessor.load(job);
				JobPanel.this.addRecentJob(this.file);
			} catch (Exception e)
			{
				e.printStackTrace();
				MessageBoxes.errorBox(JobPanel.this.frame, "Job Load Error", e.getMessage());
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final Logger	logger						= LoggerFactory.getLogger(JobPanel.class);
	private static final String	PREF_DIVIDER_POSITION		= "JobPanel.dividerPosition";
	private static final int	PREF_DIVIDER_POSITION_DEF	= -1;

	private static final String	UNTITLED_JOB_FILENAME	= "Untitled.job.xml";
	private static final String	PREF_RECENT_FILES		= "JobPanel.recentFiles";

	private static final int PREF_RECENT_FILES_MAX = 10;

	final private Configuration	configuration;
	final private MainFrame		frame;

	final private MachineControlsPanel machineControlsPanel;

	private JobProcessor				jobProcessor;
	private BoardLocationsTableModel	boardLocationsTableModel;
	private JTable						boardLocationsTable;

	private JSplitPane	splitPane;
	private ActionGroup	jobSaveActionGroup;

	private ActionGroup boardLocationSelectionActionGroup;

	private Preferences prefs = Preferences.userNodeForPackage(JobPanel.class);

	public JMenu mnOpenRecent;

	private List<File>					recentJobs	= new ArrayList<>();
	private final JobPlacementsPanel	jobPlacementsPanel;

	private final JobPastePanel jobPastePanel;

	private JTabbedPane tabbedPane;

	public final Action openJobAction = new AbstractAction("Open Job...")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (!JobPanel.this.checkForModifications())
				return;
			FileDialog fileDialog = new FileDialog(JobPanel.this.frame);
			fileDialog.setFilenameFilter(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return name.toLowerCase().endsWith(".job.xml");
				}
			});
			fileDialog.setVisible(true);
			try
			{
				if (fileDialog.getFile() == null)
					return;
				File file = new File(new File(fileDialog.getDirectory()), fileDialog.getFile());
				Job job = JobPanel.this.configuration.loadJob(file);
				JobPanel.this.jobProcessor.load(job);
				JobPanel.this.addRecentJob(file);
			} catch (Exception e)
			{
				e.printStackTrace();
				MessageBoxes.errorBox(JobPanel.this.frame, "Job Load Error", e.getMessage());
			}
		}
	};

	public final Action newJobAction = new AbstractAction("New Job")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (!JobPanel.this.checkForModifications())
				return;
			JobPanel.this.jobProcessor.load(new Job());
		}
	};

	public final Action saveJobAction = new AbstractAction("Save Job")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			JobPanel.this.saveJob();
		}
	};

	public final Action saveJobAsAction = new AbstractAction("Save Job As...")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			JobPanel.this.saveJobAs();
		}
	};

	public final Action startPauseResumeJobAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.start);
			this.putValue(Action.NAME, "Start");
			this.putValue(Action.SHORT_DESCRIPTION, "Start processing the job.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			JobState state = JobPanel.this.jobProcessor.getState();
			if (state == JobState.Stopped)
				try
				{
					JobPanel.this.jobProcessor.start();
				} catch (Exception e)
				{
					MessageBoxes.errorBox(JobPanel.this.frame, "Job Start Error", e.getMessage());
				}
			else if (state == JobState.Paused)
				JobPanel.this.jobProcessor.resume();
			else if (state == JobState.Running)
				JobPanel.this.jobProcessor.pause();
		}
	};

	public final Action stepJobAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.step);
			this.putValue(Action.NAME, "Step");
			this.putValue(Action.SHORT_DESCRIPTION, "Process one step of the job and pause.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			try
			{
				JobPanel.this.jobProcessor.step();
			} catch (Exception e)
			{
				MessageBoxes.errorBox(JobPanel.this.frame, "Job Step Failed", e.getMessage());
			}
		}
	};

	public final Action stopJobAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.stop);
			this.putValue(Action.NAME, "Stop");
			this.putValue(Action.SHORT_DESCRIPTION, "Stop processing the job.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			JobPanel.this.jobProcessor.stop();
		}
	};

	public final Action newBoardAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.neww);
			this.putValue(Action.NAME, "New Board...");
			this.putValue(Action.SHORT_DESCRIPTION, "Create a new board and add it to the job.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			FileDialog fileDialog = new FileDialog(JobPanel.this.frame, "Save New Board As...", FileDialog.SAVE);
			fileDialog.setFilenameFilter(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return name.toLowerCase().endsWith(".board.xml");
				}
			});
			fileDialog.setVisible(true);
			try
			{
				String filename = fileDialog.getFile();
				if (filename == null)
					return;
				if (!filename.toLowerCase().endsWith(".board.xml"))
					filename = filename + ".board.xml";
				File file = new File(new File(fileDialog.getDirectory()), filename);

				Board board = JobPanel.this.configuration.getBoard(file);
				BoardLocation boardLocation = new BoardLocation(board);
				JobPanel.this.jobProcessor.getJob().addBoardLocation(boardLocation);
				JobPanel.this.boardLocationsTableModel.fireTableDataChanged();

				Helpers.selectLastTableRow(JobPanel.this.boardLocationsTable);
			} catch (Exception e)
			{
				e.printStackTrace();
				MessageBoxes.errorBox(JobPanel.this.frame, "Unable to create new board", e.getMessage());
			}
		}
	};

	public final Action addBoardAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.add);
			this.putValue(Action.NAME, "Add Board...");
			this.putValue(Action.SHORT_DESCRIPTION, "Add an existing board to the job.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			FileDialog fileDialog = new FileDialog(JobPanel.this.frame);
			fileDialog.setFilenameFilter(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return name.toLowerCase().endsWith(".board.xml");
				}
			});
			fileDialog.setVisible(true);
			try
			{
				if (fileDialog.getFile() == null)
					return;
				File file = new File(new File(fileDialog.getDirectory()), fileDialog.getFile());

				Board board = JobPanel.this.configuration.getBoard(file);
				BoardLocation boardLocation = new BoardLocation(board);
				JobPanel.this.jobProcessor.getJob().addBoardLocation(boardLocation);
				// TODO: Move to a list property listener.
				JobPanel.this.boardLocationsTableModel.fireTableDataChanged();

				Helpers.selectLastTableRow(JobPanel.this.boardLocationsTable);
			} catch (Exception e)
			{
				e.printStackTrace();
				MessageBoxes.errorBox(JobPanel.this.frame, "Board load failed", e.getMessage());
			}
		}
	};

	public final Action removeBoardAction = new AbstractAction("Remove Board")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.delete);
			this.putValue(Action.NAME, "Remove Board");
			this.putValue(Action.SHORT_DESCRIPTION, "Remove the selected board from the job.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			int index = JobPanel.this.boardLocationsTable.getSelectedRow();
			if (index != -1)
			{
				index = JobPanel.this.boardLocationsTable.convertRowIndexToModel(index);
				BoardLocation boardLocation = JobPanel.this.jobProcessor.getJob().getBoardLocations().get(index);
				JobPanel.this.jobProcessor.getJob().removeBoardLocation(boardLocation);
				JobPanel.this.boardLocationsTableModel.fireTableDataChanged();
			}
		}
	};

	public final Action captureCameraBoardLocationAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.captureCamera);
			this.putValue(Action.NAME, "Capture Camera Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Set the board's location to the camera's current position.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				HeadMountable tool = MainFrame.machineControlsPanel.getSelectedTool();
				Camera camera = tool.getHead().getDefaultCamera();
				double z = JobPanel.this.getSelectedBoardLocation().getLocation().getZ();
				JobPanel.this.getSelectedBoardLocation().setLocation(camera.getLocation().derive(null, null, z, null));
				JobPanel.this.boardLocationsTableModel.fireTableRowsUpdated(JobPanel.this.boardLocationsTable.getSelectedRow(), JobPanel.this.boardLocationsTable.getSelectedRow());
			});
		}
	};

	public final Action captureToolBoardLocationAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.captureTool);
			this.putValue(Action.NAME, "Capture Tool Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Set the board's location to the tool's current position.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			HeadMountable tool = MainFrame.machineControlsPanel.getSelectedTool();
			double z = JobPanel.this.getSelectedBoardLocation().getLocation().getZ();
			JobPanel.this.getSelectedBoardLocation().setLocation(tool.getLocation().derive(null, null, z, null));
			JobPanel.this.boardLocationsTableModel.fireTableRowsUpdated(JobPanel.this.boardLocationsTable.getSelectedRow(), JobPanel.this.boardLocationsTable.getSelectedRow());
		}
	};

	public final Action moveCameraToBoardLocationAction = new AbstractAction("Move Camera To Board Location")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.centerCamera);
			this.putValue(Action.NAME, "Move Camera To Board Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Position the camera at the board's location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				HeadMountable tool = MainFrame.machineControlsPanel.getSelectedTool();
				Camera camera = tool.getHead().getDefaultCamera();
				MainFrame.cameraPanel.ensureCameraVisible(camera);
				Location location = JobPanel.this.getSelectedBoardLocation().getLocation();
				MovableUtils.moveToLocationAtSafeZ(camera, location, 1.0);
			});
		}
	};

	public final Action moveToolToBoardLocationAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.centerTool);
			this.putValue(Action.NAME, "Move Tool To Board Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Position the tool at the board's location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				HeadMountable tool = MainFrame.machineControlsPanel.getSelectedTool();
				Location location = JobPanel.this.getSelectedBoardLocation().getLocation();
				MovableUtils.moveToLocationAtSafeZ(tool, location, 1.0);
			});
		}
	};

	public final Action twoPointLocateBoardLocationAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.twoPointLocate);
			this.putValue(Action.NAME, "Two Point Board Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Set the board's location and rotation using two placements.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				new TwoPlacementBoardLocationProcess(JobPanel.this.frame, JobPanel.this);
			});
		}
	};

	public final Action fiducialCheckAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.fiducialCheck);
			this.putValue(Action.NAME, "Fiducial Check");
			this.putValue(Action.SHORT_DESCRIPTION, "Perform a fiducial check for the board and update it's location and rotation.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				Location location = FiducialLocator.locateBoard(JobPanel.this.getSelectedBoardLocation());
				JobPanel.this.getSelectedBoardLocation().setLocation(location);
				JobPanel.this.refreshSelectedBoardRow();
				HeadMountable tool = MainFrame.machineControlsPanel.getSelectedTool();
				Camera camera = tool.getHead().getDefaultCamera();
				MainFrame.cameraPanel.ensureCameraVisible(camera);
				MovableUtils.moveToLocationAtSafeZ(camera, location, 1.0);
			});
		}
	};

	private final JobProcessorListener jobProcessorListener = new JobProcessorListener.Adapter()
	{
		@Override
		public void jobEncounteredError(JobError error, String description)
		{
			MessageBoxes.errorBox(JobPanel.this.frame, error.toString(), description + "\n\nThe job will be paused.");
			// TODO: Implement a way to retry, abort, etc.
			JobPanel.this.jobProcessor.pause();
		}

		@Override
		public void jobLoaded(Job job)
		{
			if (JobPanel.this.boardLocationsTableModel.getJob() != job)
				// If the same job is being loaded there is no reason to reset
				// the table, so skip it. This allows us to leave the same
				// row selected in the case of switching job processors and
				// tabs.
				JobPanel.this.boardLocationsTableModel.setJob(job);
			job.addPropertyChangeListener("dirty", JobPanel.this.titlePropertyChangeListener);
			job.addPropertyChangeListener("file", JobPanel.this.titlePropertyChangeListener);
			JobPanel.this.updateTitle();
			JobPanel.this.updateJobActions();
		}

		@Override
		public void jobStateChanged(JobState state)
		{
			JobPanel.this.updateJobActions();
		}
	};

	private final JobProcessorDelegate jobProcessorDelegate = new JobProcessorDelegate()
	{
		@Override
		public PickRetryAction partPickFailed(BoardLocation board, Part part, Feeder feeder)
		{
			return PickRetryAction.SkipAndContinue;
		}
	};

	private final MachineListener machineListener = new MachineListener.Adapter()
	{
		@Override
		public void machineDisabled(Machine machine, String reason)
		{
			JobPanel.this.updateJobActions();
			JobPanel.this.jobProcessor.stop();
		}

		@Override
		public void machineEnabled(Machine machine)
		{
			JobPanel.this.updateJobActions();
		}
	};

	private final PropertyChangeListener titlePropertyChangeListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			JobPanel.this.updateTitle();
			JobPanel.this.jobSaveActionGroup.setEnabled(JobPanel.this.jobProcessor.getJob().isDirty());
		}
	};

	public JobPanel(Configuration configuration, MainFrame frame, MachineControlsPanel machineControlsPanel)
	{
		this.configuration = configuration;
		this.frame = frame;
		this.machineControlsPanel = machineControlsPanel;

		this.jobSaveActionGroup = new ActionGroup(this.saveJobAction);
		this.jobSaveActionGroup.setEnabled(false);

		this.boardLocationSelectionActionGroup = new ActionGroup(this.removeBoardAction, this.captureCameraBoardLocationAction, this.captureToolBoardLocationAction,
				this.moveCameraToBoardLocationAction, this.moveToolToBoardLocationAction, this.twoPointLocateBoardLocationAction, this.fiducialCheckAction);
		this.boardLocationSelectionActionGroup.setEnabled(false);

		this.boardLocationsTableModel = new BoardLocationsTableModel(configuration);

		// Suppress because adding the type specifiers breaks WindowBuilder.
		@SuppressWarnings("rawtypes")
		JComboBox sidesComboBox = new JComboBox(Side.values());

		this.boardLocationsTable = new AutoSelectTextTable(this.boardLocationsTableModel);
		this.boardLocationsTable.setAutoCreateRowSorter(true);
		this.boardLocationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.boardLocationsTable.setDefaultEditor(Side.class, new DefaultCellEditor(sidesComboBox));

		this.boardLocationsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;
				BoardLocation boardLocation = JobPanel.this.getSelectedBoardLocation();
				JobPanel.this.boardLocationSelectionActionGroup.setEnabled(boardLocation != null);
				JobPanel.this.jobPlacementsPanel.setBoardLocation(boardLocation);
				JobPanel.this.jobPastePanel.setBoardLocation(boardLocation);
			}
		});

		this.setLayout(new BorderLayout(0, 0));

		this.splitPane = new JSplitPane();
		this.splitPane.setBorder(null);
		this.splitPane.setContinuousLayout(true);
		this.splitPane.setDividerLocation(this.prefs.getInt(JobPanel.PREF_DIVIDER_POSITION, JobPanel.PREF_DIVIDER_POSITION_DEF));
		this.splitPane.addPropertyChangeListener("dividerLocation", new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				JobPanel.this.prefs.putInt(JobPanel.PREF_DIVIDER_POSITION, JobPanel.this.splitPane.getDividerLocation());
			}
		});

		JPanel pnlBoards = new JPanel();
		pnlBoards.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Boards", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pnlBoards.setLayout(new BorderLayout(0, 0));

		JToolBar toolBarBoards = new JToolBar();
		toolBarBoards.setFloatable(false);
		pnlBoards.add(toolBarBoards, BorderLayout.NORTH);

		JButton btnStartPauseResumeJob = new JButton(this.startPauseResumeJobAction);
		btnStartPauseResumeJob.setHideActionText(true);
		toolBarBoards.add(btnStartPauseResumeJob);
		JButton btnStepJob = new JButton(this.stepJobAction);
		btnStepJob.setHideActionText(true);
		toolBarBoards.add(btnStepJob);
		JButton btnStopJob = new JButton(this.stopJobAction);
		btnStopJob.setHideActionText(true);
		toolBarBoards.add(btnStopJob);
		toolBarBoards.addSeparator();
		JButton btnNewBoard = new JButton(this.newBoardAction);
		btnNewBoard.setHideActionText(true);
		toolBarBoards.add(btnNewBoard);
		JButton btnAddBoard = new JButton(this.addBoardAction);
		btnAddBoard.setHideActionText(true);
		toolBarBoards.add(btnAddBoard);
		JButton btnRemoveBoard = new JButton(this.removeBoardAction);
		btnRemoveBoard.setHideActionText(true);
		toolBarBoards.add(btnRemoveBoard);
		toolBarBoards.addSeparator();
		JButton btnCaptureCameraBoardLocation = new JButton(this.captureCameraBoardLocationAction);
		btnCaptureCameraBoardLocation.setHideActionText(true);
		toolBarBoards.add(btnCaptureCameraBoardLocation);

		JButton btnCaptureToolBoardLocation = new JButton(this.captureToolBoardLocationAction);
		btnCaptureToolBoardLocation.setHideActionText(true);
		toolBarBoards.add(btnCaptureToolBoardLocation);

		JButton btnPositionCameraBoardLocation = new JButton(this.moveCameraToBoardLocationAction);
		btnPositionCameraBoardLocation.setHideActionText(true);
		toolBarBoards.add(btnPositionCameraBoardLocation);

		JButton btnPositionToolBoardLocation = new JButton(this.moveToolToBoardLocationAction);
		btnPositionToolBoardLocation.setHideActionText(true);
		toolBarBoards.add(btnPositionToolBoardLocation);
		toolBarBoards.addSeparator();

		JButton btnTwoPointBoardLocation = new JButton(this.twoPointLocateBoardLocationAction);
		toolBarBoards.add(btnTwoPointBoardLocation);
		btnTwoPointBoardLocation.setHideActionText(true);

		JButton btnFiducialCheck = new JButton(this.fiducialCheckAction);
		toolBarBoards.add(btnFiducialCheck);
		btnFiducialCheck.setHideActionText(true);

		pnlBoards.add(new JScrollPane(this.boardLocationsTable));
		JPanel pnlRight = new JPanel();
		pnlRight.setLayout(new BorderLayout(0, 0));

		this.splitPane.setLeftComponent(pnlBoards);
		this.splitPane.setRightComponent(pnlRight);

		this.tabbedPane = new JTabbedPane(SwingConstants.TOP);
		pnlRight.add(this.tabbedPane, BorderLayout.CENTER);

		this.tabbedPane.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				Machine machine = Configuration.get().getMachine();
				JobProcessor.Type type = JobPanel.this.getSelectedJobProcessorType();
				JobPanel.this.setJobProcessor(machine.getJobProcessors().get(type));
			}
		});

		this.jobPastePanel = new JobPastePanel(this);
		this.jobPlacementsPanel = new JobPlacementsPanel(this);

		this.add(this.splitPane);

		this.mnOpenRecent = new JMenu("Open Recent Job...");
		this.loadRecentJobs();

		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationComplete(Configuration configuration) throws Exception
			{
				Machine machine = configuration.getMachine();

				machine.addListener(JobPanel.this.machineListener);

				for (JobProcessor jobProcessor : machine.getJobProcessors().values())
				{
					jobProcessor.addListener(JobPanel.this.jobProcessorListener);
					jobProcessor.setDelegate(JobPanel.this.jobProcessorDelegate);
				}

				if (machine.getJobProcessors().get(JobProcessor.Type.PickAndPlace) != null)
				{
					JobPanel.this.tabbedPane.addTab("Pick and Place", null, JobPanel.this.jobPlacementsPanel, null);
					// Creating the tab should fire off the selection event,
					// setting
					// the JobProcessor but this fails on some Linux based
					// systems,
					// so here we detect if it failed and force setting it.
					if (JobPanel.this.jobProcessor == null)
						JobPanel.this.setJobProcessor(machine.getJobProcessors().get(JobProcessor.Type.PickAndPlace));
				}
				if (machine.getJobProcessors().get(JobProcessor.Type.SolderPaste) != null)
				{
					JobPanel.this.tabbedPane.addTab("Solder Paste", null, JobPanel.this.jobPastePanel, null);
					// Creating the tab should fire off the selection event,
					// setting
					// the JobProcessor but this fails on some Linux based
					// systems,
					// so here we detect if it failed and force setting it.
					if (JobPanel.this.jobProcessor == null)
						JobPanel.this.setJobProcessor(machine.getJobProcessors().get(JobProcessor.Type.SolderPaste));
				}

				// Create an empty Job if one is not loaded
				if (JobPanel.this.jobProcessor.getJob() == null)
				{
					Job job = new Job();
					JobPanel.this.jobProcessor.load(job);
				}
			}
		});
	}

	private void addRecentJob(File file)
	{
		while (this.recentJobs.contains(file))
			this.recentJobs.remove(file);
		// add to top
		this.recentJobs.add(0, file);
		// limit length
		while (this.recentJobs.size() > JobPanel.PREF_RECENT_FILES_MAX)
			this.recentJobs.remove(this.recentJobs.size() - 1);
		this.saveRecentJobs();
	}

	private boolean checkForBoardModifications()
	{
		for (Board board : this.configuration.getBoards())
			if (board.isDirty())
			{
				int result = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(),
						"Do you want to save your changes to " + board.getFile().getName() + "?" + "\n" + "If you don't save, your changes will be lost.", "Save " + board.getFile().getName() + "?",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == JOptionPane.YES_OPTION)
					try
					{
						this.configuration.saveBoard(board);
					} catch (Exception e)
					{
						MessageBoxes.errorBox(this.getTopLevelAncestor(), "Board Save Error", e.getMessage());
						return false;
					}
				else if (result == JOptionPane.CANCEL_OPTION)
					return false;
			}
		return true;
	}

	private boolean checkForJobModifications()
	{
		if (this.jobProcessor.getJob().isDirty())
		{
			Job job = this.jobProcessor.getJob();
			String name = job.getFile() == null ? JobPanel.UNTITLED_JOB_FILENAME : job.getFile().getName();
			int result = JOptionPane.showConfirmDialog(this.frame, "Do you want to save your changes to " + name + "?" + "\n" + "If you don't save, your changes will be lost.", "Save " + name + "?",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (result == JOptionPane.YES_OPTION)
				return this.saveJob();
			else if (result == JOptionPane.CANCEL_OPTION)
				return false;
		}
		return true;
	}

	/**
	 * Checks if there are any modifications that need to be saved. Prompts the
	 * user if there are. Returns true if it's okay to exit.
	 * 
	 * @return
	 */
	public boolean checkForModifications()
	{
		if (!this.checkForBoardModifications())
			return false;
		if (!this.checkForJobModifications())
			return false;
		return true;
	}

	public JobPlacementsPanel getJobPlacementsPanel()
	{
		return this.jobPlacementsPanel;
	}

	public BoardLocation getSelectedBoardLocation()
	{
		int index = this.boardLocationsTable.getSelectedRow();
		if (index == -1)
			return null;
		else
		{
			index = this.boardLocationsTable.convertRowIndexToModel(index);
			return JobPanel.this.jobProcessor.getJob().getBoardLocations().get(index);
		}
	}

	public JobProcessor.Type getSelectedJobProcessorType()
	{
		String activeTabTitle = this.tabbedPane.getTitleAt(this.tabbedPane.getSelectedIndex());
		if (activeTabTitle.equals("Solder Paste"))
			return JobProcessor.Type.SolderPaste;
		else if (activeTabTitle.equals("Pick and Place"))
			return JobProcessor.Type.PickAndPlace;
		else
			throw new Error("Unknown job tab title: " + activeTabTitle);
	}

	public void importBoard(Class<? extends BoardImporter> boardImporterClass)
	{
		if (this.getSelectedBoardLocation() == null)
		{
			MessageBoxes.errorBox(this.getTopLevelAncestor(), "Import Failed", "Please select a board in the Jobs tab to import into.");
			return;
		}

		BoardImporter boardImporter;
		try
		{
			boardImporter = boardImporterClass.newInstance();
		} catch (Exception e)
		{
			MessageBoxes.errorBox(this.getTopLevelAncestor(), "Import Failed", e);
			return;
		}

		try
		{
			Board importedBoard = boardImporter.importBoard((Frame) this.getTopLevelAncestor());
			if (importedBoard != null)
			{
				Board existingBoard = this.getSelectedBoardLocation().getBoard();
				for (Placement placement : importedBoard.getPlacements())
					existingBoard.addPlacement(placement);
				for (BoardPad pad : importedBoard.getSolderPastePads())
				{
					// TODO: This is a temporary hack until we redesign the
					// importer
					// interface to be more intuitive. The Gerber importer tends
					// to return everything in Inches, so this is a method to
					// try to get it closer to what the user expects to see.
					pad.setLocation(pad.getLocation().convertToUnits(this.getSelectedBoardLocation().getLocation().getUnits()));
					existingBoard.addSolderPastePad(pad);
				}
				this.jobPlacementsPanel.setBoardLocation(this.getSelectedBoardLocation());
				this.jobPastePanel.setBoardLocation(this.getSelectedBoardLocation());
			}
		} catch (Exception e)
		{
			MessageBoxes.errorBox(this.getTopLevelAncestor(), "Import Failed", e);
		}
	}

	private void loadRecentJobs()
	{
		this.recentJobs.clear();
		for (int i = 0; i < JobPanel.PREF_RECENT_FILES_MAX; i++)
		{
			String path = this.prefs.get(JobPanel.PREF_RECENT_FILES + "_" + i, null);
			if (path != null)
			{
				File file = new File(path);
				this.recentJobs.add(file);
			}
		}
		this.updateRecentJobsMenu();
	}

	public void refreshSelectedBoardRow()
	{
		this.boardLocationsTableModel.fireTableRowsUpdated(this.boardLocationsTable.getSelectedRow(), this.boardLocationsTable.getSelectedRow());
	}

	private boolean saveJob()
	{
		if (this.jobProcessor.getJob().getFile() == null)
			return this.saveJobAs();
		else
			try
			{
				File file = this.jobProcessor.getJob().getFile();
				this.configuration.saveJob(this.jobProcessor.getJob(), file);
				this.addRecentJob(file);
				return true;
			} catch (Exception e)
			{
				MessageBoxes.errorBox(this.frame, "Job Save Error", e.getMessage());
				return false;
			}
	}

	private boolean saveJobAs()
	{
		FileDialog fileDialog = new FileDialog(this.frame, "Save Job As...", FileDialog.SAVE);
		fileDialog.setFilenameFilter(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.toLowerCase().endsWith(".job.xml");
			}
		});
		fileDialog.setVisible(true);
		try
		{
			String filename = fileDialog.getFile();
			if (filename == null)
				return false;
			if (!filename.toLowerCase().endsWith(".job.xml"))
				filename = filename + ".job.xml";
			File file = new File(new File(fileDialog.getDirectory()), filename);
			if (file.exists())
			{
				int ret = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(), file.getName() + " already exists. Do you want to replace it?", "Replace file?", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (ret != JOptionPane.YES_OPTION)
					return false;
			}
			this.configuration.saveJob(this.jobProcessor.getJob(), file);
			this.addRecentJob(file);
			return true;
		} catch (Exception e)
		{
			MessageBoxes.errorBox(this.frame, "Job Save Error", e.getMessage());
			return false;
		}
	}

	private void saveRecentJobs()
	{
		// blow away all the existing values
		for (int i = 0; i < JobPanel.PREF_RECENT_FILES_MAX; i++)
			this.prefs.remove(JobPanel.PREF_RECENT_FILES + "_" + i);
		// update with what we have now
		for (int i = 0; i < this.recentJobs.size(); i++)
			this.prefs.put(JobPanel.PREF_RECENT_FILES + "_" + i, this.recentJobs.get(i).getAbsolutePath());
		this.updateRecentJobsMenu();
	}

	/**
	 * Unregister the listener and delegate for the JobProcessor, set the new
	 * JobProcessor and add the listener and delegate back. If a job was
	 * previously loaded into the JobProcessor, load it into the new one. The
	 * sequencing of making this work is a bit complex. When the app is starting
	 * the following happens: 1. The UI is created and shown. At this time no
	 * JobProcessor is set. 2. The Configuration is loaded and the completion
	 * listener is called. 3. The Configuration listener checks which
	 * JobProcessors are registered and adds tabs for each. 4. The first tab
	 * that is added causes a selection event to happen, which fires a
	 * ChangeEvent on the ChangeListener above. 5. The ChangeListener checks
	 * which tab was selected and calls this method with the appropriate
	 * JobProcessor.
	 * 
	 * @param jobProcessor
	 */
	private void setJobProcessor(JobProcessor jobProcessor)
	{
		Job job = null;
		if (this.jobProcessor != null)
		{
			job = this.jobProcessor.getJob();
			if (this.jobProcessor.getState() != null && this.jobProcessor.getState() != JobProcessor.JobState.Stopped)
				throw new AssertionError("this.jobProcessor.getState() " + this.jobProcessor.getState() + " != JobProcessor.JobState.Stopped");
			this.jobProcessor.removeListener(this.jobProcessorListener);
			this.jobProcessor.setDelegate(null);
		}
		this.jobProcessor = jobProcessor;
		jobProcessor.addListener(this.jobProcessorListener);
		jobProcessor.setDelegate(this.jobProcessorDelegate);
		if (job != null)
			jobProcessor.load(job);
	}

	/**
	 * Updates the Job controls based on the Job state and the Machine's
	 * readiness.
	 */
	private void updateJobActions()
	{
		JobState state = this.jobProcessor.getState();
		if (state == JobState.Stopped)
		{
			this.startPauseResumeJobAction.setEnabled(true);
			this.startPauseResumeJobAction.putValue(Action.NAME, "Start");
			this.startPauseResumeJobAction.putValue(Action.SMALL_ICON, Icons.start);
			this.startPauseResumeJobAction.putValue(Action.SHORT_DESCRIPTION, "Start processing the job.");
			this.stopJobAction.setEnabled(false);
			this.stepJobAction.setEnabled(true);
			this.tabbedPane.setEnabled(true);
		} else if (state == JobState.Running)
		{
			this.startPauseResumeJobAction.setEnabled(true);
			this.startPauseResumeJobAction.putValue(Action.NAME, "Pause");
			this.startPauseResumeJobAction.putValue(Action.SMALL_ICON, Icons.pause);
			this.startPauseResumeJobAction.putValue(Action.SHORT_DESCRIPTION, "Pause processing of the job.");
			this.stopJobAction.setEnabled(true);
			this.stepJobAction.setEnabled(false);
			this.tabbedPane.setEnabled(false);
		} else if (state == JobState.Paused)
		{
			this.startPauseResumeJobAction.setEnabled(true);
			this.startPauseResumeJobAction.putValue(Action.NAME, "Resume");
			this.startPauseResumeJobAction.putValue(Action.SMALL_ICON, Icons.start);
			this.startPauseResumeJobAction.putValue(Action.SHORT_DESCRIPTION, "Resume processing of the job.");
			this.stopJobAction.setEnabled(true);
			this.stepJobAction.setEnabled(true);
			this.tabbedPane.setEnabled(false);
		}

		// We allow the above to run first so that all state is represented
		// correctly even if the machine is disabled.
		if (!this.configuration.getMachine().isEnabled())
		{
			this.startPauseResumeJobAction.setEnabled(false);
			this.stopJobAction.setEnabled(false);
			this.stepJobAction.setEnabled(false);
		}
	}

	private void updateRecentJobsMenu()
	{
		this.mnOpenRecent.removeAll();
		for (File file : this.recentJobs)
			this.mnOpenRecent.add(new OpenRecentJobAction(file));
	}

	private void updateTitle()
	{
		Job job = this.jobProcessor.getJob();
		String title = String.format("OpenPnP - %s%s", job.isDirty() ? "*" : "", job.getFile() == null ? JobPanel.UNTITLED_JOB_FILENAME : job.getFile().getName());
		this.frame.setTitle(title);
	}
}
