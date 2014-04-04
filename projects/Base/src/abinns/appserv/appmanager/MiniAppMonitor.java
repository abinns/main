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
		U.p("Starting");
		this.basedir = basedir;
		File base = new File(basedir);
		if (base.exists())
			for (File cur : base.listFiles())
				if (cur.toString().endsWith(".jar"))
					if (new File(U.withExt(cur, ".conf")).exists())
						try
		{
							this.load(cur, new File(U.withExt(cur, ".conf"))).start(null);
		} catch (AppLoadException e)
		{
			U.e("Error loading from file " + cur);
		}
	}

	public String getBasedir()
	{
		return this.basedir;
	}

	private App load(File cur, File configdocs) throws AppLoadException
	{
		AppConfig conf = new AppConfig(configdocs);
		U.p("Attempting to " + conf.getClassName() + " load from " + cur.getAbsolutePath());
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
