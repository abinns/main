package config.core;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;

//Static import of U not used due to cleanup and proper styling, as well as basic readability.
import backend.U;
import config.core.ExportedParam.MType;
import global.Globals;

/**
 * <p>
 * Testbed config, uses the JSON parser to parse different parts of a given
 * config file. Maintains ordering of original config file, as well as adding
 * config sections during writing out that were not in the original config file.
 * </p>
 * <p>
 * Rest under development.
 * </p>
 * <p>
 * Actually uses reflection to scan and look over the entire classpath and find
 * things with the @ConfigMember annotation.
 * </p>
 * 
 * @see ConfigMember
 * @see ExportedParam
 * @see ExportedParameter
 * @author Andrew Binns
 */
public class Config
{

	/**
	 * Looks through all classes, and finds those with the ConfigMember
	 * annotation. Note, this actually via the tricks of javaassist bytecode
	 * scanning, the sun JDI useful stuff, and the annoventions magic manages to
	 * avoid actually loading all classes in the classpath during scanning. At
	 * least, it should.
	 *
	 * @return a map of classnames to class objects that are annotated with the
	 *         ConfigMember annotation
	 */
	public static Map<String, Class<?>> findConfigMembers()
	{
		Map<String, Class<?>> res = new LinkedHashMap<String, Class<?>>();
		Discoverer discoverer = new ClasspathDiscoverer();
		discoverer.addAnnotationListener(new FinderListener((in) -> {
			try
			{
				res.put(Class.forName(in).getAnnotation(ConfigMember.class).sectionKey(), Class.forName(in));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		} , ConfigMember.class));
		discoverer.discover();
		return res;
	}

	private LinkedHashMap<String, SectionManager>	maps;
	private Map<Class<?>, SectionManager>			classToSectionMap;

	/**
	 * Attempts to load a config from the file passed.
	 *
	 * @param filename
	 *            the file to load a config from
	 */
	public Config(String filename)
	{
		this.maps = new LinkedHashMap<String, SectionManager>();
		this.classToSectionMap = new LinkedHashMap<Class<?>, SectionManager>();
		this.loadConfig(filename);
	}

	/**
	 * Access all config members stored. <b>
	 * <p>
	 * Note: this is the base map, so all changes are backed against the primary
	 * config object.</b>
	 * </p>
	 *
	 * @return
	 */
	public LinkedHashMap<String, SectionManager> getAllMaps()
	{
		// TODO: Don't expose internal data members.
		// TODONT: mistake internal data members for general datastructure, stop
		// trying to make bad code by mass memory copy.
		return this.maps;
	}

	public Map<Class<?>, SectionManager> getClassToSectionManagerMap()
	{
		return this.classToSectionMap;
	}

	/**
	 * Wrapper, simply returns an empty/default JSONArray instead of null
	 *
	 * @param curJSONSection
	 *            the original JSONObject to extract from
	 * @param curKey
	 *            the key to extract from
	 * @return the resulting JSONArray
	 */
	private JSONArray getJSONArr(JSONObject curJSONSection, String curKey)
	{
		JSONArray val;
		val = curJSONSection.optJSONArray(curKey);
		if (val == null)
			val = new JSONArray();
		return val;
	}

	/**
	 * Wrapper, simply returns an empty/default JSONObject instead of null
	 *
	 * @param curJSONSection
	 *            the original JSONObject to extract from
	 * @param curKey
	 *            the key to extract from
	 * @return the resulting JSONObject
	 */
	private JSONObject getJSONObj(JSONObject curJSONSection, String curKey)
	{
		JSONObject obj;
		obj = curJSONSection.optJSONObject(curKey);
		if (obj == null)
			obj = new JSONObject();
		return obj;
	}

	/**
	 * Internal map getter, for a given name either returns, or makes and
	 * returns a hashmap that corresponds to the given key.
	 *
	 * @param name
	 *            the key of the section
	 * @return a section manager for the given type
	 */
	private SectionManager getManager(String name, Class<?> type)
	{
		if (!(type == null))
			if (!this.maps.containsKey(name))
			{
				SectionManager secMan = new SectionManager(type);
				this.maps.put(name, secMan);
				this.classToSectionMap.put(type, secMan);
			}
		return this.maps.get(name);
	}

	/**
	 * Returns a hashmap of the implied type for the specified section.
	 *
	 * @param key
	 *            the key for the config section required
	 * @return the section manager for the given key. Null if not found.
	 */
	public SectionManager getSection(String key)
	{
		return this.getManager(key, null);
	}

	/**
	 * Given a class, finds the section manager that corresponds to the given
	 * type. Returns null if not found.
	 * 
	 * @param type
	 * @return
	 */
	public SectionManager getSectionByClass(Class<?> type)
	{
		return this.classToSectionMap.get(type);
	}

	/**
	 * Intelligently generates a JSONRepresentation of the specified object
	 * based on pre-specified annotations.
	 *
	 * @param object
	 *            the input object to create a representation of
	 * @return the resulting JSON representation
	 */
	private JSONObject intelliGen(Object input, SectionManager secMan)
	{
		JSONObject res = new JSONObject();
		Map<String, ExportedParameter> paramMap = secMan.getParamMappings();
		for (Entry<String, ExportedParameter> curExport : paramMap.entrySet())
			res.putSomeObj(curExport.getKey(), curExport.getValue().call(input, MType.GETTER));
		return res;
	}

	/**
	 * Intelligently parses the specified data from the json object and the
	 * specified type. Uses the standard annotations (ExportedParam) to
	 * correctly parse the methods as needed
	 *
	 * @param data
	 *            the JSON Data to attempt to parse
	 * @param key
	 *            the key for this section
	 * @param type
	 *            the type to attempt to use for this part of the config file
	 */
	private void intelliParse(JSONObject data, String key, Class<?> type)
	{
		JSONObject jsonData = data.optJSONObject(key);
		if (jsonData == null)
			jsonData = new JSONObject();
		SectionManager secMan = this.getManager(key, type);
		for (String cur : jsonData.keySet())
			try
			{
				// Get the current JSON object, instantiate it to the given type
				// and populate the class.
				JSONObject curJSONSection = jsonData.optJSONObject(cur);
				if (curJSONSection == null)
					curJSONSection = new JSONObject();
				Object curInstance = type.newInstance();
				Map<String, ExportedParameter> paramMap = secMan.getParamMappings();
				for (String curKey : curJSONSection.keySet())
					if (paramMap.containsKey(curKey))
						this.parseParam(curJSONSection, curKey, paramMap.get(curKey), curInstance);
					else
						U.d("Dropped extra key found in JSON structure: " + curKey + ".", 1);
				secMan.offer(cur, curInstance);
			} catch (InstantiationException e)
			{
				U.e("Error instantiating class " + type.getName() + ". Make sure you are using the correct type for the key '" + key + "' in the Config class.", e);
			} catch (IllegalAccessException | JSONException e)
			{
				U.e("Issue parsing " + key + " during config loading. Probably an internal error with the \"" + key + "\" handler.");
				e.printStackTrace();
			}
	}

	/**
	 * Based on a given filename, as well as a set of config members, parses
	 * them into their own classes.
	 *
	 * @param filename
	 *            the filename to open
	 */
	private void loadConfig(String filename)
	{
		Map<String, Class<?>> configMembers = Config.findConfigMembers();
		U.p(configMembers);
		try
		{
			JSONObject data = new JSONObject(Globals.readFile(filename));
			U.d("Loading config from data:\n" + data.toString(4), 5);
			for (String curJSONKey : data.keySet())
				try
				{
					Class<?> type = configMembers.get(curJSONKey);
					if (type == null)
						throw new ClassCastException();
					this.intelliParse(data, curJSONKey, type);
				} catch (ClassCastException ex)
				{
					U.e("Error, couldn't find parsing structure for " + curJSONKey + ". Did you spell the name correctly? Or are the correct parseables not listed?");
				}
		} catch (JSONException e)
		{
			U.e("Error parsing config file", e);
			Globals.exit();
		} catch (NullPointerException e)
		{
			U.e("Internal error parsing config file.");
			e.printStackTrace();
			Globals.exit();
		}
	}

	/**
	 * From the given JSONSection, and the specified key, parses the data into
	 * the passed exported parameter.
	 *
	 * @param curJSONSection
	 *            the json section to parse from
	 * @param curKey
	 *            the key to pull the data from
	 * @param curParam
	 *            the parameter to attempt to parse
	 */
	private void parseParam(JSONObject curJSONSection, String curKey, ExportedParameter curParam, Object instance)
	{
		JSONObject obj;
		JSONArray val;
		switch (curParam.getDatatype())
		{
			case SINGLE:
				curParam.call(instance, MType.SETTER, curJSONSection.optString(curKey, ""));
				break;
			case LIST:
				val = this.getJSONArr(curJSONSection, curKey);
				List<String> strList = new LinkedList<String>();
				for (int i = 0; i < val.length(); i++)
					strList.add(val.optString(i, ""));
				curParam.call(instance, MType.SETTER, strList);
				break;
			case MAP:
				obj = this.getJSONObj(curJSONSection, curKey);
				Map<String, String> strMap = new LinkedHashMap<String, String>();
				for (String mapKey : obj.keySet())
					strMap.put(mapKey, obj.getString(mapKey));
				curParam.call(instance, MType.SETTER, strMap);
				break;
			// case OBJ:
			// obj = this.getJSONObj(curJSONSection, curKey);
			default:
				U.d("Unknown exported parameter found.", 2);
				break;
		}
	}

	/**
	 * Basic toString method, simply returns the string representation
	 */
	@Override
	public String toString()
	{
		return this.maps.toString();
	}

	/**
	 * Writes to file, a JSON equivalent of this loaded config.
	 *
	 * @param filename
	 *            the file to export to
	 */
	public void writeToFile(String filename)
	{
		JSONObject output = new JSONObject();
		for (Entry<String, SectionManager> curConfigMember : this.maps.entrySet())
		{
			JSONObject member = new JSONObject();
			SectionManager curSecMan = curConfigMember.getValue();
			for (String curElemKey : curSecMan.getKeys())
				member.putOpt(curElemKey, this.intelliGen(curSecMan.getElem(curElemKey), curSecMan));
			output.putOnce(curConfigMember.getKey(), member);
		}
		U.d("Writing parsed config to file:\n" + output.toString(4), 5);
		U.writeToFile(filename, output.toString(4));
	}
}
