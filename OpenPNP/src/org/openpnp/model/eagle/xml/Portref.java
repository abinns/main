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
@XmlRootElement(name = "portref")
public class Portref
{

	@XmlAttribute(name = "moduleinst", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	moduleinst;
	@XmlAttribute(name = "port", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String	port;

	/**
	 * Gets the value of the moduleinst property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getModuleinst()
	{
		return this.moduleinst;
	}

	/**
	 * Gets the value of the port property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getPort()
	{
		return this.port;
	}

	/**
	 * Sets the value of the moduleinst property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setModuleinst(String value)
	{
		this.moduleinst = value;
	}

	/**
	 * Sets the value of the port property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setPort(String value)
	{
		this.port = value;
	}

}
