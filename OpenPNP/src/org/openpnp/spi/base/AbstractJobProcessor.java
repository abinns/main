package org.openpnp.spi.base;

import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Icon;

import org.openpnp.JobProcessorDelegate;
import org.openpnp.JobProcessorListener;
import org.openpnp.gui.support.PropertySheetWizardAdapter;
import org.openpnp.model.BoardLocation;
import org.openpnp.model.Job;
import org.openpnp.model.Part;
import org.openpnp.model.Placement;
import org.openpnp.spi.Feeder;
import org.openpnp.spi.JobProcessor;
import org.openpnp.spi.PropertySheetHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJobProcessor implements JobProcessor, Runnable
{
	class DefaultJobProcessorDelegate implements JobProcessorDelegate
	{
		@Override
		public PickRetryAction partPickFailed(BoardLocation board, Part part, Feeder feeder)
		{
			return PickRetryAction.SkipAndContinue;
		}
	}

	private static final Logger			logger		= LoggerFactory.getLogger(AbstractJobProcessor.class);
	protected Job						job;
	protected Set<JobProcessorListener>	listeners	= new HashSet<>();
	protected JobProcessorDelegate		delegate	= new DefaultJobProcessorDelegate();
	protected JobState					state;
	protected Thread					thread;
	protected Object					runLock		= new Object();

	private boolean pauseAtNextStep;

	@Override
	public void addListener(JobProcessorListener listener)
	{
		this.listeners.add(listener);
	}

	protected void fireDetailedStatusUpdated(String status)
	{
		AbstractJobProcessor.logger.debug("fireDetailedStatusUpdated({})", status);
		for (JobProcessorListener listener : this.listeners)
			listener.detailedStatusUpdated(status);
	}

	protected void fireJobEncounteredError(JobError error, String description)
	{
		AbstractJobProcessor.logger.debug("fireJobEncounteredError({}, {})", error, description);
		for (JobProcessorListener listener : this.listeners)
			listener.jobEncounteredError(error, description);
	}

	private void fireJobLoaded()
	{
		AbstractJobProcessor.logger.debug("fireJobLoaded()");
		for (JobProcessorListener listener : this.listeners)
			listener.jobLoaded(this.job);
	}

	protected void fireJobStateChanged()
	{
		AbstractJobProcessor.logger.debug("fireJobStateChanged({})", this.state);
		for (JobProcessorListener listener : this.listeners)
			listener.jobStateChanged(this.state);
	}

	protected void firePartPicked(BoardLocation board, Placement placement)
	{
		AbstractJobProcessor.logger.debug("firePartPicked({}, {})", board, placement);
		for (JobProcessorListener listener : this.listeners)
			listener.partPicked(board, placement);
	}

	protected void firePartPlaced(BoardLocation board, Placement placement)
	{
		AbstractJobProcessor.logger.debug("firePartPlaced({}, {})", board, placement);
		for (JobProcessorListener listener : this.listeners)
			listener.partPlaced(board, placement);
	}

	protected void firePartProcessingComplete(BoardLocation board, Placement placement)
	{
		AbstractJobProcessor.logger.debug("firePartProcessingComplete({}, {})", board, placement);
		for (JobProcessorListener listener : this.listeners)
			listener.partProcessingCompleted(board, placement);
	}

	protected void firePartProcessingStarted(BoardLocation board, Placement placement)
	{
		AbstractJobProcessor.logger.debug("firePartProcessingStarted({}, {})", board, placement);
		for (JobProcessorListener listener : this.listeners)
			listener.partProcessingStarted(board, placement);
	}

	@Override
	public PropertySheetHolder[] getChildPropertySheetHolders()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Job getJob()
	{
		return this.job;
	}

	@Override
	public Action[] getPropertySheetHolderActions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Icon getPropertySheetHolderIcon()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPropertySheetHolderTitle()
	{
		return this.getClass().getSimpleName();
	}

	@Override
	public PropertySheet[] getPropertySheets()
	{
		return new PropertySheet[]
		{ new PropertySheetWizardAdapter(this.getConfigurationWizard()) };
	}

	@Override
	public JobState getState()
	{
		return this.state;
	}

	// TODO: Change this, and most of the other properties on here to bound
	// properties.
	@Override
	public void load(Job job)
	{
		this.stop();
		this.job = job;

		this.fireJobLoaded();
	}

	@Override
	public void pause()
	{
		AbstractJobProcessor.logger.debug("pause()");
		this.state = JobState.Paused;
		this.fireJobStateChanged();
	}

	@Override
	public void removeListener(JobProcessorListener listener)
	{
		this.listeners.remove(listener);
	}

	@Override
	public void resume()
	{
		AbstractJobProcessor.logger.debug("resume()");
		this.state = JobState.Running;
		this.fireJobStateChanged();
		synchronized (this.runLock)
		{
			this.runLock.notifyAll();
		}
	}

	@Override
	public void setDelegate(JobProcessorDelegate delegate)
	{
		if (delegate == null)
			this.delegate = new DefaultJobProcessorDelegate();
		else
			this.delegate = delegate;
	}

	/**
	 * Checks if the Job has been Paused or Stopped. If it has been Paused this
	 * method blocks until the Job is Resumed. If the Job has been Stopped it
	 * returns false and the loop should break.
	 */
	protected boolean shouldJobProcessingContinue()
	{
		if (this.pauseAtNextStep)
		{
			this.pauseAtNextStep = false;
			this.pause();
		}
		while (true)
			if (this.state == JobState.Stopped)
				return false;
			else if (this.state == JobState.Paused)
				synchronized (this.runLock)
				{
					try
					{
						this.runLock.wait();
					} catch (InterruptedException ie)
					{
						throw new Error(ie);
					}
				}
			else
				break;
		return true;
	}

	@Override
	public void start() throws Exception
	{
		AbstractJobProcessor.logger.debug("start()");
		if (this.state != JobState.Stopped)
			throw new Exception("Invalid state. Cannot start new job while state is " + this.state);
		if (this.thread != null && this.thread.isAlive())
			throw new Exception("Previous Job has not yet finished.");
		this.thread = new Thread(this);
		this.thread.start();
	}

	@Override
	public void step() throws Exception
	{
		AbstractJobProcessor.logger.debug("step()");
		if (this.state == JobState.Stopped)
		{
			this.pauseAtNextStep = true;
			this.start();
		} else
		{
			this.pauseAtNextStep = true;
			this.resume();
		}
	}

	@Override
	public void stop()
	{
		AbstractJobProcessor.logger.debug("stop()");
		this.state = JobState.Stopped;
		this.fireJobStateChanged();
		synchronized (this.runLock)
		{
			this.runLock.notifyAll();
		}
	}
}
