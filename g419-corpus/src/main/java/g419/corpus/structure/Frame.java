package g419.corpus.structure;

import java.util.HashMap;
import java.util.Map;

/**
 * Struktura reprezentuje zbiór anotacji z przypisanymi rolami.
 * @author czuk
 *
 */
public class Frame {

	String type = null;
	Map<String, Annotation> slots = new HashMap<String, Annotation>();
	Map<String, Map<String, String>> slotsAttributes = new HashMap<String, Map<String, String>>();
	
	public Frame(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}
	
	public Annotation getSlot(String name){
		return this.slots.get(name);
	}
	
	public Map<String, Annotation> getSlots(){
		return this.slots;
	}
	
	public boolean hasSlot(String name){
		return this.slots.containsKey(name);
	}
	
	public void setSlot(String name, Annotation annotation){
		this.slots.put(name, annotation);
	}
	
	public void setSlotAttribute(String name, String attribute, String value){
		Map<String, String> attributes = this.slotsAttributes.get(name);
		if ( attributes == null ){
			attributes = new HashMap<String, String>();
			this.slotsAttributes.put(name, attributes);
		}
		attributes.put(attribute, value);
	}
	
	public Map<String, String> getSlotAttributes(String name){
		return this.slotsAttributes.get(name);
	}
	
	public Map<String, Map<String, String>> getSlotAttributes(){
		return this.slotsAttributes;
	}
	
}
