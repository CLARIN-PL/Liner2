package liner2.structure;

import java.util.ArrayList;

/**
 * Reprezentuje token, z których składa się zdanie (Sentence)
 * @author czuk
 *
 */
public class Token {

	/* Uporządkowana lista atrybutów */
	ArrayList<String> attributes = null;
	
	/* Lista analiz morfologicznych, jeżeli dostępna. */
	ArrayList<Tag> tags = null;
	
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
		throw new Error("Not implemented");		
	}
}
