package org.openpnp.machine.reference.driver.wizards;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.machine.reference.driver.AbstractSerialPortDriver;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class AbstractSerialPortDriverConfigurationWizard extends AbstractConfigurationWizard
{
	/**
	 * 
	 */
	private static final long				serialVersionUID	= 1L;
	private final AbstractSerialPortDriver	driver;
	private JComboBox						comboBoxPort;
	private JComboBox						comboBoxBaud;

	public AbstractSerialPortDriverConfigurationWizard(AbstractSerialPortDriver driver)
	{
		this.driver = driver;

		this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		this.contentPanel.add(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblPortName = new JLabel("Port");
		panel.add(lblPortName, "2, 2, right, default");

		this.comboBoxPort = new JComboBox();
		panel.add(this.comboBoxPort, "4, 2, fill, default");

		JLabel lblBaudRate = new JLabel("Baud");
		panel.add(lblBaudRate, "2, 4, right, default");

		this.comboBoxBaud = new JComboBox();
		panel.add(this.comboBoxBaud, "4, 4, fill, default");

		this.comboBoxBaud.addItem(new Integer(110));
		this.comboBoxBaud.addItem(new Integer(300));
		this.comboBoxBaud.addItem(new Integer(600));
		this.comboBoxBaud.addItem(new Integer(1200));
		this.comboBoxBaud.addItem(new Integer(2400));
		this.comboBoxBaud.addItem(new Integer(4800));
		this.comboBoxBaud.addItem(new Integer(9600));
		this.comboBoxBaud.addItem(new Integer(14400));
		this.comboBoxBaud.addItem(new Integer(19200));
		this.comboBoxBaud.addItem(new Integer(38400));
		this.comboBoxBaud.addItem(new Integer(56000));
		this.comboBoxBaud.addItem(new Integer(57600));
		this.comboBoxBaud.addItem(new Integer(115200));
		this.comboBoxBaud.addItem(new Integer(128000));
		this.comboBoxBaud.addItem(new Integer(153600));
		this.comboBoxBaud.addItem(new Integer(230400));
		this.comboBoxBaud.addItem(new Integer(250000));
		this.comboBoxBaud.addItem(new Integer(256000));
		this.comboBoxBaud.addItem(new Integer(460800));
		this.comboBoxBaud.addItem(new Integer(921600));

		this.comboBoxPort.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(PopupMenuEvent e)
			{
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				AbstractSerialPortDriverConfigurationWizard.this.refreshPortList();
			}
		});

		this.refreshPortList();
	}

	@Override
	public void createBindings()
	{
		IntegerConverter integerConverter = new IntegerConverter();

		this.addWrappedBinding(this.driver, "portName", this.comboBoxPort, "selectedItem");
		this.addWrappedBinding(this.driver, "baud", this.comboBoxBaud, "selectedItem");
	}

	private void refreshPortList()
	{
		if (this.driver != null)
		{
			this.comboBoxPort.removeAllItems();
			boolean exists = false;
			String[] portNames = this.driver.getPortNames();
			for (String portName : portNames)
			{
				this.comboBoxPort.addItem(portName);
				if (portName.equals(this.driver.getPortName()))
					exists = true;
			}
			if (!exists && this.driver.getPortName() != null)
				this.comboBoxPort.addItem(this.driver.getPortName());
		}
	}
}
