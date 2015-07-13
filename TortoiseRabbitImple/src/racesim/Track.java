package racesim;

import java.util.Arrays;

public class Track implements Comparable<Track>
{
	private static final char	END_MARKER		= 'F';
	private static final char	TRACK_DEFAULT	= '-';
	private Contestant			racer;
	private int					location;
	private int					length;
	private boolean				autoUpdate;

	public Track(int length, Contestant racer)
	{
		this(length, racer, false);
	}

	public Track(int length, Contestant racer, boolean autoUpdate)
	{
		this.autoUpdate = autoUpdate;
		this.racer = racer;
		this.length = length;
	}

	@Override
	public int compareTo(Track other)
	{
		return this.location - other.location;
	}

	public String genDisp()
	{
		char[] res = new char[this.length];
		Arrays.fill(res, Track.TRACK_DEFAULT);
		res[res.length - 1] = Track.END_MARKER;
		res[this.location] = this.racer.getMarker();
		return new String(res);
	}

	public int getLocation()
	{
		return this.location;
	}

	public String getRacerName()
	{
		return this.racer.getName();
	}

	public boolean hasFinished()
	{
		return this.location == this.length - 1;
	}

	public void reset()
	{
		this.location = 0;
	}

	@Override
	public String toString()
	{
		if (this.autoUpdate)
			this.update();
		return this.genDisp();
	}

	public void update()
	{
		this.location += this.racer.getNextMove();
		this.location = Math.min(this.location, this.length - 1);
		this.location = Math.max(this.location, 0);
	}
}
