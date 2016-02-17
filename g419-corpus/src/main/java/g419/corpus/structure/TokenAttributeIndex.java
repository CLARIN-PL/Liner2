package g419.corpus.structure;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Klasa reprezentuje indeks atrybutów będący mapowaniem nazwy atrybutu na unikalny indeks.
 * @author czuk
 *
 */
public class TokenAttributeIndex {

	/** 
	 * Tablica zawiera nazwy atrybutów. Pozycja, na której znajduje się dany atrybut
	 * jest indeksem tego atrybutu w tablicy atrybutów (klasa Token).
	 */
	ArrayList<String> indexes = new ArrayList<String>();
	
	HashMap<String, Integer> nameToIndex = new HashMap<String, Integer>();	

	/**
	 * Domyślny indeks zawiera jedną domyślną cechę "orth".
	 */
	public TokenAttributeIndex(){
		this.addAttribute("orth");
	}
	
	/**
	 * TODO
	 * Dodaje nowy atrybut do indeksu i zwraca jego numer porządkowy (indeks).
	 * @param name -- unikalna nazwa atrybutu
	 * @return
	 */
	public int addAttribute(String name){
		if ( !this.nameToIndex.containsKey(name) ){
			indexes.add(name);
			Integer index = indexes.size()-1;
			this.nameToIndex.put(name, index);
			return index;
		}
		else{
			return this.nameToIndex.get(name);
		}
	}
	
	/**
	 * Porównuje z innym obiektem tej klasy.
	 */
	public boolean equals(TokenAttributeIndex ai) {
		if (this.indexes.size() != ai.getLength())
			return false;
		for (int i = 0; i < this.indexes.size(); i++)
			if (ai.getIndex(this.indexes.get(i)) != i)
				return false;
		return true;
	}
	
	/**
	 * Dodaje listę atrybutów pomijając już zadeklarowane.
	 * @param features
	 */
	public void update(ArrayList<String> features) {
		for (String feature : features){
			addAttribute(feature);
		}
	}
	
	/**
	 * Zwraca numer porządkowy atrybutu o danej nazwie.
	 * @param name
	 * @return
	 */
	public int getIndex(String name){
		return this.nameToIndex.containsKey(name) ? this.nameToIndex.get(name) : -1;
	}
	
	/**
	 * Return name of an attribute for given index.
	 * @param index -- attribute index in the token feature vector
	 * @return name of the attribute
	 */
	public String getName(int index){
		return indexes.get(index);
	}
	
	/**
	 * Return number of declared attributes.
	 * @return number of attributes
	 */
	public int getLength() {
		return indexes.size();
	}

	/**
	 *
	 * @param token
	 * @param attributeName
	 * @return
	 */
	public String getAttributeValue(Token token, String attributeName){
		int idx = this.getIndex(attributeName);
		if (idx != -1)
			return token.getAttributeValue(idx);
		else
			return null;
	}
	
	public ArrayList<String> allAtributes(){
		return indexes;
	}
	
	public TokenAttributeIndex clone(){
		TokenAttributeIndex index = new TokenAttributeIndex();
		for (String name : this.indexes)
			index.addAttribute(name);
		return index;
	}
}
