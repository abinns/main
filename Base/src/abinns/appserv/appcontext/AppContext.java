package abinns.appserv.appcontext;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AppContext
{
	private ConcurrentLinkedQueue<Task>	taskQueue;
	private WorkerPool					pool;
	private StatTracker					statTracker;

	public AppContext()
	{
		this.statTracker = new StatTracker();

		this.taskQueue = new ConcurrentLinkedQueue<Task>();
		this.pool = new WorkerPool(4, this.taskQueue);
	}

	public void addTask(Task task)
	{
		this.statTracker.inc("TasksSubmitted");
		this.taskQueue.offer(task);
	}

	public String getStatus()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("App Context");
		sb.append("Stats:\n");
		sb.append(this.statTracker);
		sb.append("Worker Pool with " + this.pool.getWorkerCount() + "\n");
		sb.append(this.pool);
		sb.append("\n");
		return sb.toString();
	}
	
	public String toString()
	{
		return this.getStatus();
	}
}
