/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.prefs.Preferences;

import org.apache.commons.io.FileUtils;
import org.openpnp.ConfigurationListener;
import org.openpnp.spi.Machine;
import org.openpnp.util.ResourceUtils;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.HyphenStyle;
import org.simpleframework.xml.stream.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration extends AbstractModelObject
{
	/**
	 * Used to provide a fixed root for the Machine when serializing.
	 */
	@Root(name = "openpnp-machine")
	public static class MachineConfigurationHolder
	{
		@Element
		private Machine machine;
	}

	/**
	 * Used to provide a fixed root for the Packages when serializing.
	 */
	@Root(name = "openpnp-packages")
	public static class PackagesConfigurationHolder
	{
		@ElementList(inline = true, entry = "package", required = false)
		private ArrayList<Package> packages = new ArrayList<>();
	}

	/**
	 * Used to provide a fixed root for the Parts when serializing.
	 */
	@Root(name = "openpnp-parts")
	public static class PartsConfigurationHolder
	{
		@ElementList(inline = true, entry = "part", required = false)
		private ArrayList<Part> parts = new ArrayList<>();
	}

	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

	private static Configuration	instance;
	private static final String		PREF_UNITS	= "Configuration.units";

	private static final String	PREF_UNITS_DEF				= "Millimeters";
	private static final String	PREF_LENGTH_DISPLAY_FORMAT	= "Configuration.lengthDisplayFormat";

	private static final String	PREF_LENGTH_DISPLAY_FORMAT_DEF			= "%.3f";
	private static final String	PREF_LENGTH_DISPLAY_FORMAT_WITH_UNITS	= "Configuration.lengthDisplayFormatWithUnits";

	private static final String	PREF_LENGTH_DISPLAY_FORMAT_WITH_UNITS_DEF	= "%.3f%s";
	private static final String	PREF_VERTICAL_SCROLL_UNIT_INCREMENT			= "Configuration.verticalScrollUnitIncrement";
	private static final int	PREF_VERTICAL_SCROLL_UNIT_INCREMENT_DEF		= 16;

	public static String createId()
	{
		return UUID.randomUUID().toString();
	}

	public static Serializer createSerializer()
	{
		Style style = new HyphenStyle();
		Format format = new Format(style);
		AnnotationStrategy strategy = new AnnotationStrategy();
		Serializer serializer = new Persister(strategy, format);
		return serializer;
	}

	public static Configuration get()
	{
		if (Configuration.instance == null)
			throw new Error("Configuration instance not yet initialized.");
		return Configuration.instance;
	}

	public static synchronized void initialize(File configurationDirectory)
	{
		Configuration.instance = new Configuration(configurationDirectory);
		Configuration.instance.setLengthDisplayFormatWithUnits(Configuration.PREF_LENGTH_DISPLAY_FORMAT_WITH_UNITS_DEF);
	}

	private LinkedHashMap<String, Package> packages = new LinkedHashMap<>();

	private LinkedHashMap<String, Part> parts = new LinkedHashMap<>();

	private Machine machine;

	private LinkedHashMap<File, Board> boards = new LinkedHashMap<>();

	private boolean loaded;

	private Set<ConfigurationListener> listeners = Collections.synchronizedSet(new HashSet<>());

	private File configurationDirectory;

	private Preferences prefs;

	private Configuration(File configurationDirectory)
	{
		this.configurationDirectory = configurationDirectory;
		this.prefs = Preferences.userNodeForPackage(Configuration.class);
	}

	public void addListener(ConfigurationListener listener)
	{
		this.listeners.add(listener);
		if (this.loaded)
			try
			{
				listener.configurationLoaded(this);
				listener.configurationComplete(this);
			} catch (Exception e)
			{
				// TODO: Need to find a way to raise this to the GUI
				throw new Error(e);
			}
	}

	public void addPackage(Package pkg)
	{
		if (null == pkg.getId())
			throw new Error("Package with null Id cannot be added to Configuration.");
		this.packages.put(pkg.getId().toUpperCase(), pkg);
		this.firePropertyChange("packages", null, this.packages);
	}

	public void addPart(Part part)
	{
		if (null == part.getId())
			throw new Error("Part with null Id cannot be added to Configuration.");
		this.parts.put(part.getId().toUpperCase(), part);
		this.firePropertyChange("parts", null, this.parts);
	}

	/**
	 * Creates a new file with a unique name within the configuration directory.
	 * forClass is used to uniquely identify the file within the application and
	 * a unique name is generated within that namespace. suffix is appended to
	 * the unique part of the filename. The result of calling File.getName() on
	 * the returned file can be used to load the same file in the future by
	 * calling getResourceFile(). This method uses File.createTemporaryFile()
	 * and so the rules for that method must be followed when calling this one.
	 * 
	 * @param forClass
	 * @param suffix
	 * @return
	 * @throws IOException
	 */
	public File createResourceFile(Class forClass, String prefix, String suffix) throws IOException
	{
		File directory = new File(this.configurationDirectory, forClass.getCanonicalName());
		if (!directory.exists())
			directory.mkdirs();
		File file = File.createTempFile(prefix, suffix, directory);
		return file;
	}

	public Board getBoard(File file) throws Exception
	{
		if (!file.exists())
		{
			Board board = new Board(file);
			board.setName(file.getName());
			Serializer serializer = Configuration.createSerializer();
			serializer.write(board, file);
		}
		file = file.getCanonicalFile();
		if (this.boards.containsKey(file))
			return this.boards.get(file);
		Board board = this.loadBoard(file);
		this.boards.put(file, board);
		this.firePropertyChange("boards", null, this.boards);
		return board;
	}

	public List<Board> getBoards()
	{
		return Collections.unmodifiableList(new ArrayList<>(this.boards.values()));
	}

	public File getConfigurationDirectory()
	{
		return this.configurationDirectory;
	}

	public String getLengthDisplayFormat()
	{
		return this.prefs.get(Configuration.PREF_LENGTH_DISPLAY_FORMAT, Configuration.PREF_LENGTH_DISPLAY_FORMAT_DEF);
	}

	public String getLengthDisplayFormatWithUnits()
	{
		return this.prefs.get(Configuration.PREF_LENGTH_DISPLAY_FORMAT_WITH_UNITS, Configuration.PREF_LENGTH_DISPLAY_FORMAT_WITH_UNITS_DEF);
	}

	public Machine getMachine()
	{
		return this.machine;
	}

	public Package getPackage(String id)
	{
		if (id == null)
			return null;
		return this.packages.get(id.toUpperCase());
	}

	public List<Package> getPackages()
	{
		return Collections.unmodifiableList(new ArrayList<>(this.packages.values()));
	}

	public Part getPart(String id)
	{
		if (id == null)
			return null;
		return this.parts.get(id.toUpperCase());
	}

	public List<Part> getParts()
	{
		return Collections.unmodifiableList(new ArrayList<>(this.parts.values()));
	}

	/**
	 * Gets a File reference for the resources directory belonging to the given
	 * class. The directory is guaranteed to exist.
	 * 
	 * @param forClass
	 * @return
	 * @throws IOException
	 */
	public File getResourceDirectory(Class forClass) throws IOException
	{
		File directory = new File(this.configurationDirectory, forClass.getCanonicalName());
		if (!directory.exists())
			directory.mkdirs();
		return directory;
	}

	/**
	 * Gets a File reference for the named file within the configuration
	 * directory. forClass is used to uniquely identify the file and keep it
	 * separate from other classes' files.
	 * 
	 * @param forClass
	 * @param name
	 * @return
	 */
	public File getResourceFile(Class forClass, String name) throws IOException
	{
		return new File(this.getResourceDirectory(forClass), name);
	}

	public LengthUnit getSystemUnits()
	{
		return LengthUnit.valueOf(this.prefs.get(Configuration.PREF_UNITS, Configuration.PREF_UNITS_DEF));
	}

	public int getVerticalScrollUnitIncrement()
	{
		return this.prefs.getInt(Configuration.PREF_VERTICAL_SCROLL_UNIT_INCREMENT, Configuration.PREF_VERTICAL_SCROLL_UNIT_INCREMENT_DEF);
	}

	public synchronized void load() throws Exception
	{
		boolean forceSave = false;
		boolean overrideUserConfig = Boolean.getBoolean("overrideUserConfig");

		try
		{
			File file = new File(this.configurationDirectory, "packages.xml");
			if (overrideUserConfig || !file.exists())
			{
				Configuration.logger.info("No packages.xml found in configuration directory, loading defaults.");
				file = File.createTempFile("packages", "xml");
				FileUtils.copyURLToFile(ClassLoader.getSystemResource("config/packages.xml"), file);
				forceSave = true;
			}
			this.loadPackages(file);
		} catch (Exception e)
		{
			String message = e.getMessage();
			if (e.getCause() != null && e.getCause().getMessage() != null)
				message = e.getCause().getMessage();
			throw new Exception("Error while reading packages.xml (" + message + ")", e);
		}

		try
		{
			File file = new File(this.configurationDirectory, "parts.xml");
			if (overrideUserConfig || !file.exists())
			{
				Configuration.logger.info("No parts.xml found in configuration directory, loading defaults.");
				file = File.createTempFile("parts", "xml");
				FileUtils.copyURLToFile(ClassLoader.getSystemResource("config/parts.xml"), file);
				forceSave = true;
			}
			this.loadParts(file);
		} catch (Exception e)
		{
			String message = e.getMessage();
			if (e.getCause() != null && e.getCause().getMessage() != null)
				message = e.getCause().getMessage();
			throw new Exception("Error while reading parts.xml (" + message + ")", e);
		}

		try
		{
			File file = new File(this.configurationDirectory, "machine.xml");
			if (overrideUserConfig || !file.exists())
			{
				Configuration.logger.info("No machine.xml found in configuration directory, loading defaults.");
				file = File.createTempFile("machine", "xml");
				FileUtils.copyURLToFile(ClassLoader.getSystemResource("config/machine.xml"), file);
				forceSave = true;
			}
			this.loadMachine(file);
		} catch (Exception e)
		{
			String message = e.getMessage();
			if (e.getCause() != null && e.getCause().getMessage() != null)
				message = e.getCause().getMessage();
			throw new Exception("Error while reading machine.xml (" + message + ")", e);
		}

		this.loaded = true;

		for (ConfigurationListener listener : this.listeners)
			listener.configurationLoaded(this);

		if (forceSave)
		{
			Configuration.logger.info("Defaults were loaded. Saving to configuration directory.");
			this.configurationDirectory.mkdirs();
			this.save();
		}

		for (ConfigurationListener listener : this.listeners)
			listener.configurationComplete(this);
	}

	private Board loadBoard(File file) throws Exception
	{
		Serializer serializer = Configuration.createSerializer();
		Board board = serializer.read(Board.class, file);
		board.setFile(file);
		board.setDirty(false);
		return board;
	}

	public Job loadJob(File file) throws Exception
	{
		Serializer serializer = Configuration.createSerializer();
		Job job = serializer.read(Job.class, file);
		job.setFile(file);

		// Once the Job is loaded we need to resolve any Boards that it
		// references.
		for (BoardLocation boardLocation : job.getBoardLocations())
		{
			String boardFilename = boardLocation.getBoardFile();
			// First see if we can find the board at the given filename
			// If the filename is not absolute this will be relative
			// to the working directory
			File boardFile = new File(boardFilename);
			if (!boardFile.exists())
				// If that fails, see if we can find it relative to the
				// directory the job was in
				boardFile = new File(file.getParentFile(), boardFilename);
			if (!boardFile.exists())
				throw new Exception("Board file not found: " + boardFilename);
			Board board = this.getBoard(boardFile);
			boardLocation.setBoard(board);
		}

		job.setDirty(false);

		return job;
	}

	private void loadMachine(File file) throws Exception
	{
		Serializer serializer = Configuration.createSerializer();
		MachineConfigurationHolder holder = serializer.read(MachineConfigurationHolder.class, file);
		this.machine = holder.machine;
	}

	private void loadPackages(File file) throws Exception
	{
		Serializer serializer = Configuration.createSerializer();
		PackagesConfigurationHolder holder = serializer.read(PackagesConfigurationHolder.class, file);
		for (Package pkg : holder.packages)
			this.addPackage(pkg);
	}

	private void loadParts(File file) throws Exception
	{
		Serializer serializer = Configuration.createSerializer();
		PartsConfigurationHolder holder = serializer.read(PartsConfigurationHolder.class, file);
		for (Part part : holder.parts)
			this.addPart(part);
	}

	public void removeListener(ConfigurationListener listener)
	{
		this.listeners.remove(listener);
	}

	public void removePackage(Package pkg)
	{
		this.packages.remove(pkg.getId());
		this.firePropertyChange("packages", null, this.packages);
	}

	public void removePart(Part part)
	{
		this.parts.remove(part.getId());
		this.firePropertyChange("parts", null, this.parts);
	}

	public synchronized void save() throws Exception
	{
		try
		{
			this.saveMachine(new File(this.configurationDirectory, "machine.xml"));
		} catch (Exception e)
		{
			throw new Exception("Error while saving machine.xml (" + e.getMessage() + ")", e);
		}
		try
		{
			this.savePackages(new File(this.configurationDirectory, "packages.xml"));
		} catch (Exception e)
		{
			throw new Exception("Error while saving packages.xml (" + e.getMessage() + ")", e);
		}
		try
		{
			this.saveParts(new File(this.configurationDirectory, "parts.xml"));
		} catch (Exception e)
		{
			throw new Exception("Error while saving parts.xml (" + e.getMessage() + ")", e);
		}
	}

	public void saveBoard(Board board) throws Exception
	{
		Serializer serializer = Configuration.createSerializer();
		serializer.write(board, new ByteArrayOutputStream());
		serializer.write(board, board.getFile());
		board.setDirty(false);
	}

	public void saveJob(Job job, File file) throws Exception
	{
		Serializer serializer = Configuration.createSerializer();
		Set<Board> boards = new HashSet<>();
		// Fix the paths to any boards in the Job
		for (BoardLocation boardLocation : job.getBoardLocations())
		{
			Board board = boardLocation.getBoard();
			boards.add(board);
			try
			{
				String relativePath = ResourceUtils.getRelativePath(board.getFile().getAbsolutePath(), file.getAbsolutePath(), File.separator);
				boardLocation.setBoardFile(relativePath);
			} catch (ResourceUtils.PathResolutionException ex)
			{
				boardLocation.setBoardFile(board.getFile().getAbsolutePath());
			}
		}
		// Save any boards in the job
		for (Board board : boards)
			this.saveBoard(board);
		// Save the job
		serializer.write(job, new ByteArrayOutputStream());
		serializer.write(job, file);
		job.setFile(file);
		job.setDirty(false);
	}

	private void saveMachine(File file) throws Exception
	{
		MachineConfigurationHolder holder = new MachineConfigurationHolder();
		holder.machine = this.machine;
		Serializer serializer = Configuration.createSerializer();
		serializer.write(holder, new ByteArrayOutputStream());
		serializer.write(holder, file);
	}

	private void savePackages(File file) throws Exception
	{
		Serializer serializer = Configuration.createSerializer();
		PackagesConfigurationHolder holder = new PackagesConfigurationHolder();
		holder.packages = new ArrayList<>(this.packages.values());
		serializer.write(holder, new ByteArrayOutputStream());
		serializer.write(holder, file);
	}

	private void saveParts(File file) throws Exception
	{
		Serializer serializer = Configuration.createSerializer();
		PartsConfigurationHolder holder = new PartsConfigurationHolder();
		holder.parts = new ArrayList<>(this.parts.values());
		serializer.write(holder, new ByteArrayOutputStream());
		serializer.write(holder, file);
	}

	public void setLengthDisplayFormat(String format)
	{
		this.prefs.put(Configuration.PREF_LENGTH_DISPLAY_FORMAT, format);
	}

	public void setLengthDisplayFormatWithUnits(String format)
	{
		this.prefs.put(Configuration.PREF_LENGTH_DISPLAY_FORMAT_WITH_UNITS, format);
	}

	public void setSystemUnits(LengthUnit lengthUnit)
	{
		this.prefs.put(Configuration.PREF_UNITS, lengthUnit.name());
	}

	public void setVerticalScrollUnitIncrement(int verticalScrollUnitIncrement)
	{
		this.prefs.putInt(Configuration.PREF_VERTICAL_SCROLL_UNIT_INCREMENT, Configuration.PREF_VERTICAL_SCROLL_UNIT_INCREMENT_DEF);
	}
}
