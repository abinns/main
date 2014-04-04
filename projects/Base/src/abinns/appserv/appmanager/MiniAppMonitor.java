package abinns.appserv.appmanager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import abinns.appserv.backend.U;
import abinns.appserv.exceptions.AppLoadException;

public class MiniAppMonitor
{
	private String	basedir;

	public MiniAppMonitor(String basedir)
	{
		this.basedir = basedir;
		for (File cur : new File(basedir).listFiles())
			try
		{
				this.load(cur);
		} catch (AppLoadException e)
		{
			U.e("Error loading from file " + cur);
		}
	}

	public String getBasedir()
	{
		return this.basedir;
	}

	private App load(File cur) throws AppLoadException
	{
		ClassLoader authorizedLoader = null;
		try
		{
			authorizedLoader = URLClassLoader.newInstance(new URL[]
					{ new URL(cur.getAbsolutePath()) });
		} catch (MalformedURLException e)
		{
			throw new AppLoadException("Error loading url from file " + cur);
		}
		try
		{
			return (App) authorizedLoader.loadClass("abinns.appserv.appmanager.App").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;

	}
}
