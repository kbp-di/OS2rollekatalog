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
import javax.xml.bind.annotation.XmlType;


/**
 * FilterKomponenten giver mulighed for at vise en delmaengde af KLE, saakaldte KLE Delplaner frem for hele planen. Valg af filter bør være dynamisk, så det er nemt for brugeren at udvide eller afgrænse visningen.
 * 
 * <p>Java class for FilterKomponent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FilterKomponent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FilterTitel" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterKomponent", propOrder = {
    "filterTitel"
})
public class FilterKomponent {

    @XmlElement(name = "FilterTitel", required = true)
    protected List<Object> filterTitel;

    /**
     * Gets the value of the filterTitel property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the filterTitel property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFilterTitel().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getFilterTitel() {
        if (filterTitel == null) {
            filterTitel = new ArrayList<Object>();
        }
        return this.filterTitel;
    }

}
