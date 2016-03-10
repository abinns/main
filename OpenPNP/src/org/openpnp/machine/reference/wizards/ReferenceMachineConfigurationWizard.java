package org.openpnp.machine.reference.wizards;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.machine.openbuilds.OpenBuildsDriver;
import org.openpnp.machine.reference.ReferenceDriver;
import org.openpnp.machine.reference.ReferenceMachine;
import org.openpnp.machine.reference.driver.GrblDriver;
import org.openpnp.machine.reference.driver.LinuxCNC;
import org.openpnp.machine.reference.driver.MarlinDriver;
import org.openpnp.machine.reference.driver.NullDriver;
import org.openpnp.machine.reference.driver.SimulatorDriver;
import org.openpnp.machine.reference.driver.SprinterDriver;
import org.openpnp.machine.reference.driver.TinygDriver;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class ReferenceMachineConfigurationWizard extends AbstractConfigurationWizard
{

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1L;
	final private ReferenceMachine	machine;
	private JComboBox				comboBoxDriver;
	private String					driverClassName;

	public ReferenceMachineConfigurationWizard(ReferenceMachine machine)
	{
		this.machine = machine;
		this.contentPanel.setLayout(new FormLayout(new ColumnSpec[]
		{ FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[]
		{ FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblDriver = new JLabel("Driver");
		this.contentPanel.add(lblDriver, "2, 2, right, default");

		this.comboBoxDriver = new JComboBox();
		this.contentPanel.add(this.comboBoxDriver, "4, 2, fill, default");

		this.comboBoxDriver.addItem(NullDriver.class.getCanonicalName());
		this.comboBoxDriver.addItem(GrblDriver.class.getCanonicalName());
		this.comboBoxDriver.addItem(LinuxCNC.class.getCanonicalName());
		this.comboBoxDriver.addItem(MarlinDriver.class.getCanonicalName());
		this.comboBoxDriver.addItem(SimulatorDriver.class.getCanonicalName());
		this.comboBoxDriver.addItem(SprinterDriver.class.getCanonicalName());
		this.comboBoxDriver.addItem(TinygDriver.class.getCanonicalName());
		this.comboBoxDriver.addItem(OpenBuildsDriver.class.getCanonicalName());
		this.comboBoxDriver.addItem(org.firepick.driver.MarlinDriver.class.getCanonicalName());
		this.comboBoxDriver.addItem(org.firepick.driver.FireStepDriver.class.getCanonicalName());

		this.driverClassName = machine.getDriver().getClass().getCanonicalName();
	}

	@Override
	public void createBindings()
	{
		this.addWrappedBinding(this, "driverClassName", this.comboBoxDriver, "selectedItem");
	}

	public String getDriverClassName()
	{
		return this.driverClassName;
	}

	@Override
	protected void saveToModel()
	{
		super.saveToModel();
		MessageBoxes.errorBox(this.getTopLevelAncestor(), "Restart Required", "Please restart OpenPnP for the changes to take effect.");
	}

	public void setDriverClassName(String driverClassName) throws Exception
	{
		ReferenceDriver driver = (ReferenceDriver) Class.forName(driverClassName).newInstance();
		this.machine.setDriver(driver);
		this.driverClassName = driverClassName;
	}
}
