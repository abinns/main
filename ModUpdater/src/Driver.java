import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import backend.U;

public class Driver
{
	public static void main(String... cheese) throws IOException
	{
		String src = "G:\\Misc\\Apps\\CurseMC\\Minecraft\\Instances\\Sudo's Mystical Concoction\\mods\\";
		String dst = "G:\\Misc\\Projects\\MC\\MysticalConcoction\\Server\\mods";

		ModList srcList = new ModList(src);
		ModList dstList = new ModList(dst);

		Map<Path, Path> diffs = srcList.getDiffs(dstList);
		if (!diffs.isEmpty())
		{
			U.p("Found updates for:");
			for (Entry<Path, Path> cur : diffs.entrySet())
				U.p(cur.getValue());
			U.p("The above will be replaced with:");
			for (Entry<Path, Path> cur : diffs.entrySet())
				U.p(cur.getKey());

			if (U.confirm("Continue?"))
				for (Entry<Path, Path> cur : diffs.entrySet())
				{
					Files.delete(cur.getValue());
					Files.copy(cur.getKey(), cur.getValue().resolveSibling(cur.getKey().getFileName()));
				}
		}

		List<String> newItems = srcList.getAdditions(dstList);
		if (!newItems.isEmpty())
		{
			U.p("Additional mods:\n");
			for (String cur : newItems)
				U.p(cur);
		}
		U.p("");
		List<String> missingItems = dstList.getAdditions(srcList);
		if (!missingItems.isEmpty())
		{
			U.p("Missing mods:\n");
			for (String cur : missingItems)
				U.p(cur);
		}
		U.p("");
		U.p("Compare complete.");
	}
}
