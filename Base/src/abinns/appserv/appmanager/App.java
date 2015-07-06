package abinns.appserv.appmanager;

import abinns.appserv.appcontext.AppContext;

public interface App
{
	public String getDescription();

	public String getName();

	public String getStatus();

	public void halt();

	public void start(AppContext con);
}
