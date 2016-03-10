package org.openpnp.machine.reference;

import java.util.ArrayList;

import javax.swing.Action;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.wizards.ReferenceNozzleConfigurationWizard;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.spi.NozzleTip;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.base.AbstractNozzle;
import org.openpnp.spi.base.SimplePropertySheetHolder;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceNozzle extends AbstractNozzle implements ReferenceHeadMountable
{
	private final static Logger logger = LoggerFactory.getLogger(ReferenceNozzle.class);

	@Element
	private Location headOffsets;

	@Attribute(required = false)
	private int pickDwellMilliseconds;

	@Attribute(required = false)
	private int placeDwellMilliseconds;

	@Attribute(required = false)
	private String currentNozzleTipId;

	@Attribute(required = false)
	private boolean changerEnabled = false;

	@Element(required = false)
	protected Length safeZ = new Length(0, LengthUnit.Millimeters);

	/**
	 * If limitRotation is enabled the nozzle will reverse directions when
	 * commanded to rotate past 180 degrees. So, 190 degrees becomes -170 and
	 * -190 becomes 170.
	 */
	@Attribute(required = false)
	private boolean limitRotation = true;

	protected NozzleTip nozzleTip;

	protected ReferenceMachine	machine;
	protected ReferenceDriver	driver;

	public ReferenceNozzle()
	{
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationLoaded(Configuration configuration) throws Exception
			{
				ReferenceNozzle.this.machine = (ReferenceMachine) configuration.getMachine();
				ReferenceNozzle.this.driver = ReferenceNozzle.this.machine.getDriver();
				ReferenceNozzle.this.nozzleTip = ReferenceNozzle.this.nozzleTips.get(ReferenceNozzle.this.currentNozzleTipId);
			}
		});
	}

	@Override
	public PropertySheetHolder[] getChildPropertySheetHolders()
	{
		ArrayList<PropertySheetHolder> children = new ArrayList<>();
		children.add(new SimplePropertySheetHolder("Nozzle Tips", this.getNozzleTips()));
		return children.toArray(new PropertySheetHolder[]
		{});
	}

	@Override
	public Wizard getConfigurationWizard()
	{
		return new ReferenceNozzleConfigurationWizard(this);
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
	public NozzleTip getNozzleTip()
	{
		return this.nozzleTip;
	}

	public int getPickDwellMilliseconds()
	{
		return this.pickDwellMilliseconds;
	}

	public int getPlaceDwellMilliseconds()
	{
		return this.placeDwellMilliseconds;
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

	public boolean isChangerEnabled()
	{
		return this.changerEnabled;
	}

	public boolean isLimitRotation()
	{
		return this.limitRotation;
	}

	@Override
	public void loadNozzleTip(NozzleTip nozzleTip) throws Exception
	{
		if (this.nozzleTip == nozzleTip)
			return;
		if (!this.changerEnabled)
			throw new Exception("Can't load nozzle tip, nozzle tip changer is not enabled.");
		this.unloadNozzleTip();
		ReferenceNozzle.logger.debug("{}.loadNozzleTip({}): Start", new Object[]
		{ this.getName(), nozzleTip.getName() });
		ReferenceNozzleTip nt = (ReferenceNozzleTip) nozzleTip;
		ReferenceNozzle.logger.debug("{}.loadNozzleTip({}): moveToSafeZ", new Object[]
		{ this.getName(), nozzleTip.getName() });
		this.moveToSafeZ(1.0);
		ReferenceNozzle.logger.debug("{}.loadNozzleTip({}): moveTo Start Location", new Object[]
		{ this.getName(), nozzleTip.getName() });
		this.moveTo(nt.getChangerStartLocation(), 1.0);
		ReferenceNozzle.logger.debug("{}.loadNozzleTip({}): moveTo Mid Location", new Object[]
		{ this.getName(), nozzleTip.getName() });
		this.moveTo(nt.getChangerMidLocation(), 0.25);
		ReferenceNozzle.logger.debug("{}.loadNozzleTip({}): moveTo End Location", new Object[]
		{ this.getName(), nozzleTip.getName() });
		this.moveTo(nt.getChangerEndLocation(), 1.0);
		this.moveToSafeZ(1.0);
		ReferenceNozzle.logger.debug("{}.loadNozzleTip({}): Finished", new Object[]
		{ this.getName(), nozzleTip.getName() });
		this.nozzleTip = nozzleTip;
		this.currentNozzleTipId = nozzleTip.getId();
	}

	@Override
	public void moveTo(Location location, double speed) throws Exception
	{
		ReferenceNozzle.logger.debug("{}.moveTo({}, {})", new Object[]
		{ this.getName(), location, speed });
		if (this.limitRotation && !Double.isNaN(location.getRotation()) && Math.abs(location.getRotation()) > 180)
			if (location.getRotation() < 0)
				location = location.derive(null, null, null, location.getRotation() + 360);
			else
				location = location.derive(null, null, null, location.getRotation() - 360);
		this.driver.moveTo(this, location, speed);
		this.machine.fireMachineHeadActivity(this.head);
	}

	@Override
	public void moveToSafeZ(double speed) throws Exception
	{
		ReferenceNozzle.logger.debug("{}.moveToSafeZ({})", new Object[]
		{ this.getName(), speed });
		Length safeZ = this.safeZ.convertToUnits(this.getLocation().getUnits());
		Location l = new Location(this.getLocation().getUnits(), Double.NaN, Double.NaN, safeZ.getValue(), Double.NaN);
		this.driver.moveTo(this, l, speed);
		this.machine.fireMachineHeadActivity(this.head);
	}

	@Override
	public void pick() throws Exception
	{
		ReferenceNozzle.logger.debug("{}.pick()", this.getName());
		if (this.nozzleTip == null)
			throw new Exception("Can't pick, no nozzle tip loaded");
		this.driver.pick(this);
		this.machine.fireMachineHeadActivity(this.head);
		Thread.sleep(this.pickDwellMilliseconds);
	}

	@Override
	public void place() throws Exception
	{
		ReferenceNozzle.logger.debug("{}.place()", this.getName());
		if (this.nozzleTip == null)
			throw new Exception("Can't place, no nozzle tip loaded");
		this.driver.place(this);
		this.machine.fireMachineHeadActivity(this.head);
		Thread.sleep(this.placeDwellMilliseconds);
	}

	public void setChangerEnabled(boolean changerEnabled)
	{
		this.changerEnabled = changerEnabled;
	}

	@Override
	public void setHeadOffsets(Location headOffsets)
	{
		this.headOffsets = headOffsets;
	}

	public void setLimitRotation(boolean limitRotation)
	{
		this.limitRotation = limitRotation;
	}

	public void setPickDwellMilliseconds(int pickDwellMilliseconds)
	{
		this.pickDwellMilliseconds = pickDwellMilliseconds;
	}

	public void setPlaceDwellMilliseconds(int placeDwellMilliseconds)
	{
		this.placeDwellMilliseconds = placeDwellMilliseconds;
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

	@Override
	public void unloadNozzleTip() throws Exception
	{
		if (this.nozzleTip == null)
			return;
		if (!this.changerEnabled)
			throw new Exception("Can't unload nozzle tip, nozzle tip changer is not enabled.");
		ReferenceNozzle.logger.debug("{}.unloadNozzleTip(): Start", new Object[]
		{ this.getName() });
		ReferenceNozzleTip nt = (ReferenceNozzleTip) this.nozzleTip;
		ReferenceNozzle.logger.debug("{}.unloadNozzleTip(): moveToSafeZ", new Object[]
		{ this.getName() });
		this.moveToSafeZ(1.0);
		ReferenceNozzle.logger.debug("{}.unloadNozzleTip(): moveTo End Location", new Object[]
		{ this.getName() });
		this.moveTo(nt.getChangerEndLocation(), 1.0);
		ReferenceNozzle.logger.debug("{}.unloadNozzleTip(): moveTo Mid Location", new Object[]
		{ this.getName() });
		this.moveTo(nt.getChangerMidLocation(), 1.0);
		ReferenceNozzle.logger.debug("{}.unloadNozzleTip(): moveTo Start Location", new Object[]
		{ this.getName() });
		this.moveTo(nt.getChangerStartLocation(), 0.25);
		this.moveToSafeZ(1.0);
		ReferenceNozzle.logger.debug("{}.unloadNozzleTip(): Finished", new Object[]
		{ this.getName() });
		this.nozzleTip = null;
		this.currentNozzleTipId = null;
	}
}
