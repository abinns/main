//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2014.10.23 at 08:50:01 AM PDT
//

package org.openpnp.model.eagle.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder =
{ "attribute", "variant" })
@XmlRootElement(name = "element")
public class Element
{

	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String			name;
	@XmlAttribute(name = "library", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String			library;
	@XmlAttribute(name = "package", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String			_package;
	@XmlAttribute(name = "value", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String			value;
	@XmlAttribute(name = "x", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String			x;
	@XmlAttribute(name = "y", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String			y;
	@XmlAttribute(name = "locked")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String			locked;
	@XmlAttribute(name = "populate")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String			populate;
	@XmlAttribute(name = "smashed")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String			smashed;
	@XmlAttribute(name = "rot")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String			rot;
	protected List<Attribute>	attribute;
	protected List<Variant>		variant;

	/**
	 * Gets the value of the attribute property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the attribute property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAttribute().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Attribute
	 * }
	 */
	public List<Attribute> getAttribute()
	{
		if (this.attribute == null)
			this.attribute = new ArrayList<>();
		return this.attribute;
	}

	/**
	 * Gets the value of the library property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getLibrary()
	{
		return this.library;
	}

	/**
	 * Gets the value of the locked property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getLocked()
	{
		if (this.locked == null)
			return "no";
		else
			return this.locked;
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
	 * Gets the value of the package property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getPackage()
	{
		return this._package;
	}

	/**
	 * Gets the value of the populate property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getPopulate()
	{
		if (this.populate == null)
			return "yes";
		else
			return this.populate;
	}

	/**
	 * Gets the value of the rot property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getRot()
	{
		if (this.rot == null)
			return "R0";
		else
			return this.rot;
	}

	/**
	 * Gets the value of the smashed property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getSmashed()
	{
		if (this.smashed == null)
			return "no";
		else
			return this.smashed;
	}

	/**
	 * Gets the value of the value property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getValue()
	{
		return this.value;
	}

	/**
	 * Gets the value of the variant property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the variant property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getVariant().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Variant }
	 */
	public List<Variant> getVariant()
	{
		if (this.variant == null)
			this.variant = new ArrayList<>();
		return this.variant;
	}

	/**
	 * Gets the value of the x property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getX()
	{
		return this.x;
	}

	/**
	 * Gets the value of the y property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getY()
	{
		return this.y;
	}

	/**
	 * Sets the value of the library property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setLibrary(String value)
	{
		this.library = value;
	}

	/**
	 * Sets the value of the locked property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setLocked(String value)
	{
		this.locked = value;
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
	 * Sets the value of the populate property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setPopulate(String value)
	{
		this.populate = value;
	}

	/**
	 * Sets the value of the rot property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setRot(String value)
	{
		this.rot = value;
	}

	/**
	 * Sets the value of the smashed property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setSmashed(String value)
	{
		this.smashed = value;
	}

	/**
	 * Sets the value of the value property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/**
	 * Sets the value of the x property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setX(String value)
	{
		this.x = value;
	}

	/**
	 * Sets the value of the y property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setY(String value)
	{
		this.y = value;
	}

}