package racesim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Race
{
	private List<Track>	tracks;
	private Track		winner;
	private RaceParams	params;

	public Race(RaceParams raceParams)
	{
		this.params = raceParams;
		this.tracks = new ArrayList<Track>(raceParams.getContestentCount());
		for (Contestant cur : raceParams.getContestents())
			this.tracks.add(new Track(raceParams.getLength(), cur));
	}

	public Track getWinner()
	{
		return this.winner;
	}

	public void reset()
	{
		this.winner = null;
		for (Track cur : this.tracks)
			cur.reset();
	}

	public void run()
	{
		int rounds = 0;
		this.winner = null;
		while (this.winner == null)
		{
			for (Track cur : this.tracks)
			{
				cur.update();
				if (cur.hasFinished())
					this.winner = cur;
			}
			if (this.params.getRoundDisplay())
				System.out.println("\nRound " + rounds++ + "\n" + this);
		}
		System.out.println("Finished!");
		System.out.println("Winner was " + this.winner.getRacerName());
		Collections.sort(this.tracks);
	}

	@Override
	public String toString()
	{
		StringBuilder res = new StringBuilder();
		res.append("\n************\n");
		for (Track cur : this.tracks)
			res.append(cur + "\n");
		return res.toString();
	}

	public void update()
	{
		for (Track cur : this.tracks)
			cur.update();
	}
}
