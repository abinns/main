package abinns.appserv.appmanager.parser;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import abinns.appserv.backend.U;

public class Parser
{
	private DocumentBuilderFactory	factory	= DocumentBuilderFactory
													.newInstance();
	private DocumentBuilder			builder;
	private Document				doc;

	public Parser(String filename)
	{
		this(new File(filename));
	}

	public Parser(File file)
	{
		U.p("Loading xml tree from " + file);
		factory.setIgnoringElementContentWhitespace(true);

		try
		{
			builder = factory.newDocumentBuilder();
			doc = builder.parse(file);
		} catch (ParserConfigurationException | SAXException | IOException e1)
		{
			U.e("Error parsing xml file " + file);
		}
	}

	public String getTextFromTag(String tagname)
	{
		Element e = (Element) doc.getElementsByTagName(tagname).item(0);
		if (e != null)
			return e.getFirstChild().getNodeValue();
		return "";
	}

	public LinkedList<ParsedElement> getElementsByTag(String enclosing)
	{
		LinkedList<ParsedElement> res = new LinkedList<ParsedElement>();

		Element elements = (Element) doc.getElementsByTagName(enclosing).item(
				0);
		NodeList list = elements.getChildNodes();
		int elementCount = list.getLength();

		for (int i = 1; i < elementCount; i++)
		{
			Node e = list.item(i);
			if (e.getFirstChild() != null)
			{
				Node first = e.getFirstChild();

				ParsedElement p = new ParsedElement(e.getNodeName(),
						first.getNodeValue());
				NamedNodeMap attrs = e.getAttributes();
				if (attrs != null)
				{
					int n = attrs.getLength();
					for (int j = 0; j < n; j++)
					{
						Node cur = attrs.item(j);
						if (cur != null)
							p.addAttr(cur.getNodeName(), cur.getNodeValue());
					}
				}
				res.add(p);
			}
		}

		return res;
	}
}
