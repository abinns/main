import abinns.appserv.appcontext.AppContext;
import abinns.appserv.backend.U;

public class OtherPart
{
	public OtherPart(AppContext con)
	{
		U.p(con + " From somewhere Else!!!");

		con.addTask(() -> U.p("This is done from another task!!"));
		con.addTask(() -> U.p("This is done from another task!!"));
		con.addTask(() -> U.p("This is done from another task!!"));
		con.addTask(() -> U.p("This is done from another task!!"));
		con.addTask(() -> U.p("This is done from another task!!"));
		con.addTask(() -> U.p("This is done from another task!!"));
		con.addTask(() -> U.p("This is done from another task!!"));
		con.addTask(() -> U.p("This is done from another task!!"));
		U.sleep(1000);
	}
}
