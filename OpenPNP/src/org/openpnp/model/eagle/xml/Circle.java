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
@XmlType(name = "")
@XmlRootElement(name = "circle")
public class Circle
{

	@XmlAttribute(name = "x", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	x;
	@XmlAttribute(name = "y", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	y;
	@XmlAttribute(name = "radius", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	radius;
	@XmlAttribute(name = "width", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	width;
	@XmlAttribute(name = "layer", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	layer;

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
	 * Gets the value of the radius property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getRadius()
	{
		return this.radius;
	}

	/**
	 * Gets the value of the width property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getWidth()
	{
		return this.width;
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
	 * Sets the value of the radius property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setRadius(String value)
	{
		this.radius = value;
	}

	/**
	 * Sets the value of the width property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setWidth(String value)
	{
		this.width = value;
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