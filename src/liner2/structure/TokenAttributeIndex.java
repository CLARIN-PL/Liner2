package liner2.structure;

import java.util.ArrayList;

/**
 * Klasa reprezentuje indeks atrybutów będący mapowaniem nazwy atrybutu na unikalny indeks.
 * @author czuk
 *
 */
public class TokenAttributeIndex {

	/* Tablica zawiera nazwy atrybutów. Pozycja, na której znajduje się dany atrybut
	 * jest indeksem tego atrybutu w tablicy atrybutów (klasa Token).
	 */
	ArrayList<String> indexes = new ArrayList<String>();
	
	/**
	 * TODO
	 * Dodaje nowy atrybut do indeksu i zwraca jego numer porządkowy (indeks).
	 * @param name -- unikalna nazwa atrybutu
	 * @return
	 */
	public int addAttribute(String name){
		indexes.add(name);
		return indexes.size()-1;
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
	
	public void update(ArrayList<String> features) {
		indexes = new ArrayList<String>();
		for (String feature : features)
			addAttribute(feature);
	}
	
	/**
	 * Zwraca numer porządkowy atrybutu o danej nazwie.
	 * @param name
	 * @return
	 */
	public int getIndex(String name){
		return indexes.indexOf(name);
	}
	
	public String getName(int index){
		return indexes.get(index);
	}
	
	public int getLength() {
		return indexes.size();
	}
	
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
	
}
