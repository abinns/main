package racesim;

import java.util.ArrayList;
import java.util.List;

public class RaceParams
{
	private int					length;
	private List<Contestant>	racers;
	private boolean				displayRounds;

	public RaceParams()
	{
		this.length = 100;
		this.displayRounds = true;
		this.racers = new ArrayList<Contestant>();
	}

	public void addContestant(Contestant cur)
	{
		this.racers.add(cur);
	}

	public int getContestentCount()
	{
		return this.racers.size();
	}

	public List<Contestant> getContestents()
	{
		return this.racers;
	}

	public int getLength()
	{
		return this.length;
	}

	public boolean getRoundDisplay()
	{
		return this.displayRounds;
	}

	public void setLength(int len)
	{
		this.length = len;
	}

	public void setRoundDisplay(boolean b)
	{
		this.displayRounds = b;
	}

}