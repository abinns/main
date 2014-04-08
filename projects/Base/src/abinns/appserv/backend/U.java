package abinns.appserv.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Utility class, handles printing output nicely, as well as multiple levels of
 * debug logging.
 *
 * @author Andrew Binns
 *
 */
public class U
{
	private static PrintStream			output;
	private static int					debugging	= 0;
	private static DateTimeFormatter	formatter	= DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
	private static Scanner				input;

	/**
	 * Static constructor, used to initialize output to the system.out
	 * printstream, could be used to init it to different things to add native
	 * support for logging to a file, etc.
	 */
	static
	{
		U.output = System.out;
		U.input = new Scanner(System.in);
	}

	/**
	 * Simple little function do so some substring magics to rename get a file
	 * with the same name but a different extension
	 *
	 * @param cur
	 *            the base file
	 * @param nExt
	 *            the new extension desired
	 * @return a string with the original filename, but modified with the new
	 *         extension.
	 */
	public static String changeExtTo(File cur, String nExt)
	{
		return cur.getAbsolutePath().substring(0, cur.getAbsolutePath().lastIndexOf(".")) + nExt;
	}

	/**
	 * Simple confirmation function, returns a true or false in response to the
	 * passed message.
	 *
	 * @param message
	 *            the question to ask the person.
	 * @return true or false depending on the user response.
	 */
	public static boolean confirm(String message)
	{
		U.p(message);
		String in = U.input.next();
		while (!in.equalsIgnoreCase("yes") && !in.equalsIgnoreCase("y") && !in.equalsIgnoreCase("no") && !in.equalsIgnoreCase("n"))
		{
			U.p("Invalid response, please input 'yes' or 'no'");
			in = U.input.next();
		}
		return in.equalsIgnoreCase("yes") || in.equalsIgnoreCase("y");
	}

	/**
	 * Prints out simple integers as strings, with the appropriate debug level.
	 *
	 * @param in
	 *            the int to print out
	 * @param level
	 *            the debugging level to use
	 */
	public static void d(int in, int level)
	{
		U.d(in + "", level);
	}

	/**
	 * Prints out a general object as a string, with the appropriate debug
	 * level.
	 *
	 * @param in
	 *            the object to print out.
	 * @param level
	 *            the debugging level to use
	 */
	public static void d(Object in, int level)
	{
		U.d(in.toString(), level);
	}

	/**
	 * Prints the specified string if the debug level specified is greater or
	 * equal to the current debugging level specified in this class.
	 *
	 * @param in
	 *            the string to print.
	 * @param level
	 *            the debugging level to print this message out at.
	 */
	public static void d(String in, int level)
	{
		if (U.debugging >= level)
			U.printWithTag(in, "DEBUG");
	}

	/**
	 * Takes a character and count, and returns a string which is that character
	 * duplicated count times.
	 *
	 * @param count
	 *            the number of duplications
	 * @param c
	 *            the character to duplicate
	 * @return a string made up of the duplicated characters
	 */
	public static String dupChar(int count, char c)
	{
		char[] res = new char[count];
		for (int i = 0; i < count; i++)
			res[i] = c;
		return new String(res);
	}

	/**
	 * Prints this string as an error
	 *
	 * @param in
	 *            the string to print
	 */
	public static void e(String in)
	{
		U.printWithTag(in, "ERROR");
	}

	/**
	 * Prints the specified string and exception, for error logging.
	 *
	 * @param in
	 *            the string to print
	 * @param E
	 *            the exception to also print.
	 */
	public static void e(String in, Exception E)
	{
		U.printWithTag(in + " - " + E, "ERROR");
	}

	/**
	 * Lazy routine, handles file-not-found exceptions, returns null instead of
	 * throwing error.
	 *
	 * @param filename
	 *            the file to load a scanner with
	 * @return a loaded scanner if success, null if fail.
	 */
	public static Scanner load(String filename)
	{
		try
		{
			return new Scanner(new File(filename));
		} catch (FileNotFoundException e)
		{
			U.e("Could not load file \"" + filename + "\"", e);
		}
		return null;
	}

	/**
	 * Prints a basic 2d array of floats nicely, rows & cols
	 *
	 * @param arr
	 *            the array to print
	 */
	public static void p(float[][] arr)
	{
		int i;
		StringBuilder[] grid = new StringBuilder[arr[0].length];
		for (i = 0; i < grid.length; i++)
			grid[i] = new StringBuilder();
		for (float[] col : arr)
		{
			i = 0;
			for (float cur : col)
				grid[i++].append("," + cur);
		}
		for (StringBuilder cur : grid)
			cur.deleteCharAt(0);
		StringBuilder res = new StringBuilder("\n");
		for (StringBuilder cur : grid)
		{
			res.append(cur);
			res.append('\n');
		}
		U.p(res.toString());
	}

	/**
	 * Prints the specified integer as simple output
	 *
	 * @param in
	 *            the score to print out.
	 */
	public static void p(int in)
	{
		U.p(in + "");

	}

	/**
	 * Prints the specified object as general output.
	 *
	 * @param in
	 *            the object to print.
	 */
	public static void p(Object in)
	{
		if (in != null)
			U.p(in.toString());
		else
			U.p("null");
	}

	/**
	 * Prints the specified string as general output.
	 *
	 * @param in
	 *            the string to print.
	 */
	public static void p(String in)
	{
		U.printWithTag(in, "OUTPUT");
	}

	/**
	 * A private method which prints the specified message to the output
	 * printstream, with the specified string as a tag, as well as the current
	 * date and time.
	 *
	 * @param in
	 *            the string to print
	 * @param tag
	 *            the string to tag it with
	 */
	private static void printWithTag(String in, String tag)
	{
		StringBuilder res = new StringBuilder();
		res.append(LocalDateTime.now().format(U.formatter));
		res.append("[");
		res.append(tag);
		res.append("] ");
		res.append(in);
		U.output.println(res.toString().trim());
	}

	/**
	 * Sets the debugging level to something new. Used so that the debugging
	 * level is set in the main, and not forgotten about...
	 *
	 * @param newLevel
	 *            the new debuglevel to use.
	 */
	public static void setDebugLevel(int newLevel)
	{
		U.debugging = newLevel;
	}

	/**
	 * A helper method which cleans up code, handles the try-catch so the
	 * calling method doesn't have to.
	 *
	 * @param millis
	 *            the number of milliseconds to sleep.
	 */
	public static void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		} catch (InterruptedException e)
		{
			U.e("Error sleeping", e);
		}
	}
}
