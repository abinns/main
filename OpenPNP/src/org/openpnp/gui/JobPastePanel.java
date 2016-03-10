package org.openpnp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import org.openpnp.gui.components.ClassSelectionDialog;
import org.openpnp.gui.support.ActionGroup;
import org.openpnp.gui.support.Helpers;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.tablemodel.PadsTableModel;
import org.openpnp.model.Board;
import org.openpnp.model.Board.Side;
import org.openpnp.model.BoardLocation;
import org.openpnp.model.BoardPad;
import org.openpnp.model.BoardPad.Type;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.model.Pad;
import org.openpnp.spi.Camera;
import org.openpnp.spi.PasteDispenser;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.UiUtils;
import org.openpnp.util.Utils2D;

public class JobPastePanel extends JPanel
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
			this.putValue(Action.SHORT_DESCRIPTION, "Set pad side(s) to " + side.toString());
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			for (BoardPad pad : JobPastePanel.this.getSelections())
				pad.setSide(this.side);
		}
	}

	class SetTypeAction extends AbstractAction
	{
		/**
		 * 
		 */
		private static final long	serialVersionUID	= 1L;
		final BoardPad.Type			type;

		public SetTypeAction(BoardPad.Type type)
		{
			this.type = type;
			this.putValue(Action.NAME, type.toString());
			this.putValue(Action.SHORT_DESCRIPTION, "Set pad type(s) to " + type.toString());
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			for (BoardPad pad : JobPastePanel.this.getSelections())
				pad.setType(this.type);
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
			if (type == Type.Paste)
			{
				this.setBorder(new LineBorder(this.getBackground()));
				this.setForeground(Color.black);
				this.setBackground(JobPastePanel.typeColorPaste);
			} else if (type == Type.Ignore)
			{
				this.setBorder(new LineBorder(this.getBackground()));
				this.setForeground(Color.black);
				this.setBackground(JobPastePanel.typeColorIgnore);
			}
		}
	}

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private static Color		typeColorIgnore		= new Color(252, 255, 157);
	private static Color		typeColorPaste		= new Color(157, 255, 168);
	private JTable				table;
	private PadsTableModel		tableModel;

	private ActionGroup	boardLocationSelectionActionGroup;
	private ActionGroup	singleSelectionActionGroup;

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
			this.putValue(Action.NAME, "New Pad");
			this.putValue(Action.SHORT_DESCRIPTION, "Create a new pad and add it to the board.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			List<Class<? extends Pad>> padClasses = new ArrayList<>();
			padClasses.add(Pad.RoundRectangle.class);
			padClasses.add(Pad.Circle.class);
			padClasses.add(Pad.Ellipse.class);
			// See note on Pad.Line
			// padClasses.add(Pad.Line.class);
			ClassSelectionDialog<Pad> dialog = new ClassSelectionDialog<>(JOptionPane.getFrameForComponent(JobPastePanel.this), "Select Pad...", "Please select a pad type from the list below.",
					padClasses);
			dialog.setVisible(true);
			Class<? extends Pad> padClass = dialog.getSelectedClass();
			if (padClass == null)
				return;
			try
			{
				Pad pad = padClass.newInstance();
				BoardPad boardPad = new BoardPad();
				boardPad.setLocation(new Location(Configuration.get().getSystemUnits()));
				boardPad.setPad(pad);

				JobPastePanel.this.boardLocation.getBoard().addSolderPastePad(boardPad);
				JobPastePanel.this.tableModel.fireTableDataChanged();
				Helpers.selectLastTableRow(JobPastePanel.this.table);
			} catch (Exception e)
			{
				MessageBoxes.errorBox(JOptionPane.getFrameForComponent(JobPastePanel.this), "Pad Error", e);
			}
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
			this.putValue(Action.NAME, "Remove Pad");
			this.putValue(Action.SHORT_DESCRIPTION, "Remove the currently selected pad.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			for (BoardPad pad : JobPastePanel.this.getSelections())
				JobPastePanel.this.boardLocation.getBoard().removeSolderPastePad(pad);
			JobPastePanel.this.tableModel.fireTableDataChanged();
		}
	};

	public final Action moveCameraToPadLocation = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.centerCamera);
			this.putValue(Action.NAME, "Move Camera To Pad Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Position the camera at the pad's location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				Location location = Utils2D.calculateBoardPlacementLocation(JobPastePanel.this.boardLocation, JobPastePanel.this.getSelection().getLocation());

				Camera camera = MainFrame.machineControlsPanel.getSelectedTool().getHead().getDefaultCamera();
				MovableUtils.moveToLocationAtSafeZ(camera, location, 1.0);
			});
		}
	};

	public final Action moveToolToPadLocation = new AbstractAction()
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.centerTool);
			this.putValue(Action.NAME, "Move Tool To Pad Location");
			this.putValue(Action.SHORT_DESCRIPTION, "Position the tool at the pad's location.");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			Location location = Utils2D.calculateBoardPlacementLocation(JobPastePanel.this.boardLocation, JobPastePanel.this.getSelection().getLocation());

			PasteDispenser dispenser = MainFrame.machineControlsPanel.getSelectedPasteDispenser();
			UiUtils.submitUiMachineTask(() -> {
				MovableUtils.moveToLocationAtSafeZ(dispenser, location, 1.0);
			});
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
			this.putValue(Action.SHORT_DESCRIPTION, "Set pad type(s) to...");
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
			this.putValue(Action.SHORT_DESCRIPTION, "Set pad side(s) to...");
		}

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
		}
	};

	public JobPastePanel(JobPanel jobPanel)
	{
		Configuration configuration = Configuration.get();

		this.boardLocationSelectionActionGroup = new ActionGroup(this.newAction);
		this.boardLocationSelectionActionGroup.setEnabled(false);

		this.singleSelectionActionGroup = new ActionGroup(this.removeAction, this.setTypeAction);
		this.singleSelectionActionGroup.setEnabled(false);

		this.multiSelectionActionGroup = new ActionGroup(this.removeAction, this.setTypeAction);
		this.multiSelectionActionGroup.setEnabled(false);

		this.captureAndPositionActionGroup = new ActionGroup(this.moveCameraToPadLocation, this.moveToolToPadLocation);
		this.captureAndPositionActionGroup.setEnabled(false);

		JComboBox<Side> sidesComboBox = new JComboBox(Side.values());
		JComboBox<Type> typesComboBox = new JComboBox(Type.values());

		this.setLayout(new BorderLayout(0, 0));
		JToolBar toolBar = new JToolBar();
		this.add(toolBar, BorderLayout.NORTH);

		toolBar.setFloatable(false);
		JButton btnNewPad = new JButton(this.newAction);
		btnNewPad.setHideActionText(true);
		toolBar.add(btnNewPad);
		JButton btnRemovePad = new JButton(this.removeAction);
		btnRemovePad.setHideActionText(true);
		toolBar.add(btnRemovePad);
		toolBar.addSeparator();

		JButton btnPositionCameraPositionLocation = new JButton(this.moveCameraToPadLocation);
		btnPositionCameraPositionLocation.setHideActionText(true);
		toolBar.add(btnPositionCameraPositionLocation);

		JButton btnPositionToolPositionLocation = new JButton(this.moveToolToPadLocation);
		btnPositionToolPositionLocation.setHideActionText(true);
		toolBar.add(btnPositionToolPositionLocation);

		toolBar.addSeparator();

		this.tableModel = new PadsTableModel(configuration);

		this.table = new AutoSelectTextTable(this.tableModel);
		this.table.setAutoCreateRowSorter(true);
		this.table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		this.table.setDefaultEditor(Side.class, new DefaultCellEditor(sidesComboBox));

		this.table.setDefaultRenderer(Type.class, new TypeRenderer());
		this.table.setDefaultEditor(Type.class, new DefaultCellEditor(typesComboBox));

		this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;

				if (JobPastePanel.this.getSelections().size() > 1)
				{
					// multi select
					JobPastePanel.this.singleSelectionActionGroup.setEnabled(false);
					JobPastePanel.this.captureAndPositionActionGroup.setEnabled(false);
					JobPastePanel.this.multiSelectionActionGroup.setEnabled(true);
				} else
				{
					// single select, or no select
					JobPastePanel.this.multiSelectionActionGroup.setEnabled(false);
					JobPastePanel.this.singleSelectionActionGroup.setEnabled(JobPastePanel.this.getSelection() != null);
					JobPastePanel.this.captureAndPositionActionGroup
							.setEnabled(JobPastePanel.this.getSelection() != null && JobPastePanel.this.getSelection().getSide() == JobPastePanel.this.boardLocation.getSide());
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
					BoardPad pad = JobPastePanel.this.getSelection();
					pad.setType(pad.getType() == Type.Paste ? Type.Ignore : Type.Paste);
					JobPastePanel.this.tableModel.fireTableRowsUpdated(JobPastePanel.this.table.getSelectedRow(), JobPastePanel.this.table.getSelectedRow());
				} else
					super.keyTyped(e);
			}
		});

		JPopupMenu popupMenu = new JPopupMenu();

		JMenu setTypeMenu = new JMenu(this.setTypeAction);
		for (BoardPad.Type type : BoardPad.Type.values())
			setTypeMenu.add(new SetTypeAction(type));
		popupMenu.add(setTypeMenu);

		JMenu setSideMenu = new JMenu(this.setSideAction);
		for (Board.Side side : Board.Side.values())
			setSideMenu.add(new SetSideAction(side));
		popupMenu.add(setSideMenu);

		this.table.setComponentPopupMenu(popupMenu);

		JScrollPane scrollPane = new JScrollPane(this.table);
		this.add(scrollPane, BorderLayout.CENTER);
	};

	public BoardPad getSelection()
	{
		List<BoardPad> selectedPads = this.getSelections();
		if (selectedPads.isEmpty())
			return null;
		return selectedPads.get(0);
	}

	public List<BoardPad> getSelections()
	{
		ArrayList<BoardPad> rows = new ArrayList<>();
		if (this.boardLocation == null)
			return rows;
		int[] selectedRows = this.table.getSelectedRows();
		for (int selectedRow : selectedRows)
		{
			selectedRow = this.table.convertRowIndexToModel(selectedRow);
			rows.add(this.boardLocation.getBoard().getSolderPastePads().get(selectedRow));
		}
		return rows;
	};

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
