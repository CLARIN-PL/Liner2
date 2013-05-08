package liner2.structure;

import java.util.ArrayList;

/**
 * Reprezentuje token, z których składa się zdanie (Sentence)
 * @author czuk
 *
 */
public class Token {

	/* Uporządkowana lista atrybutów */
	ArrayList<String> attributes = new ArrayList<String>();
	
	/* Lista analiz morfologicznych, jeżeli dostępna. */
	ArrayList<Tag> tags = new ArrayList<Tag>();
	
	/* Oznaczenie, czy między bieżącym a następnym tokenem był biały znak. */
	boolean noSpaceAfter = false; 

	private String id = null;
	
	public void clearAttributes() {
		this.attributes = new ArrayList<String>();
	}
	
	public void removeAttribute(int attrIdx){
		this.attributes.remove(attrIdx);
	}
	
	/**
	 * TODO
	 * Zwraca wartość atrybutu o podany indeksie.
	 * @param name
	 * @return
	 */
	public String getAttributeValue(int index){
		return attributes.get(index);
	}
	
	public int getNumAttributes() {
		return attributes.size();
	}

	public String getId(){
		return this.id;
	}
	
	/**
	 * TODO
	 * Funkcja pomocnicza zwraca wartość pierwszego atrybutu.
	 * Przeważnie będzie to orth.
	 * @return
	 */
	public String getFirstValue(){
		return attributes.get(0);		
	}
	
	public boolean getNoSpaceAfter() {
		return this.noSpaceAfter;
	}
	
	public void addTag(Tag tag) {
		tags.add(tag);
		if (attributes.size() < 3) {
			this.setAttributeValue(1, tag.getBase());
			this.setAttributeValue(2, tag.getCtag());
		}
	}
	
	public ArrayList<Tag> getTags() {
		return tags;
	}
	
	public void packAtributes(int size){
		while(getNumAttributes()<size)
			attributes.add(null);
	}
	
	public void setAttributeValue(int index, String value) {
		if (index < attributes.size())
			attributes.set(index, value);
		else if (index == attributes.size())
			attributes.add(value);
	}
	
	public void setNoSpaceAfter(boolean noSpaceAfter) {
		this.noSpaceAfter = noSpaceAfter;
	}
	
	public void setId(String id){
		this.id = id;
	}	
}
