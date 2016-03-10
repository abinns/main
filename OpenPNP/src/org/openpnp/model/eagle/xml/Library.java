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
{ "description", "packages", "symbols", "devicesets" })
@XmlRootElement(name = "library")
public class Library
{

	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String		name;
	protected Description	description;
	protected Packages		packages;
	protected Symbols		symbols;
	protected Devicesets	devicesets;

	/**
	 * Gets the value of the description property.
	 * 
	 * @return possible object is {@link Description }
	 */
	public Description getDescription()
	{
		return this.description;
	}

	/**
	 * Gets the value of the devicesets property.
	 * 
	 * @return possible object is {@link Devicesets }
	 */
	public Devicesets getDevicesets()
	{
		return this.devicesets;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Gets the value of the packages property.
	 * 
	 * @return possible object is {@link Packages }
	 */
	public Packages getPackages()
	{
		return this.packages;
	}

	/**
	 * Gets the value of the symbols property.
	 * 
	 * @return possible object is {@link Symbols }
	 */
	public Symbols getSymbols()
	{
		return this.symbols;
	}

	/**
	 * Sets the value of the description property.
	 * 
	 * @param value
	 *            allowed object is {@link Description }
	 */
	public void setDescription(Description value)
	{
		this.description = value;
	}

	/**
	 * Sets the value of the devicesets property.
	 * 
	 * @param value
	 *            allowed object is {@link Devicesets }
	 */
	public void setDevicesets(Devicesets value)
	{
		this.devicesets = value;
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
	 * Sets the value of the packages property.
	 * 
	 * @param value
	 *            allowed object is {@link Packages }
	 */
	public void setPackages(Packages value)
	{
		this.packages = value;
	}

	/**
	 * Sets the value of the symbols property.
	 * 
	 * @param value
	 *            allowed object is {@link Symbols }
	 */
	public void setSymbols(Symbols value)
	{
		this.symbols = value;
	}

}