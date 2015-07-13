package racesim;

import java.util.function.IntUnaryOperator;

public class Racer implements Contestant
{
	private char				marker;
	private IntUnaryOperator	supplier;
	private int					seed;
	private String				name;

	public Racer(char marker, String name, IntUnaryOperator moveGen)
	{
		this.supplier = moveGen;
		this.marker = marker;
		this.seed = 0;
		this.name = name;
	}

	@Override
	public char getMarker()
	{
		return this.marker;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public int getNextMove()
	{
		return this.supplier.applyAsInt(this.seed++);
	}
}
