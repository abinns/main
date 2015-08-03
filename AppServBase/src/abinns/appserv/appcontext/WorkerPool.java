package abinns.appserv.appcontext;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorkerPool
{

	private int							workerCount	= 0;
	private ConcurrentLinkedQueue<Task>	queue;
	private LinkedList<WorkerThread>	workers;

	public WorkerPool(int i, ConcurrentLinkedQueue<Task> taskQueue)
	{
		this.queue = taskQueue;
		this.workers = new LinkedList<WorkerThread>();
		this.setWorkerCount(i);
	}

	public void setWorkerCount(int newCount)
	{
		if (this.workerCount < newCount)
			for (int i = this.workerCount; i <= newCount; i++)
				this.workers.add(new WorkerThread(this.queue, "WkrThread " + i));
		else
			for (int i = this.workerCount; i > newCount; i--)
				this.workers.removeLast().end();
		this.workerCount = newCount;
	}

	public int getWorkerCount()
	{
		return this.workerCount;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (WorkerThread cur : this.workers)
			sb.append(cur + "\n");
		return sb.toString();
	}

}
