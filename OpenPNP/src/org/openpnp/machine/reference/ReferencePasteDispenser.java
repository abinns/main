package org.openpnp.machine.reference;

import javax.swing.Action;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.wizards.ReferencePasteDispenserConfigurationWizard;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.base.AbstractPasteDispenser;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferencePasteDispenser extends AbstractPasteDispenser implements ReferenceHeadMountable
{
	private final static Logger logger = LoggerFactory.getLogger(ReferencePasteDispenser.class);

	@Element
	private Location headOffsets;

	@Element(required = false)
	protected Length safeZ = new Length(0, LengthUnit.Millimeters);

	protected ReferenceMachine	machine;
	protected ReferenceDriver	driver;

	public ReferencePasteDispenser()
	{
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationLoaded(Configuration configuration) throws Exception
			{
				ReferencePasteDispenser.this.machine = (ReferenceMachine) configuration.getMachine();
				ReferencePasteDispenser.this.driver = ReferencePasteDispenser.this.machine.getDriver();
			}
		});
	}

	@Override
	public void dispense(Location startLocation, Location endLocation, long dispenseTimeMilliseconds) throws Exception
	{
		ReferencePasteDispenser.logger.debug("{}.dispense()", this.getName());
		Thread.sleep(dispenseTimeMilliseconds);
		this.driver.dispense(this, startLocation, endLocation, dispenseTimeMilliseconds);
		this.machine.fireMachineHeadActivity(this.head);
	}

	@Override
	public PropertySheetHolder[] getChildPropertySheetHolders()
	{
		return null;
	}

	@Override
	public Wizard getConfigurationWizard()
	{
		return new ReferencePasteDispenserConfigurationWizard(this);
	}

	@Override
	public Location getHeadOffsets()
	{
		return this.headOffsets;
	}

	@Override
	public Location getLocation()
	{
		return this.driver.getLocation(this);
	}

	@Override
	public Action[] getPropertySheetHolderActions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPropertySheetHolderTitle()
	{
		return this.getClass().getSimpleName() + " " + this.getName();
	}

	@Override
	public PropertySheet[] getPropertySheets()
	{
		return new PropertySheet[]
		{ new PropertySheetWizardAdapter(this.getConfigurationWizard()) };
	}

	public Length getSafeZ()
	{
		return this.safeZ;
	}

	@Override
	public void moveTo(Location location, double speed) throws Exception
	{
		ReferencePasteDispenser.logger.debug("{}.moveTo({}, {})", new Object[]
		{ this.getName(), location, speed });
		this.driver.moveTo(this, location, speed);
		this.machine.fireMachineHeadActivity(this.head);
	}

	@Override
	public void moveToSafeZ(double speed) throws Exception
	{
		ReferencePasteDispenser.logger.debug("{}.moveToSafeZ({})", new Object[]
		{ this.getName(), speed });
		Length safeZ = this.safeZ.convertToUnits(this.getLocation().getUnits());
		Location l = new Location(this.getLocation().getUnits(), Double.NaN, Double.NaN, safeZ.getValue(), Double.NaN);
		this.driver.moveTo(this, l, speed);
		this.machine.fireMachineHeadActivity(this.head);
	}

	@Override
	public void setHeadOffsets(Location headOffsets)
	{
		this.headOffsets = headOffsets;
	}

	public void setSafeZ(Length safeZ)
	{
		this.safeZ = safeZ;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}
}
