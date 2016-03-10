package org.openpnp.machine.reference;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.wizards.ReferenceNozzleTipConfigurationWizard;
import org.openpnp.model.Configuration;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Part;
import org.openpnp.spi.Head;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.NozzleTip;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.spi.base.AbstractNozzleTip;
import org.openpnp.util.UiUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceNozzleTip extends AbstractNozzleTip
{
	private final static Logger logger = LoggerFactory.getLogger(ReferenceNozzleTip.class);

	@ElementList(required = false, entry = "id")
	private Set<String> compatiblePackageIds = new HashSet<>();

	@Attribute(required = false)
	private boolean allowIncompatiblePackages;

	@Element(required = false)
	private Location	changerStartLocation	= new Location(LengthUnit.Millimeters);
	@Element(required = false)
	private Location	changerMidLocation		= new Location(LengthUnit.Millimeters);
	@Element(required = false)
	private Location	changerEndLocation		= new Location(LengthUnit.Millimeters);

	private Set<org.openpnp.model.Package> compatiblePackages = new HashSet<>();

	public Action loadAction = new AbstractAction("Load")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.load);
			this.putValue(Action.NAME, "Load");
			this.putValue(Action.SHORT_DESCRIPTION, "Load the currently selected nozzle tip.");
		}

		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				ReferenceNozzleTip.this.getParentNozzle().loadNozzleTip(ReferenceNozzleTip.this);
			});
		}
	};

	public Action unloadAction = new AbstractAction("Unoad")
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			this.putValue(Action.SMALL_ICON, Icons.unload);
			this.putValue(Action.NAME, "Unload");
			this.putValue(Action.SHORT_DESCRIPTION, "Unoad the currently loaded nozzle tip.");
		}

		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			UiUtils.submitUiMachineTask(() -> {
				ReferenceNozzleTip.this.getParentNozzle().unloadNozzleTip();
			});
		}
	};

	public ReferenceNozzleTip()
	{
		Configuration.get().addListener(new ConfigurationListener.Adapter()
		{
			@Override
			public void configurationLoaded(Configuration configuration) throws Exception
			{
				for (String id : ReferenceNozzleTip.this.compatiblePackageIds)
				{
					org.openpnp.model.Package pkg = configuration.getPackage(id);
					if (pkg == null)
						continue;
					ReferenceNozzleTip.this.compatiblePackages.add(pkg);
				}
			}
		});
	}

	@Override
	public boolean canHandle(Part part)
	{
		boolean result = this.allowIncompatiblePackages || this.compatiblePackages.contains(part.getPackage());
		ReferenceNozzleTip.logger.debug("{}.canHandle({}) => {}", new Object[]
		{ this.getName(), part.getId(), result });
		return result;
	}

	public Location getChangerEndLocation()
	{
		return this.changerEndLocation;
	}

	public Location getChangerMidLocation()
	{
		return this.changerMidLocation;
	}

	public Location getChangerStartLocation()
	{
		return this.changerStartLocation;
	}

	@Override
	public PropertySheetHolder[] getChildPropertySheetHolders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<org.openpnp.model.Package> getCompatiblePackages()
	{
		return new HashSet<>(this.compatiblePackages);
	}

	@Override
	public Wizard getConfigurationWizard()
	{
		return new ReferenceNozzleTipConfigurationWizard(this);
	}

	private Nozzle getParentNozzle()
	{
		for (Head head : Configuration.get().getMachine().getHeads())
			for (Nozzle nozzle : head.getNozzles())
				for (NozzleTip nozzleTip : nozzle.getNozzleTips())
					if (nozzleTip == this)
						return nozzle;
		return null;
	}

	@Override
	public Action[] getPropertySheetHolderActions()
	{
		return new Action[]
		{ this.unloadAction, this.loadAction };
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

	public boolean isAllowIncompatiblePackages()
	{
		return this.allowIncompatiblePackages;
	}

	public void setAllowIncompatiblePackages(boolean allowIncompatiblePackages)
	{
		this.allowIncompatiblePackages = allowIncompatiblePackages;
	}

	public void setChangerEndLocation(Location changerEndLocation)
	{
		this.changerEndLocation = changerEndLocation;
	}

	public void setChangerMidLocation(Location changerMidLocation)
	{
		this.changerMidLocation = changerMidLocation;
	}

	public void setChangerStartLocation(Location changerStartLocation)
	{
		this.changerStartLocation = changerStartLocation;
	}

	public void setCompatiblePackages(Set<org.openpnp.model.Package> compatiblePackages)
	{
		this.compatiblePackages.clear();
		this.compatiblePackages.addAll(compatiblePackages);
		this.compatiblePackageIds.clear();
		for (org.openpnp.model.Package pkg : compatiblePackages)
			this.compatiblePackageIds.add(pkg.getId());
	}

	@Override
	public String toString()
	{
		return this.getName();
	}
}
