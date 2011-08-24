package liner2.structure;

import java.util.ArrayList;

/**
 * Klasa reprezentuje indeks atrybutów będący mapowaniem nazwy atrybutu na unikalny indeks.
 * @author czuk
 *
 */
public class AttributeIndex {

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
		throw new Error("Not implemented");
	}
	
	/**
	 * Zwraca numer porządkowy atrybutu o danej nazwie.
	 * @param name
	 * @return
	 */
	public int getIndex(String name){
		throw new Error("Not implemented");
	}
	
	public String getAttributeValue(Token token, String attributeName){
		return token.getAttributeValue(this.getIndex(attributeName));
	}
	
}
