package org.openpnp.spi.base;

import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.openpnp.model.Configuration;
import org.openpnp.spi.Actuator;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Head;
import org.openpnp.spi.Machine;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.PasteDispenser;
import org.openpnp.util.IdentifiableList;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.core.Commit;

public abstract class AbstractHead implements Head
{
	protected Machine machine;

	@Attribute
	protected String id;

	@Attribute(required = false)
	protected String name;

	@ElementList(required = false)
	protected IdentifiableList<Nozzle> nozzles = new IdentifiableList<>();

	@ElementList(required = false)
	protected IdentifiableList<Actuator> actuators = new IdentifiableList<>();

	@ElementList(required = false)
	protected IdentifiableList<Camera> cameras = new IdentifiableList<>();

	@ElementList(required = false)
	protected IdentifiableList<PasteDispenser> pasteDispensers = new IdentifiableList<>();

	public AbstractHead()
	{
		this.id = Configuration.createId();
		this.name = this.getClass().getSimpleName();
	}

	@Override
	public void addCamera(Camera camera) throws Exception
	{
		this.cameras.add(camera);
	}

	@SuppressWarnings("unused")
	@Commit
	private void commit()
	{
		for (Nozzle nozzle : this.nozzles)
			nozzle.setHead(this);
		for (Camera camera : this.cameras)
			camera.setHead(this);
		for (Actuator actuator : this.actuators)
			actuator.setHead(this);
		for (PasteDispenser pasteDispenser : this.pasteDispensers)
			pasteDispenser.setHead(this);
	}

	@Override
	public Actuator getActuator(String id)
	{
		return this.actuators.get(id);
	}

	@Override
	public Actuator getActuatorByName(String name)
	{
		for (Actuator actuator : this.actuators)
			if (actuator.getName().equals(name))
				return actuator;
		return null;
	}

	@Override
	public List<Actuator> getActuators()
	{
		return Collections.unmodifiableList(this.actuators);
	}

	@Override
	public Camera getCamera(String id)
	{
		return this.cameras.get(id);
	}

	@Override
	public List<Camera> getCameras()
	{
		return Collections.unmodifiableList(this.cameras);
	}

	@Override
	public Camera getDefaultCamera() throws Exception
	{
		List<Camera> cameras = this.getCameras();
		if (cameras == null || cameras.isEmpty())
			throw new Exception("No default camera available on head " + this.getName());
		return cameras.get(0);
	}

	@Override
	public Nozzle getDefaultNozzle() throws Exception
	{
		List<Nozzle> nozzles = this.getNozzles();
		if (nozzles == null || nozzles.isEmpty())
			throw new Exception("No default nozzle available on head " + this.getName());
		return nozzles.get(0);
	}

	@Override
	public PasteDispenser getDefaultPasteDispenser() throws Exception
	{
		List<PasteDispenser> dispensers = this.getPasteDispensers();
		if (dispensers == null || dispensers.isEmpty())
			throw new Exception("No default paste dispenser available on head " + this.getName());
		return dispensers.get(0);
	}

	@Override
	public String getId()
	{
		return this.id;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public Nozzle getNozzle(String id)
	{
		return this.nozzles.get(id);
	}

	@Override
	public List<Nozzle> getNozzles()
	{
		return Collections.unmodifiableList(this.nozzles);
	}

	@Override
	public PasteDispenser getPasteDispenser(String id)
	{
		return this.pasteDispensers.get(id);
	}

	@Override
	public List<PasteDispenser> getPasteDispensers()
	{
		return Collections.unmodifiableList(this.pasteDispensers);
	}

	@Override
	public Icon getPropertySheetHolderIcon()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveToSafeZ(double speed) throws Exception
	{
		for (Nozzle nozzle : this.nozzles)
			nozzle.moveToSafeZ(speed);
		for (Camera camera : this.cameras)
			camera.moveToSafeZ(speed);
		for (Actuator actuator : this.actuators)
			actuator.moveToSafeZ(speed);
		for (PasteDispenser dispenser : this.pasteDispensers)
			dispenser.moveToSafeZ(speed);
	}

	@Override
	public void removeCamera(Camera camera)
	{
		this.cameras.remove(camera);
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}
}
