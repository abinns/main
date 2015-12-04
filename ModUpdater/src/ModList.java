import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class ModList
{

	private static final Set<String> ignored;

	static
	{
		ignored = new HashSet<>();
		// In-game wiki mod
		ignored.add("IGWMod");
		// Custom Main Menu
		ignored.add("CustomMainMenu");
		ignored.add("BetterFoliage");
		ignored.add("ResourceLoader");
		ignored.add("babyanimals");
		ignored.add("MouseTweaks");
		ignored.add("journeymap");
	}

	private List<Path>			noCompare;
	private Map<String, Path>	files;
	private Map<String, String>	keyToModMapping;

	public ModList(String directory) throws JsonSyntaxException, IOException
	{
		this.noCompare = new ArrayList<>();
		this.files = new TreeMap<>();
		this.keyToModMapping = new TreeMap<>();
		JsonParser parser = new JsonParser();
		Files.walk(Paths.get(".").resolve(directory), 1).filter(cur -> cur.getFileName().toString().endsWith(".jar")).forEach(cur -> {
			JsonElement elem = null;
			String data = getInfo(cur.toString());
			if (data == null)
				this.noCompare.add(cur);
			else
			{
				elem = parser.parse(data);

				String[] keys = getIdent(elem);
				if (!ignored.contains(keys[1]))
				{
					this.files.put(keys[0], cur);
					this.keyToModMapping.put(keys[1], keys[0]);
				}
			}
		});
	}

	public Map<Path, Path> getDiffs(ModList other)
	{
		Map<Path, Path> res = new LinkedHashMap<>();
		for (Entry<String, String> cur : this.keyToModMapping.entrySet())
			if (other.keyToModMapping.containsKey(cur.getKey()))
				if (!other.files.containsKey(cur.getValue()))
					res.put(this.files.get(cur.getValue()), other.files.get(other.keyToModMapping.get(cur.getKey())));
		return res;
	}

	private String[] getIdent(JsonElement elem)
	{
		StringBuilder key = new StringBuilder();
		JsonArray arr = null;
		String modName = "";
		if (elem.isJsonArray())
			arr = elem.getAsJsonArray();
		else if (elem.isJsonObject())
			arr = elem.getAsJsonObject().get("modList").getAsJsonArray();
		for (JsonElement mod : arr)
		{
			JsonObject obj = mod.getAsJsonObject();
			modName = obj.get("modid").getAsString();
			key.append(modName);
			if (obj.has("version"))
				key.append(obj.get("version").getAsString());
		}
		return new String[]
		{ key.toString(), modName };
	}

	private String getInfo(String filename)
	{
		try
		{
			JarFile jar = new JarFile(filename);
			ZipEntry info = jar.getEntry("mcmod.info");
			if (info == null)
			{
				jar.close();
				return null;
			}
			byte[] data = new byte[(int) info.getSize()];
			DataInputStream in = new DataInputStream(jar.getInputStream(info));
			in.readFully(data);
			jar.close();
			return new String(data);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return "";
	}

	public List<String> getAdditions(ModList other)
	{
		List<String> res = new ArrayList<>();
		for (String cur : this.keyToModMapping.keySet())
			if (!other.keyToModMapping.containsKey(cur))
				res.add(cur);
		Collections.sort(res);
		return res;
	}
}
