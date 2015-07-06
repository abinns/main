import abinns.appserv.appcontext.AppContext;
import abinns.appserv.appmanager.App;

public class SnmpMon implements App
{

	@Override
	public String getDescription()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName()
	{
		return "SNMP Monitor";
	}

	@Override
	public String getStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void halt()
	{
		// TODO Auto-generated method stub

	}
	@Override
	public void start(AppContext con)
	{
		PrinterMon test = new PrinterMon("172.16.192.211");
	}

}
