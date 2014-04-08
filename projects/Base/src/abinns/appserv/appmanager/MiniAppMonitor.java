package abinns.appserv.appmanager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;

import abinns.appserv.appcontext.AppContext;
import abinns.appserv.backend.U;
import abinns.appserv.exceptions.AppLoadException;

public class MiniAppMonitor
{
	private LinkedList<App>	apps;

	private AppContext		context;

	public MiniAppMonitor(String basedir)
	{
		this.apps = new LinkedList<App>();
		this.context = new AppContext();

		U.p("Loading base from path " + basedir);
		File base = new File(basedir);

		this.loadFromDir(base);

		this.start();
	}

	private App load(File appContainer, File configDocs) throws AppLoadException
	{
		AppConfig conf = new AppConfig(configDocs);
		U.p("Attempting to load " + conf.getClassName() + " from " + appContainer.getAbsolutePath());
		try
		{
			URL url = appContainer.toURI().toURL();
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { url });
			return (App) loader.loadClass(conf.getClassName()).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | MalformedURLException e)
		{
			throw new AppLoadException("Error loading class" + conf.getClassName() + " from " + appContainer.getAbsolutePath() + ".");
		}
	}

	private void loadFromDir(File base)
	{
		if (base.exists())
			for (File cur : base.listFiles())
				if (cur.toString().endsWith(".jar"))
				{
					File conf = new File(U.changeExtTo(cur, ".conf"));
					if (conf.exists())
						if (!conf.isDirectory())
							try
					{
								this.apps.add(this.load(cur, new File(U.changeExtTo(cur, ".conf"))));
					} catch (AppLoadException e)
					{
						U.e("Error loading from file " + cur);
					}
				}
	}

	public void start()
	{
		for (App cur : this.apps)
			cur.start(this.context);
	}
}
