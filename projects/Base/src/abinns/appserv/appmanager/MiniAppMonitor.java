package abinns.appserv.appmanager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;

import abinns.appserv.backend.U;
import abinns.appserv.exceptions.AppLoadException;

public class MiniAppMonitor
{
	private LinkedList<App>	apps;

	public MiniAppMonitor(String basedir)
	{
		this.apps = new LinkedList<App>();

		U.p("Loading base from path " + basedir);
		File base = new File(basedir);

		loadFromDir(base);
		
		this.start();
	}
	
	public void start()
	{
		for(App cur : this.apps)
			cur.start(null);
	}

	private void loadFromDir(File base)
	{
		if (base.exists())
			for (File cur : base.listFiles())
				if (cur.toString().endsWith(".jar"))
				{
					File conf = new File(U.withExt(cur, ".conf"));
					if (conf.exists())
						if (!conf.isDirectory())
							try
							{
								this.apps.add(this.load(cur, new File(U.withExt(cur, ".conf"))));
							} catch (AppLoadException e)
							{
								U.e("Error loading from file " + cur);
							}
				}
	}

	private App load(File cur, File configdocs) throws AppLoadException
	{
		AppConfig conf = new AppConfig(configdocs);
		U.p("Attempting to load " + conf.getClassName() + " from " + cur.getAbsolutePath());
		try
		{
			URL url = cur.toURI().toURL();
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { url });
			return (App) loader.loadClass(conf.getClassName()).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | MalformedURLException e)
		{
			U.e("Error loading class" + conf.getClassName() + " from " + cur.getAbsolutePath() + ".", e);
		}
		return null;

	}
}
