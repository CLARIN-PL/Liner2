package liner2.structure;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * Reprezentuje zdanie jako sekwencję tokenów i zbiór anotacji.
 * @author czuk
 *
 */
public class Sentence {
	
	/* Indeks nazw atrybutów */
	AttributeIndex attributesIndex = null;
	
	/* Sekwencja tokenów wchodzących w skład zdania */
	ArrayList<Token> tokens = new ArrayList<Token>();
	
	/* Zbiór anotacji */
	HashSet<Chunk> chunks = new HashSet<Chunk>();
	
	public Sentence()	{}
	
	public void addToken(Token token) {
		tokens.add(token);
	}
	
	public ArrayList<Token> getTokens() {
		return tokens;
	}
}
