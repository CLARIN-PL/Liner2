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
	
	/**
	 * TODO
	 * Zwraca wartość atrybutu o podany indeksie.
	 * @param name
	 * @return
	 */
	public String getAttributeValue(int index){
		throw new Error("Not implemented");
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
	
	public void addTag(Tag tag) {
		tags.add(tag);
	}
	
	public ArrayList<Tag> getTags() {
		return tags;
	}
	
	public void setAttributeValue(int index, String value) {
		if (index < attributes.size())
			attributes.set(index, value);
		else if (index == attributes.size())
			attributes.add(value);
	}
}
