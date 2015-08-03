package abinns.appserv.appmanager;

import java.io.File;

import abinns.appserv.appmanager.parser.Parser;

public class AppConfig
{

	private String	classname;

	public AppConfig(File configdocs)
	{
		Parser parser = new Parser(configdocs);
		this.classname = parser.getTextFromTag("classname");
	}

	public String getClassName()
	{
		return this.classname;
	}

}
