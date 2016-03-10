package org.openpnp.gui.importer.rs274x;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpnp.model.BoardPad;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Pad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple RS-274X parser. Not intended to be a general parser, but implements
 * only OpenPnP specific functionality.
 */
public class Rs274xParser
{
	static abstract class Aperture
	{
		final protected int index;

		public Aperture(int index)
		{
			this.index = index;
		}

		public abstract BoardPad createPad(LengthUnit unit, Point2D.Double coordinate);

		public int getIndex()
		{
			return this.index;
		}
	}

	static class CircleAperture extends StandardAperture
	{
		public double	diameter;
		public Double	holeDiameter;

		public CircleAperture(int index, double diameter, Double holeDiameter)
		{
			super(index);
			this.diameter = diameter;
			this.holeDiameter = holeDiameter;
		}

		@Override
		public BoardPad createPad(LengthUnit unit, Point2D.Double coordinate)
		{
			Pad.Circle pad = new Pad.Circle();
			pad.setRadius(this.diameter / 2);
			pad.setUnits(unit);
			BoardPad boardPad = new BoardPad(pad, new Location(unit, coordinate.x, coordinate.y, 0, 0));
			return boardPad;
		}

		@Override
		public String toString()
		{
			return "CircleAperture [diameter=" + this.diameter + ", holeDiameter=" + this.holeDiameter + "]";
		}
	}

	enum InterpolationMode
	{
		Linear, Clockwise, CounterClockwise
	}

	enum LevelPolarity
	{
		Dark, Clear
	}

	static class MacroAperture extends Aperture
	{
		public MacroAperture(int index)
		{
			super(index);
		}

		@Override
		public BoardPad createPad(LengthUnit unit, java.awt.geom.Point2D.Double coordinate)
		{
			return null;
		}
	}

	static class ObroundAperture extends RectangleAperture
	{
		public ObroundAperture(int index, double width, double height, Double holeDiameter)
		{
			super(index, width, height, holeDiameter);
		}

		@Override
		public String toString()
		{
			return "ObroundAperture [width=" + this.width + ", height=" + this.height + ", holeDiameter=" + this.holeDiameter + "]";
		}
	}

	static class ParseStatistics
	{
		public int	lineCount;
		public int	linePerformedCount;

		public int	arcCount;
		public int	arcPerformedCount;

		public int	regionLineCount;
		public int	regionLinePerformedCount;

		public int	regionArcCount;
		public int	regionArcPerformedCount;

		public int	regionCount;
		public int	regionPerformedCount;

		public int	flashCount;
		public int	flashPerformedCount;

		public int padCount;

		public boolean errored;

		public void add(ParseStatistics p)
		{
			this.lineCount += p.lineCount;
			this.linePerformedCount += p.linePerformedCount;

			this.arcCount += p.arcCount;
			this.arcPerformedCount += p.arcPerformedCount;

			this.regionLineCount += p.regionLineCount;
			this.regionLinePerformedCount += p.regionLinePerformedCount;

			this.regionArcCount += p.regionArcCount;
			this.regionArcPerformedCount += p.regionArcPerformedCount;

			this.regionCount += p.regionCount;
			this.regionPerformedCount += p.regionPerformedCount;

			this.flashCount += p.flashCount;
			this.flashPerformedCount += p.flashPerformedCount;

			this.padCount += p.padCount;
		}

		public double percent(double count, double total)
		{
			if (total == 0)
				return 100;
			return count / total * 100;
		}

		@Override
		public String toString()
		{
			int total = this.flashCount + this.regionCount + this.lineCount + this.arcCount;
			int totalPerformed = this.flashPerformedCount + this.regionPerformedCount + this.linePerformedCount + this.arcPerformedCount;
			return String.format(
					"%s Total %3.0f%% (%4d/%4d), Flash %3.0f%% (%4d/%4d), Line %3.0f%% (%4d/%4d), Arc %3.0f%% (%4d/%4d), Region %3.0f%% (%4d/%4d), Region line %3.0f%% (%4d/%4d), Region Arc %3.0f%% (%4d/%4d), Pads %4d",
					this.errored ? "FAIL" : "PASS", this.percent(totalPerformed, total), totalPerformed, total, this.percent(this.flashPerformedCount, this.flashCount), this.flashPerformedCount,
					this.flashCount, this.percent(this.linePerformedCount, this.lineCount), this.linePerformedCount, this.lineCount, this.percent(this.arcPerformedCount, this.arcCount),
					this.arcPerformedCount, this.arcCount, this.percent(this.regionPerformedCount, this.regionCount), this.regionPerformedCount, this.regionCount,
					this.percent(this.regionLinePerformedCount, this.regionLineCount), this.regionLinePerformedCount, this.regionLineCount,
					this.percent(this.regionArcPerformedCount, this.regionArcCount), this.regionArcPerformedCount, this.regionArcCount, this.padCount);
		}
	}

	static class PolygonAperture extends CircleAperture
	{
		public int		numberOfVertices;
		public Double	rotation;

		public PolygonAperture(int index, double diameter, int numberOfVertices, Double rotation, Double holeDiameter)
		{
			super(index, diameter, holeDiameter);
			this.numberOfVertices = numberOfVertices;
			this.rotation = rotation;
		}

		@Override
		public String toString()
		{
			return "PolygonAperture [numberOfVertices=" + this.numberOfVertices + ", rotation=" + this.rotation + ", diameter=" + this.diameter + ", holeDiameter=" + this.holeDiameter + "]";
		}
	}

	static class RectangleAperture extends StandardAperture
	{
		public double	width;
		public double	height;
		public Double	holeDiameter;

		public RectangleAperture(int index, double width, double height, Double holeDiameter)
		{
			super(index);
			this.width = width;
			this.height = height;
			this.holeDiameter = holeDiameter;
		}

		@Override
		public BoardPad createPad(LengthUnit unit, Point2D.Double coordinate)
		{
			Pad.RoundRectangle pad = new Pad.RoundRectangle();
			pad.setUnits(unit);
			pad.setWidth(this.width);
			pad.setHeight(this.height);
			pad.setRoundness(0);
			BoardPad boardPad = new BoardPad(pad, new Location(unit, coordinate.x, coordinate.y, 0, 0));
			return boardPad;
		}

		@Override
		public String toString()
		{
			return "RectangleAperture [width=" + this.width + ", height=" + this.height + ", holeDiameter=" + this.holeDiameter + "]";
		}
	}

	static abstract class StandardAperture extends Aperture
	{
		public StandardAperture(int index)
		{
			super(index);
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(Rs274xParser.class);

	public static void main(String[] args) throws Exception
	{
		HashMap<File, ParseStatistics> results = new HashMap<>();
		File[] files = new File("/Users/jason/Desktop/paste_tests").listFiles();
		for (File file : files)
		{
			if (file.isDirectory())
				continue;
			if (file.getName().equals(".DS_Store"))
				continue;
			Rs274xParser parser = new Rs274xParser();
			try
			{
				parser.parseSolderPastePads(file);
			} catch (Exception e)
			{
				System.out.println(file.getName() + " " + e.getMessage());
			}
			results.put(file, parser.parseStatistics);
		}

		ParseStatistics total = new ParseStatistics();
		Rs274xParser.logger.info("");
		Rs274xParser.logger.info("");
		ArrayList<File> sortedFiles = new ArrayList<>(results.keySet());
		Collections.sort(sortedFiles, new Comparator<File>()
		{
			@Override
			public int compare(File o1, File o2)
			{
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
		});
		for (File file : sortedFiles)
		{
			ParseStatistics stats = results.get(file);
			total.add(stats);
			;
			Rs274xParser.logger.info(String.format("%-32s: %s", file.getName(), stats.toString()));
		}
		String totalLine = String.format("%-32s: %s", "TOTALS", total.toString());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < totalLine.length(); i++)
			sb.append("-");
		Rs274xParser.logger.info(sb.toString());
		Rs274xParser.logger.info(totalLine);
	}

	private BufferedReader	reader;
	// Context
	private LengthUnit		unit;
	private Aperture		currentAperture;
	private Point2D.Double	currentPoint;
	private LevelPolarity	levelPolarity;

	private InterpolationMode	interpolationMode;
	private boolean				multiQuadrantMode;
	private boolean				regionMode;
	private int					coordinateFormatIntegerLength;

	private int coordinateFormatDecimalLength;

	private boolean coordinateFormatTrailingZeroOmission;

	private boolean coordinateFormatIncremental;

	private Map<Integer, Aperture> apertures = new HashMap<>();

	/**
	 * Maps Aperture indexes to a count to aid in generation of pad names.
	 */
	private Map<Integer, Integer> apertureUseCounts = new HashMap<>();

	private boolean stopped;

	private int lineNumber;

	private ParseStatistics parseStatistics;

	private boolean regionStarted;

	private List<BoardPad> pads;

	public Rs274xParser()
	{
		this.reset();
	}

	/**
	 * Return the next character in the reader without consuming it.
	 * 
	 * @return
	 * @throws Exception
	 */
	private int _peek() throws Exception
	{
		this.reader.mark(1);
		int ch = this.reader.read();
		if (ch == -1)
			this.error("Unexpected end of stream");
		this.reader.reset();
		return ch;
	}

	private void addRegionArc(Point2D.Double coordinate, Point2D.Double arcCoordinate) throws Exception
	{
		if (!this.regionMode)
			this.error("Can't add region arc outside of region mode");
		if (!this.regionStarted)
			this.regionStarted = true;
		this.parseStatistics.regionArcCount++;
		this.warn("Circular interpolation in region mode not yet supported");
	}

	private void addRegionLine(Point2D.Double coordinate) throws Exception
	{
		if (!this.regionMode)
			this.error("Can't add region line outside of region mode");
		if (!this.regionStarted)
			this.regionStarted = true;
		this.parseStatistics.regionLineCount++;
		this.warn("Linear interpolation in region mode not yet supported");
	}

	private void closeRegion() throws Exception
	{
		if (!this.regionMode)
			this.error("Can't end region when not in region mode");
		if (this.regionStarted)
		{
			this.regionStarted = false;
			this.parseStatistics.regionCount++;
		}
	}

	private void disableRegionMode() throws Exception
	{
		if (!this.regionMode)
			this.error("Can't exit region mode, not in region mode");
		this.closeRegion();
		this.regionMode = false;
	}

	private void enableRegionMode() throws Exception
	{
		if (this.regionMode)
			this.error("Can't start region mode when already in region mode");
		this.regionMode = true;
		this.regionStarted = false;
	}

	private void error(String s) throws Exception
	{
		throw new Exception("ERROR: " + this.lineNumber + ": " + s);
	}

	/**
	 * Parse the given File for solder paste pads.
	 * 
	 * @see #parseSolderPastePads(Reader)
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public List<BoardPad> parseSolderPastePads(File file) throws Exception
	{
		Rs274xParser.logger.info("Parsing " + file);
		return this.parseSolderPastePads(new FileReader(file));
	}

	/**
	 * Parse the input from the Reader extracting individual pads to be used for
	 * solder paste application. It is expected that the input is is an RS-274X
	 * Gerber solder paste layer. Currently this code only parses out single
	 * flashes of rectangular, circular and oblong apertures. Ideas for future
	 * versions include rendering the entire file and uses blob detection and
	 * contour finding to create polygon pads. Another option is to consider
	 * each operation it's own element/shape. This is how gerbv seems to do it.
	 * 
	 * @param reader
	 * @return
	 * @throws Exception
	 */
	public List<BoardPad> parseSolderPastePads(Reader reader) throws Exception
	{
		this.reset();

		this.reader = new BufferedReader(reader);

		try
		{
			while (!this.stopped)
				this.readCommand();
		} catch (Exception e)
		{
			this.parseStatistics.errored = true;
			this.error("Uncaught error: " + e.getMessage());
		}

		return this.pads;
	}

	/**
	 * Peek at the next character in the stream, skipping any \r or \n that
	 * precede it.
	 * 
	 * @return
	 * @throws Exception
	 */
	private int peek() throws Exception
	{
		this.skipCrLf();
		return this._peek();
	}

	/**
	 * Linear or circular interpolation. If in region mode, add a line or arc to
	 * the current contour. Otherwise draw a line or arc.
	 * 
	 * @param coordinate
	 * @param arcCoordinate
	 * @throws Exception
	 */
	private void performD01(Point2D.Double coordinate, Point2D.Double arcCoordinate) throws Exception
	{
		if (this.interpolationMode == null)
			this.error("Interpolation most must be set before using D02");

		if (this.regionMode)
		{
			if (this.interpolationMode == InterpolationMode.Linear)
				this.addRegionLine(coordinate);
			else
				this.addRegionArc(coordinate, arcCoordinate);
		} else if (this.interpolationMode == InterpolationMode.Linear)
		{
			this.parseStatistics.lineCount++;
			this.warn("Linear interpolation not yet supported");
		} else
		{
			this.parseStatistics.arcCount++;
			this.warn("Circular interpolation not yet supported");
		}
		this.currentPoint = coordinate;
	}

	/**
	 * Move / set the current coordinate. Additionally, in region mode end the
	 * current contour.
	 * 
	 * @param coordinate
	 * @throws Exception
	 */
	private void performD02(Point2D.Double coordinate) throws Exception
	{
		if (this.interpolationMode == null)
			this.error("Interpolation mode must be set before using D02");

		if (this.regionMode)
			this.closeRegion();

		this.currentPoint = coordinate;
	}

	/**
	 * Flash the current aperture at the given coordinate.
	 * 
	 * @param coordinate
	 * @throws Exception
	 */
	private void performD03(Point2D.Double coordinate) throws Exception
	{
		if (this.currentAperture == null)
			this.error("Can't flash, no current aperture");
		if (this.regionMode)
			this.error("Can't flash in region mode");

		this.parseStatistics.flashCount++;

		Integer counter = this.apertureUseCounts.get(this.currentAperture.getIndex());
		if (counter == null)
			counter = 0;
		else
			counter++;
		this.apertureUseCounts.put(this.currentAperture.getIndex(), counter);

		BoardPad pad = this.currentAperture.createPad(this.unit, coordinate);
		pad.setName(String.format("D%02d-%03d", this.currentAperture.getIndex(), counter++));
		this.pads.add(pad);
		this.parseStatistics.padCount++;

		this.currentPoint = coordinate;

		this.parseStatistics.flashPerformedCount++;
	}

	/**
	 * Read the next character in the stream, skipping any \r or \n that precede
	 * it.
	 * 
	 * @return
	 * @throws Exception
	 */
	private int read() throws Exception
	{
		this.skipCrLf();
		int ch = this.reader.read();
		if (ch == -1)
			this.error("Unexpected end of stream");
		return ch;
	}

	private void readApertureDefinition() throws Exception
	{
		int ch;
		if (this.read() != 'D')
			this.error("Expected aperture D code");

		int code = this.readInteger();
		int type = this.read();
		Aperture aperture = null;
		switch (type)
		{
			case 'R':
			{
				aperture = this.readRectangleApertureDefinition(code);
				break;
			}
			case 'C':
			{
				aperture = this.readCircleApertureDefinition(code);
				break;
			}
			case 'O':
			{
				aperture = this.readObroundApertureDefinition(code);
				break;
			}
			case 'P':
			{
				aperture = this.readPolygonApertureDefinition(code);
				break;
			}
			default:
			{
				this.error(String.format("Unhandled aperture definition type %c, code %d", (char) type, code));
			}
		}
		this.apertures.put(code, aperture);
	}

	private Aperture readCircleApertureDefinition(int index) throws Exception
	{
		if (this.read() != ',')
			this.error("Expected , in circle aperture definition");
		double diameter = this.readDecimal();
		Double holeDiameter = null;
		if (this.peek() == 'X')
		{
			this.read();
			holeDiameter = this.readDecimal();
		}
		if (this.read() != '*')
			this.error("Expected end of data block");
		return new CircleAperture(index, diameter, holeDiameter);
	}

	private void readCommand() throws Exception
	{
		if (this.peek() == '%')
			this.readExtendedCodeCommand();
		else
			this.readFunctionCodeCommand();
	}

	private void readCoordinateFormat() throws Exception
	{
		int ch;

		while ("LTAI".indexOf((char) this.peek()) != -1)
		{
			ch = this.read();
			switch (ch)
			{
				case 'L':
				{
					this.coordinateFormatTrailingZeroOmission = false;
					break;
				}
				case 'T':
				{
					this.coordinateFormatTrailingZeroOmission = true;
					break;
				}
				case 'A':
				{
					this.coordinateFormatIncremental = false;
					break;
				}
				case 'I':
				{
					this.coordinateFormatIncremental = true;
					break;
				}
			}
		}

		int xI, xD, yI, yD;

		ch = this.read();
		if (ch != 'X')
			this.error("Expected X coordinate format");
		xI = this.read() - '0';
		if (xI < 0 || xI > 6)
			this.warn("Invalid coordinate format, X integer part {}, should be >= 0 && <= 6", xI);
		xD = this.read() - '0';
		if (xD < 4 || xD > 6)
			this.warn("Invalid coordinate format, X decimal part {}, should be >= 4 && <= 6", xD);

		ch = this.read();
		if (ch != 'Y')
			this.error("Expected Y coordinate format");
		yI = this.read() - '0';
		if (yI < 0 || yI > 6)
			this.warn("Invalid coordinate format, Y integer part {}, should be >= 0 && <= 6", yI);
		yD = this.read() - '0';
		if (yD < 4 || yD > 6)
			this.warn("Invalid coordinate format, Y decimal part {}, should be >= 4 && <= 6", yD);

		if (xI != yI || xD != yD)
			this.error(String.format("Coordinate format X does not match Y: %d.%d != %d.%d", xI, xD, yI, yD));

		this.coordinateFormatIntegerLength = xI;
		this.coordinateFormatDecimalLength = xD;

		if (this.read() != '*')
			this.error("Expected end of data block");
	}

	private double readCoordinateValue() throws Exception
	{
		if (this.coordinateFormatIncremental)
			this.error("Incremental coordinate format not supported");
		if (this.coordinateFormatTrailingZeroOmission)
			this.error("Trailing zero omission not supported");
		if (this.coordinateFormatIntegerLength == -1 || this.coordinateFormatDecimalLength == -1)
			this.error("Coordinate format not specified.");
		// Read the value as an integer first, since this will read until it
		// hits
		// something that isn't an integer character, then pad it out and then
		// break up the components.
		int value = this.readInteger();
		String sValue = Integer.toString(Math.abs(value));
		while (sValue.length() < this.coordinateFormatIntegerLength + this.coordinateFormatDecimalLength)
			sValue = "0" + sValue;
		String integerPart = sValue.substring(0, this.coordinateFormatIntegerLength);
		String decimalPart = sValue.substring(this.coordinateFormatIntegerLength, this.coordinateFormatIntegerLength + this.coordinateFormatDecimalLength - 1);
		return (value < 0 ? -1 : 1) * Double.parseDouble(integerPart + "." + decimalPart);
	}

	private void readDcode(Point2D.Double coordinate, Point2D.Double arcCoordinate) throws Exception
	{
		int code = this.readInteger();
		switch (code)
		{
			case 1:
			{
				this.performD01(coordinate, arcCoordinate);
				break;
			}
			case 2:
			{
				this.performD02(coordinate);
				break;
			}
			case 3:
			{
				this.performD03(coordinate);
				break;
			}
			default:
			{
				if (code < 10)
					this.error("Unknown reserved D code " + code);
				// anything else is an aperture code, so look up the aperture
				// and set it as the current
				this.currentAperture = this.apertures.get(code);
				if (this.currentAperture == null)
					this.warn("Unknown aperture " + code);
			}
		}
	}

	private double readDecimal() throws Exception
	{
		boolean negative = false;
		int ch = this.peek();
		if (ch == '-')
		{
			negative = true;
			this.read();
		} else if (ch == '+')
			this.read();
		StringBuffer sb = new StringBuffer();
		String allowed = "0123456789.";
		while (allowed.indexOf(this.peek()) != -1)
			sb.append((char) this.read());
		return (negative ? -1 : 1) * Double.parseDouble(sb.toString());
	}

	private void readExtendedCodeCommand() throws Exception
	{
		if (this.read() != '%')
			this.error("Expected start of extended code command");
		String code = "" + (char) this.read() + (char) this.read();
		switch (code)
		{
			case "FS":
			{
				// Sets the ‘Coordinate format’ graphics state parameter. See
				// 4.9.
				// These commands are mandatory and must be used only once, in
				// the header of a file.
				// Example: FSLAX24Y24
				this.readCoordinateFormat();
				break;
			}
			case "MO":
			{
				// Sets the ‘Unit’ graphics state parameter. See 4.10.
				this.readUnit();
				break;
			}
			case "AD":
			{
				// Assigns a D code number to an aperture definition. See 4.11.
				// These commands can be used multiple times. It is recommended
				// to put them in
				// header of a file.
				this.readApertureDefinition();
				break;
			}
			case "AM":
			{
				// Defines macro apertures which can be referenced from the AD
				// command. See 4.12.
				// TODO: We just ignore them for now.
				while (this.peek() != '%')
				{
					this.readUntil('*');
					this.read();
				}
				break;
			}
			case "SR":
			{
				// Sets the ‘Step and Repeat’ graphics state parameter. See
				// 4.13.
				// These commands can be used multiple times over the whole
				// file.
				this.readUntil('*');
				this.read();
				break;
			}
			case "LP":
			{
				// Starts a new level and sets the ‘Level polarity’ graphics
				// state parameter. See
				// 4.14.
				this.readUntil('*');
				this.read();
				break;
			}
			case "AS":
			{
				// Deprecated axis select, ignore
				this.readUntil('*');
				this.read();
				break;
			}
			case "IN":
			{
				// Deprecated image name, ignore
				this.readUntil('*');
				this.read();
				break;
			}
			case "IP":
			{
				// Deprecated image polarity, ignore
				this.readUntil('*');
				this.read();
				break;
			}
			case "IR":
			{
				// Deprecated image rotation, ignore
				this.readUntil('*');
				this.read();
				break;
			}
			case "LN":
			{
				// Deprecated level name, ignore
				this.readUntil('*');
				this.read();
				break;
			}
			case "MI":
			{
				// Deprecated mirror image, ignore
				this.readUntil('*');
				this.read();
				break;
			}
			case "OF":
			{
				// Deprecated offset, ignore
				this.readUntil('*');
				this.read();
				break;
			}
			case "SF":
			{
				// Deprecated scale factor, ignore
				this.readUntil('*');
				this.read();
				break;
			}
			default:
			{
				this.warn("Unknown extended command code " + code);
				while (this.peek() != '%')
					this.read();
			}
		}
		if (this.read() != '%')
			this.error("Expected end of extended code command");
	}

	private void readFunctionCodeCommand() throws Exception
	{
		// a command is either a D, G, M or coordinate data
		// followed by a D.
		// X, Y
		// TODO: Make sure this becomes the current point.
		Point2D.Double coordinate = new Point2D.Double(this.currentPoint.getX(), this.currentPoint.getY());
		// I, J
		Point2D.Double arcCoordinate = new Point2D.Double(0, 0);
		while (!this.stopped)
		{
			int ch = this.read();
			switch (ch)
			{
				case '*':
				{
					// Empty block or end of block that was not terminated
					// from a previous read.
					return;
				}
				case 'D':
				{
					this.readDcode(coordinate, arcCoordinate);
					return;
				}
				case 'G':
				{
					this.readGcode();
					return;
				}
				case 'M':
				{
					this.readMcode();
					return;
				}
					// TODO: See 7.2 Coordinate Data without Operation Code
				case 'X':
				{
					coordinate.x = this.readCoordinateValue();
					break;
				}
				case 'Y':
				{
					coordinate.y = this.readCoordinateValue();
					break;
				}
				case 'I':
				{
					arcCoordinate.x = this.readCoordinateValue();
					break;
				}
				case 'J':
				{
					arcCoordinate.y = this.readCoordinateValue();
					break;
				}
				default:
				{
					this.error("Unknown function code " + (char) ch);
				}
			}
		}
	}

	// G54D06
	private void readGcode() throws Exception
	{
		int code = this.readInteger();
		switch (code)
		{
			case 1:
			{
				this.interpolationMode = InterpolationMode.Linear;
				break;
			}
			case 2:
			{
				this.interpolationMode = InterpolationMode.Clockwise;
				break;
			}
			case 3:
			{
				this.interpolationMode = InterpolationMode.CounterClockwise;
				break;
			}
			case 4:
			{
				// comment, ignore
				this.readUntil('*');
				break;
			}
			case 36:
			{
				this.enableRegionMode();
				break;
			}
			case 37:
			{
				this.disableRegionMode();
				break;
			}
			case 54:
			{
				// deprecated, prepare to select aperture
				break;
			}
			case 55:
			{
				// deprecated, prepare for flash
				break;
			}
			case 70:
			{
				// deprecated, unit inch
				this.unit = LengthUnit.Inches;
				break;
			}
			case 71:
			{
				// deprecated, unit mm
				this.unit = LengthUnit.Millimeters;
				break;
			}
			case 74:
			{
				this.multiQuadrantMode = false;
				break;
			}
			case 75:
			{
				this.multiQuadrantMode = true;
				break;
			}
			case 90:
			{
				// deprecated, absolute coordinate mode
				this.coordinateFormatIncremental = false;
				break;
			}
			case 91:
			{
				// deprecated, incremental coordinate mode
				this.coordinateFormatIncremental = true;
				break;
			}
			default:
			{
				this.warn("Unknown G code " + code);
			}
		}
	}

	private int readInteger() throws Exception
	{
		boolean negative = false;
		int ch = this.peek();
		if (ch == '-')
		{
			negative = true;
			this.read();
		} else if (ch == '+')
			this.read();
		StringBuffer sb = new StringBuffer();
		String allowed = "0123456789";
		while (allowed.indexOf(this.peek()) != -1)
			sb.append((char) this.read());
		return (negative ? -1 : 1) * Integer.parseInt(sb.toString());
	}

	private void readMcode() throws Exception
	{
		int code = this.readInteger();
		switch (code)
		{
			case 0:
			{
				// deprecated version of stop command
				this.stopped = true;
				break;
			}
			case 2:
			{
				this.stopped = true;
				break;
			}
			case 1:
			{
				// deprecated, does nothing
				break;
			}
			default:
			{
				this.warn("Unknown M code " + code);
			}
		}
	}

	private Aperture readObroundApertureDefinition(int index) throws Exception
	{
		if (this.read() != ',')
			this.error("Expected , in obround aperture definition");
		double width = this.readDecimal();
		if (this.read() != 'X')
			this.error("Expected X in obround aperture definition");
		double height = this.readDecimal();
		Double holeDiameter = null;
		if (this.peek() == 'X')
		{
			this.read();
			holeDiameter = this.readDecimal();
		}
		if (this.read() != '*')
			this.error("Expected end of data block");
		return new ObroundAperture(index, width, height, holeDiameter);
	}

	private Aperture readPolygonApertureDefinition(int index) throws Exception
	{
		if (this.read() != ',')
			this.error("Expected , in circle aperture definition");

		double diameter = this.readDecimal();

		if (this.read() != 'X')
			this.error("Expected X in polygon aperture definition");
		int numberOfVertices = this.readInteger();

		Double rotation = null;
		if (this.peek() == 'X')
		{
			this.read();
			rotation = this.readDecimal();
		}

		Double holeDiameter = null;
		if (this.peek() == 'X')
		{
			this.read();
			holeDiameter = this.readDecimal();
		}
		if (this.read() != '*')
			this.error("Expected end of data block");
		return new PolygonAperture(index, diameter, numberOfVertices, rotation, holeDiameter);
	}

	private Aperture readRectangleApertureDefinition(int index) throws Exception
	{
		if (this.read() != ',')
			this.error("Expected , in rectangle aperture definition");
		double width = this.readDecimal();
		if (this.read() != 'X')
			this.error("Expected X in rectangle aperture definition");
		double height = this.readDecimal();
		Double holeDiameter = null;
		if (this.peek() == 'X')
		{
			this.read();
			holeDiameter = this.readDecimal();
		}
		if (this.read() != '*')
			this.error("Expected end of data block");
		return new RectangleAperture(index, width, height, holeDiameter);
	}

	private String readString(int length) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++)
			sb.append((char) this.read());
		return sb.toString();
	}

	private void readUnit() throws Exception
	{
		String unitCode = this.readString(2);
		if (unitCode.equals("MM"))
			this.unit = LengthUnit.Millimeters;
		else if (unitCode.equals("IN"))
			this.unit = LengthUnit.Inches;
		else
			this.error("Unknown unit code " + unitCode);

		if (this.read() != '*')
			this.error("Expected end of data block");
	}

	private String readUntil(int ch) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		while (this.peek() != ch)
			sb.append((char) this.read());
		return sb.toString();
	}

	private void reset()
	{
		this.unit = null;
		this.currentAperture = null;
		this.currentPoint = new Point2D.Double(0, 0);
		this.levelPolarity = LevelPolarity.Dark;
		/*
		 * This is non-standard, but expected by Eagle, at least. The standard
		 * says that interpolation mode is undefined at the start of the file
		 * but Eagle does not appear to send a G01 at any point before it starts
		 * sending D01s.
		 */
		this.interpolationMode = InterpolationMode.Linear;
		this.stopped = false;
		this.regionMode = false;
		this.coordinateFormatIntegerLength = -1;
		this.coordinateFormatDecimalLength = -1;
		this.coordinateFormatTrailingZeroOmission = false;
		this.coordinateFormatIncremental = false;
		this.apertures = new HashMap<>();
		this.lineNumber = 1;
		this.pads = new ArrayList<>();
		this.regionStarted = false;
		this.apertureUseCounts = new HashMap<>();

		this.parseStatistics = new ParseStatistics();
	}

	/**
	 * Consume any number of \r or \n, stopping when another character is found.
	 * 
	 * @throws Exception
	 */
	private void skipCrLf() throws Exception
	{
		while (true)
		{
			int ch = this._peek();
			if (ch == '\n')
			{
				this.lineNumber++;
				this.reader.read();
			} else if (ch == '\r')
				this.reader.read();
			else
				return;
		}
	}

	private void warn(String s)
	{
		Rs274xParser.logger.warn("WARNING: " + this.lineNumber + ": " + s);
	}

	private void warn(String fmt, Object o1)
	{
		Rs274xParser.logger.warn("WARNING: " + this.lineNumber + ": " + fmt, o1);
	}

	private void warn(String fmt, Object o1, Object o2)
	{
		Rs274xParser.logger.warn("WARNING: " + this.lineNumber + ": " + fmt, o1, o2);
	}

	private void warn(String fmt, Object[] o)
	{
		Rs274xParser.logger.warn("WARNING: " + this.lineNumber + ": " + fmt, o);
	}
}
