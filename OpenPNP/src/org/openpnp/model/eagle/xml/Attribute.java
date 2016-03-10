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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "attribute")
public class Attribute
{

	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	name;
	@XmlAttribute(name = "value")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	value;
	@XmlAttribute(name = "x")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	x;
	@XmlAttribute(name = "y")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	y;
	@XmlAttribute(name = "size")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	size;
	@XmlAttribute(name = "layer")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	layer;
	@XmlAttribute(name = "font")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String	font;
	@XmlAttribute(name = "ratio")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	ratio;
	@XmlAttribute(name = "rot")
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	rot;
	@XmlAttribute(name = "display")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String	display;
	@XmlAttribute(name = "constant")
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	protected String	constant;

	/**
	 * Gets the value of the constant property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getConstant()
	{
		if (this.constant == null)
			return "no";
		else
			return this.constant;
	}

	/**
	 * Gets the value of the display property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getDisplay()
	{
		if (this.display == null)
			return "value";
		else
			return this.display;
	}

	/**
	 * Gets the value of the font property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getFont()
	{
		return this.font;
	}

	/**
	 * Gets the value of the layer property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getLayer()
	{
		return this.layer;
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
	 * Gets the value of the ratio property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getRatio()
	{
		return this.ratio;
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
	 * Gets the value of the size property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getSize()
	{
		return this.size;
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
	 * Sets the value of the constant property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setConstant(String value)
	{
		this.constant = value;
	}

	/**
	 * Sets the value of the display property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setDisplay(String value)
	{
		this.display = value;
	}

	/**
	 * Sets the value of the font property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setFont(String value)
	{
		this.font = value;
	}

	/**
	 * Sets the value of the layer property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setLayer(String value)
	{
		this.layer = value;
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
	 * Sets the value of the ratio property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setRatio(String value)
	{
		this.ratio = value;
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
	 * Sets the value of the size property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setSize(String value)
	{
		this.size = value;
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
