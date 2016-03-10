//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2014.10.23 at 08:50:01 AM PDT
//

package org.openpnp.model.eagle.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder =
{ "connects", "technologies" })
@XmlRootElement(name = "device")
public class Device
{

	@XmlAttribute(name = "name")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String		name;
	@XmlAttribute(name = "package")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String		_package;
	protected Connects		connects;
	protected Technologies	technologies;

	/**
	 * Gets the value of the connects property.
	 * 
	 * @return possible object is {@link Connects }
	 */
	public Connects getConnects()
	{
		return this.connects;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getName()
	{
		if (this.name == null)
			return "";
		else
			return this.name;
	}

	/**
	 * Gets the value of the package property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getPackage()
	{
		return this._package;
	}

	/**
	 * Gets the value of the technologies property.
	 * 
	 * @return possible object is {@link Technologies }
	 */
	public Technologies getTechnologies()
	{
		return this.technologies;
	}

	/**
	 * Sets the value of the connects property.
	 * 
	 * @param value
	 *            allowed object is {@link Connects }
	 */
	public void setConnects(Connects value)
	{
		this.connects = value;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setName(String value)
	{
		this.name = value;
	}

	/**
	 * Sets the value of the package property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setPackage(String value)
	{
		this._package = value;
	}

	/**
	 * Sets the value of the technologies property.
	 * 
	 * @param value
	 *            allowed object is {@link Technologies }
	 */
	public void setTechnologies(Technologies value)
	{
		this.technologies = value;
	}

}
