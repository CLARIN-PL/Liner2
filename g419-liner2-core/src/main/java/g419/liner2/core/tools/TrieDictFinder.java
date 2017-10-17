package g419.liner2.core.tools;

import java.util.List;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

/**
 * Znajduje sekwencje tokenów wystąpujące w słowniku TrieDict.
 * 
 * @author Michał Marcińczuk
 *
 */
public class TrieDictFinder {

	TrieDictNode dict = null;
	
	/**
	 * 
	 * @param dict słownik sekwencji
	 */
	public TrieDictFinder(TrieDictNode dict){
		this.dict = dict;
	}

	/**
	 * Funkcja sprawdza, czy sekwencja tokenów zaczynająca się od indeksu index znajduje się w słowniku.
	 * 
	 * Sprawdzane są wartości basów.
	 * 
	 * @param index
	 * @return
	 */
	public int find(Sentence sentence, int index){
		TrieDictNode currentNode = dict;
		List<Token> tokens = sentence.getTokens();
		int longestMatch = 0;
		int offset = 0;
		while ( currentNode != null && index+offset < tokens.size() ){
			// Dla uproszczenia sprawdzany jest tylko base dla pierwszego disamba
			String word = tokens.get(index+(offset++)).getDisambTag().getBase();
			TrieDictNode nextNode = currentNode.getChild(word);
			
			if ( nextNode != null && nextNode.isTerminal() ){
				longestMatch = offset;
			}
			currentNode = nextNode;
		}
		return longestMatch;
	}	

	/**
	 * Funkcja sprawdza, czy sekwencja tokenów zaczynająca się od indeksu index znajduje się w słowniku.
	 * 
	 * Sprawdzane są wartości orthów.
	 * 
	 * @param sentence
	 * @param index
	 * @param cs Czy brana jest pod uwagę wielkość liter
	 * @return
	 */
	public int findByOrth(Sentence sentence, int index, boolean cs){
		TrieDictNode currentNode = dict;
		List<Token> tokens = sentence.getTokens();
		int longestMatch = 0;
		int offset = 0;
		while ( currentNode != null && index+offset < tokens.size() ){
			String word = tokens.get(index+(offset++)).getOrth();
			if ( !cs ){
				word = word.toLowerCase();
			}
			TrieDictNode nextNode = currentNode.getChild(word);
			
			if ( nextNode != null && nextNode.isTerminal() ){
				longestMatch = offset;
			}
			currentNode = nextNode;
		}
		return longestMatch;
	}	
}
