package org.openpnp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.openpnp.gui.components.AutoSelectTextTable;
import org.openpnp.gui.support.ActionGroup;
import org.openpnp.gui.support.Helpers;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.IdentifiableListCellRenderer;
import org.openpnp.gui.support.IdentifiableTableCellRenderer;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.PartsComboBoxModel;
import org.openpnp.gui.tablemodel.PlacementsTableModel;
import org.openpnp.gui.tablemodel.PlacementsTableModel.Status;
import org.openpnp.model.Board;
import org.openpnp.model.Board.Side;
import org.openpnp.model.BoardLocation;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.model.Part;
import org.openpnp.model.Placement;
import org.openpnp.model.Placement.Type;
import org.openpnp.spi.Camera;
import org.openpnp.spi.HeadMountable;
import org.openpnp.spi.Nozzle;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;
import org.openpnp.util.Utils2D;

public class JobPlacementsPanel extends JPanel
{
	class SetSideAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 1L;
		final Board.Side			side;

		public SetSideAction(Board.Side side)
		{
			this.side = side;
			this.putValue(Action.NAME, side.toString());
			this.putValue(Action.SHORT_DESCRIPTION, "Set placement side(s) to " + side.toString());
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			for (Placement placement : JobPlacementsPanel.this.getSelections())
				placement.setSide(this.side);
		}
	}

	class SetTypeAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 1L;
		final Placement.Type		type;

		public SetTypeAction(Placement.Type type)
		{
			this.type = type;
			this.putValue(Action.NAME, type.toString());
			this.putValue(Action.SHORT_DESCRIPTION, "Set placement type(s) to " + type.toString());
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			for (Placement placement : JobPlacementsPanel.this.getSelections())
				placement.setType(this.type);
		}
	}

	static class StatusRenderer extends DefaultTableCellRenderer
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Object value)
		{
			Status status = (Status) value;
			if (status == Status.Ready)
			{
				this.setBorder(new LineBorder(this.getBackground()));
				this.setForeground(Color.black);
				this.setBackground(JobPlacementsPanel.statusColorReady);
				this.setText("Ready");
			} else if (status == Status.MissingFeeder)
			{
				this.setBorder(new LineBorder(this.getBackground()));
				this.setForeground(Color.black);
				this.setBackground(JobPlacementsPanel.statusColorError);
				this.setText("Missing Feeder");
			} else if (status == Status.ZeroPartHeight)
			{
				this.setBorder(new LineBorder(this.getBackground()));
				this.setForeground(Color.black);
				this.setBackground(JobPlacementsPanel.statusColorWarning);
				this.setText("Part Height");
			} else if (status == Status.MissingPart)
			{
				this.setBorder(new LineBorder(this.getBackground()));
				this.setForeground(Color.black);
				this.setBackground(JobPlacementsPanel.statusColorError);
				this.setText("Missing Part");
			} else
			{
				this.setBorder(new LineBorder(this.getBackground()));
				this.setForeground(Color.black);
				this.setBackground(JobPlacementsPanel.statusColorError);
				this.setText(status.toString());
			}
		}
	}

	static class TypeRenderer extends DefaultTableCellRenderer
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void setValue(Object value)
		{
			Type type = (Type) value;
			this.setText(type.name());
			if (type == Type.Fiducial)
			{
				this.setBorder(new LineBorder(this.getBackground()));
				this.setForeground(Color.black);
				this.setBackground(JobPlacementsPanel.typeColorFiducial);
			} else if (type == Type.Ignore)
			{
				this.setBorder(new LineBorder(this.getBackground()));
				this.setForeground(Color.black);
				this.setBackground(JobPlacementsPanel.typeColorIgnore);
			} else if (type == Type.Place)
			{
				this.setBorder(new LineBorder(this.getBackground()));
				this.setForeground(Color.black);
				this.setBackground(JobPlacementsPanel.typeColorPlace);
			}
		}
	}

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private static Color		typeColorIgnore		= new Color(252, 255, 157);
	private static Color		typeColorFiducial	= new Color(157, 188, 255);
	private static Color		typeColorPlace		= new Color(157, 255, 168);

	private static Color			statusColorWarning	= new Color(252, 255, 157);
	private static Color			statusColorReady	= new Color(157, 255, 168);
	private static Color			statusColorError	= new Color(255, 157, 157);
	private JTable					table;
	private PlacementsTableModel	tableModel;
	private ActionGroup				boardLocationSelectionActionGroup;

	private ActionGroup singleSelectionActionGroup;

	private ActionGroup multiSelectionActionGroup;

	private ActionGroup captureAndPositionActionGroup;

	private BoardLocation boardLocation;

	public final Action newAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.add);
			this.putValue(Action.NAME, "New Placement");
			this.putValue(Action.SHORT_DESCRIPTION, "Create a new placement and add it to the board.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (Configuration.get().getParts().size() == 0)
			{
				MessageBoxes.errorBox(JobPlacementsPanel.this.getTopLevelAncestor(), "Error",
						"There are currently no parts defined in the system. Please create at least one part before creating a placement.");
				return;
			}

			String id = JOptionPane.showInputDialog(JobPlacementsPanel.this.getTopLevelAncestor(), "Please enter an ID for the new placement.");
			if (id == null)
				return;
			// TODO: Make sure it's unique.
			Placement placement = new Placement(id);

			placement.setPart(Configuration.get().getParts().get(0));
			placement.setLocation(new Location(Configuration.get().getSystemUnits()));

			JobPlacementsPanel.this.boardLocation.getBoard().addPlacement(placement);
			JobPlacementsPanel.this.tableModel.fireTableDataChanged();
			Helpers.selectLastTableRow(JobPlacementsPanel.this.table);
		}
	};

	public final Action removeAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.delete);
			this.putValue(Action.NAME, "Remove Placement(s)");
			this.putValue(Action.SHORT_DESCRIPTION, "Remove the currently selected placement(s).");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			for (Placement placement : JobPlacementsPanel.this.getSelections())
				JobPlacementsPanel.this.boardLocation.getBoard().removePlacement(placement);
			JobPlacementsPanel.this.tableModel.fireTableDataChanged();
		}
	};

	public final Action moveCameraToPlacementLocation = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.centerCamera);
			this.putValue(Action.NAME, "Move Camera To Placement Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Position the camera at the placement's location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				Location location = Utils2D.calculateBoardPlacementLocation(JobPlacementsPanel.this.boardLocation, JobPlacementsPanel.this.getSelection().getLocation());

				Camera camera = MainFrame.machineControlsPanel.getSelectedTool().getHead().getDefaultCamera();
				MovableUtils.moveToLocationAtSafeZ(camera, location, 1.0);
			});
		}
	};

	public final Action moveToolToPlacementLocation = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.centerTool);
			this.putValue(Action.NAME, "Move Tool To Placement Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Position the tool at the placement's location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			Location location = Utils2D.calculateBoardPlacementLocation(JobPlacementsPanel.this.boardLocation, JobPlacementsPanel.this.getSelection().getLocation());

			Nozzle nozzle = MainFrame.machineControlsPanel.getSelectedNozzle();
			UiUtils.submitUiMachineTask(() -> {
				MovableUtils.moveToLocationAtSafeZ(nozzle, location, 1.0);
			});
		}
	};

	public final Action captureCameraPlacementLocation = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.captureCamera);
			this.putValue(Action.NAME, "Capture Camera Placement Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Set the placement's location to the camera's current position.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.messageBoxOnException(() -> {
				HeadMountable tool = MainFrame.machineControlsPanel.getSelectedTool();
				Camera camera = tool.getHead().getDefaultCamera();
				Location placementLocation = Utils2D.calculateBoardPlacementLocationInverse(JobPlacementsPanel.this.boardLocation, camera.getLocation());
				JobPlacementsPanel.this.getSelection().setLocation(placementLocation);
				JobPlacementsPanel.this.table.repaint();
			});
		}
	};

	public final Action captureToolPlacementLocation = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.captureTool);
			this.putValue(Action.NAME, "Capture Tool Placement Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Set the placement's location to the tool's current position.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			Nozzle nozzle = MainFrame.machineControlsPanel.getSelectedNozzle();
			Location placementLocation = Utils2D.calculateBoardPlacementLocationInverse(JobPlacementsPanel.this.boardLocation, nozzle.getLocation());
			JobPlacementsPanel.this.getSelection().setLocation(placementLocation);
			JobPlacementsPanel.this.table.repaint();
		}
	};

	public final Action editPlacementFeederAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.editFeeder);
			this.putValue(Action.NAME, "Edit Placement Feeder");
			this.putValue(Action.SHORT_DESCRIPTION, "Edit the placement's associated feeder definition.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			Placement placement = JobPlacementsPanel.this.getSelection();
			MainFrame.feedersPanel.showFeederForPart(placement.getPart());
		}
	};

	public final Action setTypeAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.NAME, "Set Type");
			this.putValue(Action.SHORT_DESCRIPTION, "Set placement type(s) to...");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
		}
	};

	public final Action setSideAction = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.NAME, "Set Side");
			this.putValue(Action.SHORT_DESCRIPTION, "Set placement side(s) to...");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
		}
	};;

	public JobPlacementsPanel(JobPanel jobPanel)
	{
		Configuration configuration = Configuration.get();

		this.boardLocationSelectionActionGroup = new ActionGroup(this.newAction);
		this.boardLocationSelectionActionGroup.setEnabled(false);

		this.singleSelectionActionGroup = new ActionGroup(this.removeAction, this.editPlacementFeederAction, this.setTypeAction);
		this.singleSelectionActionGroup.setEnabled(false);

		this.multiSelectionActionGroup = new ActionGroup(this.removeAction, this.setTypeAction);
		this.multiSelectionActionGroup.setEnabled(false);

		this.captureAndPositionActionGroup = new ActionGroup(this.captureCameraPlacementLocation, this.captureToolPlacementLocation, this.moveCameraToPlacementLocation,
				this.moveToolToPlacementLocation);
		this.captureAndPositionActionGroup.setEnabled(false);

		JComboBox<PartsComboBoxModel> partsComboBox = new JComboBox(new PartsComboBoxModel());
		partsComboBox.setRenderer(new IdentifiableListCellRenderer<Part>());
		JComboBox<Side> sidesComboBox = new JComboBox(Side.values());
		JComboBox<Type> typesComboBox = new JComboBox(Type.values());

		this.setLayout(new BorderLayout(0, 0));
		JToolBar toolBarPlacements = new JToolBar();
		this.add(toolBarPlacements, BorderLayout.NORTH);

		toolBarPlacements.setFloatable(false);
		JButton btnNewPlacement = new JButton(this.newAction);
		btnNewPlacement.setHideActionText(true);
		toolBarPlacements.add(btnNewPlacement);
		JButton btnRemovePlacement = new JButton(this.removeAction);
		btnRemovePlacement.setHideActionText(true);
		toolBarPlacements.add(btnRemovePlacement);
		toolBarPlacements.addSeparator();
		JButton btnCaptureCameraPlacementLocation = new JButton(this.captureCameraPlacementLocation);
		btnCaptureCameraPlacementLocation.setHideActionText(true);
		toolBarPlacements.add(btnCaptureCameraPlacementLocation);

		JButton btnCaptureToolPlacementLocation = new JButton(this.captureToolPlacementLocation);
		btnCaptureToolPlacementLocation.setHideActionText(true);
		toolBarPlacements.add(btnCaptureToolPlacementLocation);

		JButton btnPositionCameraPositionLocation = new JButton(this.moveCameraToPlacementLocation);
		btnPositionCameraPositionLocation.setHideActionText(true);
		toolBarPlacements.add(btnPositionCameraPositionLocation);

		JButton btnPositionToolPositionLocation = new JButton(this.moveToolToPlacementLocation);
		btnPositionToolPositionLocation.setHideActionText(true);
		toolBarPlacements.add(btnPositionToolPositionLocation);

		toolBarPlacements.addSeparator();

		JButton btnEditFeeder = new JButton(this.editPlacementFeederAction);
		btnEditFeeder.setHideActionText(true);
		toolBarPlacements.add(btnEditFeeder);

		this.tableModel = new PlacementsTableModel(configuration);

		this.table = new AutoSelectTextTable(this.tableModel);
		this.table.setAutoCreateRowSorter(true);
		this.table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.table.setDefaultEditor(Side.class, new DefaultCellEditor(sidesComboBox));
		this.table.setDefaultEditor(Part.class, new DefaultCellEditor(partsComboBox));
		this.table.setDefaultEditor(Type.class, new DefaultCellEditor(typesComboBox));
		this.table.setDefaultRenderer(Part.class, new IdentifiableTableCellRenderer<Part>());
		this.table.setDefaultRenderer(PlacementsTableModel.Status.class, new StatusRenderer());
		this.table.setDefaultRenderer(Placement.Type.class, new TypeRenderer());
		this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;

				if (JobPlacementsPanel.this.getSelections().size() > 1)
				{
					// multi select
					JobPlacementsPanel.this.singleSelectionActionGroup.setEnabled(false);
					JobPlacementsPanel.this.captureAndPositionActionGroup.setEnabled(false);
					JobPlacementsPanel.this.multiSelectionActionGroup.setEnabled(true);
				} else
				{
					// single select, or no select
					JobPlacementsPanel.this.multiSelectionActionGroup.setEnabled(false);
					JobPlacementsPanel.this.singleSelectionActionGroup.setEnabled(JobPlacementsPanel.this.getSelection() != null);
					JobPlacementsPanel.this.captureAndPositionActionGroup
							.setEnabled(JobPlacementsPanel.this.getSelection() != null && JobPlacementsPanel.this.getSelection().getSide() == JobPlacementsPanel.this.boardLocation.getSide());
				}
			}
		});
		this.table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent mouseEvent)
			{
				if (mouseEvent.getClickCount() != 2)
					return;
				int row = JobPlacementsPanel.this.table.rowAtPoint(new Point(mouseEvent.getX(), mouseEvent.getY()));
				int col = JobPlacementsPanel.this.table.columnAtPoint(new Point(mouseEvent.getX(), mouseEvent.getY()));
				if (JobPlacementsPanel.this.tableModel.getColumnClass(col) == Status.class)
				{
					Status status = (Status) JobPlacementsPanel.this.tableModel.getValueAt(row, col);
					// TODO: This is some sample code for handling the user
					// wishing to do something with the status. Not using it
					// right now but leaving it here for the future.
					System.out.println(status);
				}
			}
		});
		this.table.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
				if (e.getKeyChar() == ' ')
				{
					Placement placement = JobPlacementsPanel.this.getSelection();
					placement.setType(placement.getType() == Type.Place ? Type.Ignore : Type.Place);
					JobPlacementsPanel.this.tableModel.fireTableRowsUpdated(JobPlacementsPanel.this.table.getSelectedRow(), JobPlacementsPanel.this.table.getSelectedRow());
				} else
					super.keyTyped(e);
			}
		});

		JPopupMenu popupMenu = new JPopupMenu();

		JMenu setTypeMenu = new JMenu(this.setTypeAction);
		for (Placement.Type type : Placement.Type.values())
			setTypeMenu.add(new SetTypeAction(type));
		popupMenu.add(setTypeMenu);

		JMenu setSideMenu = new JMenu(this.setSideAction);
		for (Board.Side side : Board.Side.values())
			setSideMenu.add(new SetSideAction(side));
		popupMenu.add(setSideMenu);

		this.table.setComponentPopupMenu(popupMenu);

		JScrollPane scrollPane = new JScrollPane(this.table);
		this.add(scrollPane, BorderLayout.CENTER);
	}

	public Placement getSelection()
	{
		List<Placement> selectedPlacements = this.getSelections();
		if (selectedPlacements.isEmpty())
			return null;
		return selectedPlacements.get(0);
	};

	public List<Placement> getSelections()
	{
		ArrayList<Placement> placements = new ArrayList<>();
		if (this.boardLocation == null)
			return placements;
		int[] selectedRows = this.table.getSelectedRows();
		for (int selectedRow : selectedRows)
		{
			selectedRow = this.table.convertRowIndexToModel(selectedRow);
			placements.add(this.boardLocation.getBoard().getPlacements().get(selectedRow));
		}
		return placements;
	}

	public void setBoardLocation(BoardLocation boardLocation)
	{
		this.boardLocation = boardLocation;
		if (boardLocation == null)
		{
			this.tableModel.setBoard(null);
			this.boardLocationSelectionActionGroup.setEnabled(false);
		} else
		{
			this.tableModel.setBoard(boardLocation.getBoard());
			this.boardLocationSelectionActionGroup.setEnabled(true);
		}
	}
}
