package races;

import racesim.Race;
import racesim.RaceParams;
import racesim.Racer;

public class LoopedMoves
{
	public static void main(String... cheese)
	{
		RaceParams params = new RaceParams();
		// set the length of the racetrack
		params.setLength(200);
		// add our contestents to the race setup
		params.addContestant(new Racer('T', "Tortoise", seed -> {
			int[] moves = new int[]
			{ -1, 0, 1, 2 };
			return moves[seed % moves.length];
		}));
		params.addContestant(new Racer('R', "Rabbit", seed -> {
			int[] moves = new int[]
			{ -2, -1, 3, 4 };
			return moves[seed % moves.length];
		}));

		// Init the race
		Race race = new Race(params);
		// Run the race!
		race.run();

	}
}
