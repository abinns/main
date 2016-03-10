package org.openpnp.spi.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;

import org.openpnp.spi.Actuator;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Feeder;
import org.openpnp.spi.Head;
import org.openpnp.spi.JobPlanner;
import org.openpnp.spi.JobProcessor;
import org.openpnp.spi.JobProcessor.Type;
import org.openpnp.spi.Machine;
import org.openpnp.spi.MachineListener;
import org.openpnp.util.IdentifiableList;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.core.Commit;

import com.google.common.util.concurrent.FutureCallback;

public abstract class AbstractMachine implements Machine
{
	/**
	 * History: Note: Can't actually use the @Version annotation because of a
	 * bug in SimpleXML. See
	 * http://sourceforge.net/p/simple/mailman/message/27887562/ 1.0: Initial
	 * revision. 1.1: Added jobProcessors Map and deprecated JobProcesor and
	 * JobPlanner.
	 */

	@ElementList
	protected IdentifiableList<Head> heads = new IdentifiableList<>();

	@ElementList(required = false)
	protected IdentifiableList<Feeder> feeders = new IdentifiableList<>();

	@ElementList(required = false)
	protected IdentifiableList<Camera> cameras = new IdentifiableList<>();

	@ElementList(required = false)
	protected IdentifiableList<Actuator> actuators = new IdentifiableList<>();

	@Deprecated
	@Element(required = false)
	protected JobPlanner jobPlanner;

	@Deprecated
	@Element(required = false)
	protected JobProcessor jobProcessor;

	@ElementMap(entry = "jobProcessor", key = "type", attribute = true, inline = false, required = false)
	protected Map<JobProcessor.Type, JobProcessor> jobProcessors = new HashMap<>();

	protected Set<MachineListener> listeners = Collections.synchronizedSet(new HashSet<>());

	protected ThreadPoolExecutor executor;

	protected AbstractMachine()
	{
	}

	@Override
	public void addCamera(Camera camera) throws Exception
	{
		this.cameras.add(camera);
	}

	@Override
	public void addFeeder(Feeder feeder) throws Exception
	{
		this.feeders.add(feeder);
	}

	@Override
	public void addListener(MachineListener listener)
	{
		this.listeners.add(listener);
	}

	@SuppressWarnings("unused")
	@Commit
	private void commit()
	{
		if (this.jobProcessors.isEmpty())
		{
			this.jobProcessors.put(JobProcessor.Type.PickAndPlace, this.jobProcessor);
			this.jobProcessor = null;
			this.jobPlanner = null;
		}
	}

	public void fireMachineBusy(boolean busy)
	{
		for (MachineListener listener : this.listeners)
			listener.machineBusy(this, busy);
	}

	public void fireMachineDisabled(String reason)
	{
		for (MachineListener listener : this.listeners)
			listener.machineDisabled(this, reason);
	}

	public void fireMachineDisableFailed(String reason)
	{
		for (MachineListener listener : this.listeners)
			listener.machineDisableFailed(this, reason);
	}

	public void fireMachineEnabled()
	{
		for (MachineListener listener : this.listeners)
			listener.machineEnabled(this);
	}

	public void fireMachineEnableFailed(String reason)
	{
		for (MachineListener listener : this.listeners)
			listener.machineEnableFailed(this, reason);
	}

	public void fireMachineHeadActivity(Head head)
	{
		for (MachineListener listener : this.listeners)
			listener.machineHeadActivity(this, head);
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
	public Head getDefaultHead() throws Exception
	{
		List<Head> heads = this.getHeads();
		if (heads == null || heads.isEmpty())
			throw new Exception("No default head available.");
		return heads.get(0);
	}

	@Override
	public Feeder getFeeder(String id)
	{
		return this.feeders.get(id);
	}

	@Override
	public List<Feeder> getFeeders()
	{
		return Collections.unmodifiableList(this.feeders);
	}

	@Override
	public Head getHead(String id)
	{
		return this.heads.get(id);
	}

	@Override
	public List<Head> getHeads()
	{
		return Collections.unmodifiableList(this.heads);
	}

	@Override
	public Map<Type, JobProcessor> getJobProcessors()
	{
		return Collections.unmodifiableMap(this.jobProcessors);
	}

	@Override
	public Icon getPropertySheetHolderIcon()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void home() throws Exception
	{
		for (Head head : this.heads)
			head.home();
	}

	@Override
	public void removeCamera(Camera camera)
	{
		this.cameras.remove(camera);
	}

	@Override
	public void removeFeeder(Feeder feeder)
	{
		this.feeders.remove(feeder);
	}

	@Override
	public void removeListener(MachineListener listener)
	{
		this.listeners.remove(listener);
	}

	@Override
	public <T> Future<T> submit(Callable<T> callable)
	{
		return this.submit(callable, null);
	}

	@Override
	public <T> Future<T> submit(final Callable<T> callable, final FutureCallback<T> callback)
	{
		return this.submit(callable, callback, false);
	}

	@Override
	public <T> Future<T> submit(final Callable<T> callable, final FutureCallback<T> callback, final boolean ignoreEnabled)
	{
		synchronized (this)
		{
			if (this.executor == null || this.executor.isShutdown())
				this.executor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
		}

		Callable<T> wrapper = new Callable<T>()
		{
			@Override
			public T call() throws Exception
			{
				// TODO: lock driver

				// Notify listeners that the machine is now busy
				AbstractMachine.this.fireMachineBusy(true);

				// Call the task, storing the result and exception if any
				T result = null;
				Exception exception = null;
				try
				{
					if (!ignoreEnabled && !AbstractMachine.this.isEnabled())
						throw new Exception("Machine has not been started.");
					result = callable.call();
				} catch (Exception e)
				{
					exception = e;
				}

				// If a callback was supplied, call it with the results
				if (callback != null)
					if (exception != null)
						callback.onFailure(exception);
					else
						callback.onSuccess(result);

				// If there was an error cancel all pending tasks.
				if (exception != null)
					AbstractMachine.this.executor.shutdownNow();

				// TODO: unlock driver

				// If no more tasks are scheduled notify listeners that
				// the machine is no longer busy
				if (AbstractMachine.this.executor.getQueue().isEmpty())
					AbstractMachine.this.fireMachineBusy(false);

				// Finally, fulfill the Future by either throwing the
				// exception or returning the result.
				if (exception != null)
					throw exception;
				return result;
			}
		};

		return this.executor.submit(wrapper);
	}

	@Override
	public Future<Object> submit(Runnable runnable)
	{
		return this.submit(Executors.callable(runnable));
	}
}
