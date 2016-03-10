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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder =
{ "compatibilityOrDrawing" })
@XmlRootElement(name = "eagle")
public class Eagle
{

	@XmlAttribute(name = "version", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String		version;
	@XmlElements(
	{ @XmlElement(name = "compatibility", type = Compatibility.class), @XmlElement(name = "drawing", type = Drawing.class) })
	protected List<Object>	compatibilityOrDrawing;

	/**
	 * Gets the value of the compatibilityOrDrawing property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the compatibilityOrDrawing property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCompatibilityOrDrawing().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Compatibility } {@link Drawing }
	 */
	public List<Object> getCompatibilityOrDrawing()
	{
		if (this.compatibilityOrDrawing == null)
			this.compatibilityOrDrawing = new ArrayList<>();
		return this.compatibilityOrDrawing;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getVersion()
	{
		return this.version;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setVersion(String value)
	{
		this.version = value;
	}

}
