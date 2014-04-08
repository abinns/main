package abinns.appserv.appcontext;

import java.util.concurrent.ConcurrentLinkedQueue;

public class WorkerThread implements Runnable
{

	private Thread						thread;
	private ConcurrentLinkedQueue<Task>	queue;
	private boolean						isRunning;
	private String	name;

	public WorkerThread(ConcurrentLinkedQueue<Task> queue, String name)
	{
		this.name = name;
		this.queue = queue;
		this.thread = new Thread(this);
		this.thread.setPriority(Thread.MIN_PRIORITY);
		this.thread.setDaemon(true);

		this.isRunning = true;

		this.thread.start();
	}

	@Override
	public void run()
	{
		while (this.isRunning)
			while (!this.queue.isEmpty())
				this.queue.poll().doTask();
	}

	public void end()
	{
		this.isRunning = false;
	}

	public String toString()
	{
		if (this.thread.isAlive() && this.isRunning)
			return this.name + " - Running";
		if (this.thread.isAlive() && !this.isRunning)
			return this.name + " - Finishing";
		if (!this.thread.isAlive() && !this.isRunning)
			return this.name + " - Dead";
		return this.name + " - Error";
	}

}
