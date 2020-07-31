//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.12 at 11:47:17 AM CEST 
//


package dk.kleonline.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for KLEAdministrativInfoKomponent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="KLEAdministrativInfoKomponent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OprettetDato" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="RettetDato" type="{http://www.w3.org/2001/XMLSchema}date" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Historisk" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;choice>
 *                   &lt;element name="UdgaaetDato" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *                   &lt;element name="Flyttet">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="FlyttetDato" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *                             &lt;element name="AfloestAf" maxOccurs="unbounded" minOccurs="0">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;choice>
 *                                       &lt;element ref="{}HovedgruppeNr"/>
 *                                       &lt;element ref="{}GruppeNr"/>
 *                                       &lt;element ref="{}EmneNr"/>
 *                                       &lt;element ref="{}HandlingsfacetNr"/>
 *                                     &lt;/choice>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/choice>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KLEAdministrativInfoKomponent", propOrder = {
    "oprettetDato",
    "rettetDato",
    "historisk"
})
public class KLEAdministrativInfoKomponent {

    @XmlElement(name = "OprettetDato", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar oprettetDato;
    @XmlElement(name = "RettetDato")
    @XmlSchemaType(name = "date")
    protected List<XMLGregorianCalendar> rettetDato;
    @XmlElement(name = "Historisk")
    protected KLEAdministrativInfoKomponent.Historisk historisk;

    /**
     * Gets the value of the oprettetDato property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOprettetDato() {
        return oprettetDato;
    }

    /**
     * Sets the value of the oprettetDato property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOprettetDato(XMLGregorianCalendar value) {
        this.oprettetDato = value;
    }

    /**
     * Gets the value of the rettetDato property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rettetDato property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRettetDato().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XMLGregorianCalendar }
     * 
     * 
     */
    public List<XMLGregorianCalendar> getRettetDato() {
        if (rettetDato == null) {
            rettetDato = new ArrayList<XMLGregorianCalendar>();
        }
        return this.rettetDato;
    }

    /**
     * Gets the value of the historisk property.
     * 
     * @return
     *     possible object is
     *     {@link KLEAdministrativInfoKomponent.Historisk }
     *     
     */
    public KLEAdministrativInfoKomponent.Historisk getHistorisk() {
        return historisk;
    }

    /**
     * Sets the value of the historisk property.
     * 
     * @param value
     *     allowed object is
     *     {@link KLEAdministrativInfoKomponent.Historisk }
     *     
     */
    public void setHistorisk(KLEAdministrativInfoKomponent.Historisk value) {
        this.historisk = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;choice>
     *         &lt;element name="UdgaaetDato" type="{http://www.w3.org/2001/XMLSchema}date"/>
     *         &lt;element name="Flyttet">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="FlyttetDato" type="{http://www.w3.org/2001/XMLSchema}date"/>
     *                   &lt;element name="AfloestAf" maxOccurs="unbounded" minOccurs="0">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;choice>
     *                             &lt;element ref="{}HovedgruppeNr"/>
     *                             &lt;element ref="{}GruppeNr"/>
     *                             &lt;element ref="{}EmneNr"/>
     *                             &lt;element ref="{}HandlingsfacetNr"/>
     *                           &lt;/choice>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/choice>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "udgaaetDato",
        "flyttet"
    })
    public static class Historisk {

        @XmlElement(name = "UdgaaetDato")
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar udgaaetDato;
        @XmlElement(name = "Flyttet")
        protected KLEAdministrativInfoKomponent.Historisk.Flyttet flyttet;

        /**
         * Gets the value of the udgaaetDato property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getUdgaaetDato() {
            return udgaaetDato;
        }

        /**
         * Sets the value of the udgaaetDato property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setUdgaaetDato(XMLGregorianCalendar value) {
            this.udgaaetDato = value;
        }

        /**
         * Gets the value of the flyttet property.
         * 
         * @return
         *     possible object is
         *     {@link KLEAdministrativInfoKomponent.Historisk.Flyttet }
         *     
         */
        public KLEAdministrativInfoKomponent.Historisk.Flyttet getFlyttet() {
            return flyttet;
        }

        /**
         * Sets the value of the flyttet property.
         * 
         * @param value
         *     allowed object is
         *     {@link KLEAdministrativInfoKomponent.Historisk.Flyttet }
         *     
         */
        public void setFlyttet(KLEAdministrativInfoKomponent.Historisk.Flyttet value) {
            this.flyttet = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="FlyttetDato" type="{http://www.w3.org/2001/XMLSchema}date"/>
         *         &lt;element name="AfloestAf" maxOccurs="unbounded" minOccurs="0">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;choice>
         *                   &lt;element ref="{}HovedgruppeNr"/>
         *                   &lt;element ref="{}GruppeNr"/>
         *                   &lt;element ref="{}EmneNr"/>
         *                   &lt;element ref="{}HandlingsfacetNr"/>
         *                 &lt;/choice>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "flyttetDato",
            "afloestAf"
        })
        public static class Flyttet {

            @XmlElement(name = "FlyttetDato", required = true)
            @XmlSchemaType(name = "date")
            protected XMLGregorianCalendar flyttetDato;
            @XmlElement(name = "AfloestAf")
            protected List<KLEAdministrativInfoKomponent.Historisk.Flyttet.AfloestAf> afloestAf;

            /**
             * Gets the value of the flyttetDato property.
             * 
             * @return
             *     possible object is
             *     {@link XMLGregorianCalendar }
             *     
             */
            public XMLGregorianCalendar getFlyttetDato() {
                return flyttetDato;
            }

            /**
             * Sets the value of the flyttetDato property.
             * 
             * @param value
             *     allowed object is
             *     {@link XMLGregorianCalendar }
             *     
             */
            public void setFlyttetDato(XMLGregorianCalendar value) {
                this.flyttetDato = value;
            }

            /**
             * Gets the value of the afloestAf property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the afloestAf property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getAfloestAf().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link KLEAdministrativInfoKomponent.Historisk.Flyttet.AfloestAf }
             * 
             * 
             */
            public List<KLEAdministrativInfoKomponent.Historisk.Flyttet.AfloestAf> getAfloestAf() {
                if (afloestAf == null) {
                    afloestAf = new ArrayList<KLEAdministrativInfoKomponent.Historisk.Flyttet.AfloestAf>();
                }
                return this.afloestAf;
            }


            /**
             * <p>Java class for anonymous complex type.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;choice>
             *         &lt;element ref="{}HovedgruppeNr"/>
             *         &lt;element ref="{}GruppeNr"/>
             *         &lt;element ref="{}EmneNr"/>
             *         &lt;element ref="{}HandlingsfacetNr"/>
             *       &lt;/choice>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "hovedgruppeNr",
                "gruppeNr",
                "emneNr",
                "handlingsfacetNr"
            })
            public static class AfloestAf {

                @XmlElement(name = "HovedgruppeNr")
                protected String hovedgruppeNr;
                @XmlElement(name = "GruppeNr")
                protected String gruppeNr;
                @XmlElement(name = "EmneNr")
                protected String emneNr;
                @XmlElement(name = "HandlingsfacetNr")
                protected String handlingsfacetNr;

                /**
                 * Gets the value of the hovedgruppeNr property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getHovedgruppeNr() {
                    return hovedgruppeNr;
                }

                /**
                 * Sets the value of the hovedgruppeNr property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setHovedgruppeNr(String value) {
                    this.hovedgruppeNr = value;
                }

                /**
                 * Gets the value of the gruppeNr property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getGruppeNr() {
                    return gruppeNr;
                }

                /**
                 * Sets the value of the gruppeNr property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setGruppeNr(String value) {
                    this.gruppeNr = value;
                }

                /**
                 * Gets the value of the emneNr property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getEmneNr() {
                    return emneNr;
                }

                /**
                 * Sets the value of the emneNr property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setEmneNr(String value) {
                    this.emneNr = value;
                }

                /**
                 * Gets the value of the handlingsfacetNr property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getHandlingsfacetNr() {
                    return handlingsfacetNr;
                }

                /**
                 * Sets the value of the handlingsfacetNr property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setHandlingsfacetNr(String value) {
                    this.handlingsfacetNr = value;
                }

            }

        }

    }

}
