package abinns.appserv.appcontext;

public class Stat
{
	int count;
	public Stat()
	{
		this.count = 0;
	}

	public void inc()
	{
		this.count++;
	}
	
	public String toString()
	{
		return this.count + "";
	}

}
