package abinns.appserv.appmanager.parser;

import java.util.HashMap;
import java.util.LinkedList;

public class ParsedElement
{
	private String					value;
	private HashMap<String, String>	attrs		= new HashMap<String, String>();
	private LinkedList<String>		attrList	= new LinkedList<String>();
	private String					type;

	public ParsedElement(String type, String value)
	{
		this.value = value;
		this.type = type;
	}

	public void addAttr(String attribute, String value)
	{
		this.attrs.put(attribute, value);
		this.attrList.add(attribute);
	}

	public LinkedList<String> getAttrList()
	{
		return this.attrList;
	}

	public HashMap<String, String> getAttrs()
	{
		return this.attrs;
	}

	public String getType()
	{
		return this.type;
	}

	public String getValue()
	{
		return this.value;
	}

	@Override
	public String toString()
	{
		return "[" + this.type + "] " + this.value + " - " + this.attrs;
	}
}
