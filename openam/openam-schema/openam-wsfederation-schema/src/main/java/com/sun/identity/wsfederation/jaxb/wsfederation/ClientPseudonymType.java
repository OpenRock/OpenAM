//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.6-b27-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.11 at 10:34:16 AM PDT 
//


package com.sun.identity.wsfederation.jaxb.wsfederation;


/**
 * Java content class for ClientPseudonymType complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/Users/allan/A-SVN/trunk/opensso/products/federation/library/xsd/wsfederation/ws-federation.xsd line 344)
 * <p>
 * <pre>
 * &lt;complexType name="ClientPseudonymType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PPID" type="{http://schemas.xmlsoap.org/ws/2006/12/federation}AttributeExtensibleString" minOccurs="0"/>
 *         &lt;element name="DisplayName" type="{http://schemas.xmlsoap.org/ws/2006/12/federation}AttributeExtensibleString" minOccurs="0"/>
 *         &lt;element name="EMail" type="{http://schemas.xmlsoap.org/ws/2006/12/federation}AttributeExtensibleString" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface ClientPseudonymType {


    /**
     * Gets the value of the Any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the Any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.Object}
     * 
     */
    java.util.List getAny();

    /**
     * Gets the value of the eMail property.
     * 
     * @return
     *     possible object is
     *     {@link com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString}
     */
    com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString getEMail();

    /**
     * Sets the value of the eMail property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString}
     */
    void setEMail(com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString value);

    /**
     * Gets the value of the ppid property.
     * 
     * @return
     *     possible object is
     *     {@link com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString}
     */
    com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString getPPID();

    /**
     * Sets the value of the ppid property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString}
     */
    void setPPID(com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString value);

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString}
     */
    com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString getDisplayName();

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString}
     */
    void setDisplayName(com.sun.identity.wsfederation.jaxb.wsfederation.AttributeExtensibleString value);

}