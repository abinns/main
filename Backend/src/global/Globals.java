package global;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Scanner;

import backend.U;
import backend.functionInterfaces.Func;

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
	 * Given a lambda, adds it to the internal onclose handler list.
	 *
	 * @param f
	 *            the lambda to add
	 */

	public static void registerExitHandler(Func f)
	{
		Globals.onClose.add(f);
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
}
