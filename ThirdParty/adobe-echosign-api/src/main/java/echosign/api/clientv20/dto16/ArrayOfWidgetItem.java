
package echosign.api.clientv20.dto16;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfWidgetItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfWidgetItem">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="WidgetItem" type="{http://dto16.api.echosign}WidgetItem" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfWidgetItem", propOrder = {
    "widgetItem"
})
public class ArrayOfWidgetItem {

    @XmlElement(name = "WidgetItem", nillable = true)
    protected List<WidgetItem> widgetItem;

    /**
     * Gets the value of the widgetItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the widgetItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWidgetItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WidgetItem }
     * 
     * 
     */
    public List<WidgetItem> getWidgetItem() {
        if (widgetItem == null) {
            widgetItem = new ArrayList<WidgetItem>();
        }
        return this.widgetItem;
    }

}
