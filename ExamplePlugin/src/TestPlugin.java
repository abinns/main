import abinns.appserv.appcontext.AppContext;
import abinns.appserv.appmanager.App;


public class TestPlugin implements App
{

	public void start(AppContext con)
	{
		new OtherPart(con);
	}

	@Override
	public void halt()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
