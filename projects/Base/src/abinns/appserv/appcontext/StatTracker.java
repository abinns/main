package abinns.appserv.appcontext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class StatTracker
{
	private HashMap<String, Stat>	stats;

	public StatTracker()
	{
		this.stats = new HashMap<String, Stat>();
	}

	public void inc(String string)
	{
		if (!this.stats.containsKey(string))
			this.stats.put(string, new Stat());
		this.stats.get(string).inc();
	}

	public String toString()
	{
		Iterator<Entry<String, Stat>> iter = this.stats.entrySet().iterator();

		StringBuilder sb = new StringBuilder();

		Consumer<Entry<String, Stat>> statAdder = (Entry<String, Stat> e) -> sb.append(e.getKey() + " - " + e.getValue() + "\n");

		while (iter.hasNext())
			statAdder.accept(iter.next());
		
		return sb.toString();
	}

}
