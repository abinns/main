package races;

import java.util.HashMap;
import java.util.Map;

import racesim.Contestant;
import racesim.Race;
import racesim.RaceParams;
import racesim.Racer;
import extra.RandomCollection;

public class RandomMoves
{
	public static void main(String... cheese)
	{
		RaceParams params = new RaceParams();
		params.setLength(70);
		final RandomCollection<Integer> tortiseMoves = new RandomCollection<Integer>();
		final RandomCollection<Integer> rabbitMoves = new RandomCollection<Integer>();
		// Equal weights, can substitute 1 for any value to have different
		// results
		tortiseMoves.add(1, -1);
		tortiseMoves.add(1, 0);
		tortiseMoves.add(1, 1);
		tortiseMoves.add(1, 2);

		rabbitMoves.add(1, -2);
		rabbitMoves.add(1, -1);
		rabbitMoves.add(1, 3);
		rabbitMoves.add(1, 4);

		params.addContestant(new Racer('T', "Tortoise", seed -> {
			return tortiseMoves.next();
		}));
		params.addContestant(new Racer('R', "Rabbit", seed -> {
			return rabbitMoves.next();
		}));
		// Disable printing of the status each round for simulation speed
		params.setRoundDisplay(false);

		Race race = new Race(params);

		// init the wincount histogram
		Map<String, Integer> histo = new HashMap<String, Integer>();
		for (Contestant cur : params.getContestents())
			histo.put(cur.getName(), 0);

		// Run the race a lot of times
		for (int i = 0; i < 2000; i++)
		{
			race.run();
			// Inc the winner's value (Could be done faster probably, but
			// shouldn't really matter too much.)
			histo.put(race.getWinner().getRacerName(), histo.get(race.getWinner().getRacerName()) + 1);
			race.reset();
		}

		System.out.println("Win Histogram:");
		System.out.println(histo);
	}
}
