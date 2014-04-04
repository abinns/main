package abinns.appserv.appmanager;

public interface App
{
	public void start(AppContext con);
	
	public void halt();
	
	public String getName();
	
	public String getStatus();
	
	public String getDescription();
}
