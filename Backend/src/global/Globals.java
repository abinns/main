package global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Scanner;

import backend.U;
import backend.functionInterfaces.Func;
import backend.lib.lzmastreams.LzmaInputStream;
import backend.lib.lzmastreams.LzmaOutputStream;

public class Globals
{
	private static LinkedList<Func> onClose;

	static
	{
		Globals.onClose = new LinkedList<Func>();
		// Adds shutdown hook, executes items prior to ending.
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			for (Func f : Globals.onClose)
				f.exec();
		}));
	}

	/**
	 * Simply a wrapper for System.exit(0).
	 */
	public static void exit()
	{
		System.exit(0);
	}

	/**
	 * Given a filename, reads an object from it. Note: make sure you are
	 * reading into the correct class (and version of the class), otherwise
	 * ClassNotFound exceptions will be thrown.
	 *
	 * @param filename
	 *            the filename to said object to.
	 * @throws ClassNotFoundException
	 *             if the class that this is trying to read into does not match
	 *             the stored file
	 * @throws FileNotFoundException
	 *             if the file is not found
	 * @throws IOException
	 */

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T objReadFromFile(String filename) throws FileNotFoundException, ClassNotFoundException, IOException
	{
		T result = null;
		try (FileInputStream fis = new FileInputStream(filename); LzmaInputStream lis = new LzmaInputStream(fis); ObjectInputStream ois = new ObjectInputStream(lis);)
		{
			result = (T) ois.readObject();
		} catch (FileNotFoundException e)
		{
			U.e("'" + filename + "' not found, throwing exception.", e);
			throw e;
		} catch (ClassNotFoundException e)
		{
			U.e("'" + filename + "' could not be thrown into, throwing exception.", e);
			throw e;
		} catch (IOException e)
		{
			U.e("'" + filename + "' file invalid, throwing exception.", e);
			throw e;
		}
		return result;
	}

	/**
	 * Given a Serializable object, exports to the filename specified.
	 *
	 * @param obj
	 *            the object to save
	 * @param filename
	 *            the filename to said object to.
	 */
	public static <T extends Serializable> void objWriteToFile(T obj, String filename) throws FileNotFoundException, IOException
	{
		U.d("Writing object to filename " + filename + ".", 10);
		try (FileOutputStream fos = new FileOutputStream(filename); LzmaOutputStream los = new LzmaOutputStream(fos); ObjectOutputStream oos = new ObjectOutputStream(los))
		{
			oos.writeObject(obj);
		} catch (FileNotFoundException e)
		{
			U.e("Error: '" + filename + "' file not found.", e);
			throw e;
		} catch (IOException e)
		{
			U.e("Error: '" + filename + "' file invalid, throwing exception.", e);
			throw e;
		}
	}

	/**
	 * Attempts to open the file specified.
	 *
	 * @param filename
	 *            the file to try and open
	 * @return a scanner to the given file
	 */

	public static Scanner openScannerOnFile(String filename)
	{
		try
		{
			return new Scanner(new File(filename));
		} catch (FileNotFoundException e)
		{
			U.e("Error opening file " + filename, e);
		}
		return null;
	}

	/**
	 * Given a file, attempts to load the contents as a string. Assumes UTF_8
	 * encoding. Returns "" if any kind of error.
	 *
	 * @param path
	 *            the file to open
	 * @return the contents of the specified file
	 */

	public static String readFile(String path)
	{
		return Globals.readFile(path, StandardCharsets.UTF_8);
	}

	/**
	 * Given a file and encoding, attempts to load the contents as a string.
	 * Returns "" if any kind of error.
	 *
	 * @param path
	 *            the file to open
	 * @param encoding
	 * @return the contents of the specified file
	 */
	public static String readFile(String path, Charset encoding)
	{
		try
		{
			return new String(Files.readAllBytes(Paths.get(path)), encoding);
		} catch (IOException e)
		{
			U.e("Error reading from file " + path);
			return "";
		}
	}

	/**
	 * Given a lambda, adds it to the internal onclose handler list.
	 *
	 * @param f
	 *            the lambda to add
	 */

	public static void registerExitHandler(Func f)
	{
		Globals.onClose.add(f);
	}
}
