package liner2.chunker;

import java.util.HashSet;

import liner2.structure.Chunk;
import liner2.structure.Sentence;

public abstract class Chunker {

	/**
	 * TODO
	 * Przetwarza podane zdanie, rozpoznaje chunki i zwraca w postaci tablicy chunków.
	 * @param sentence
	 * @return
	 */
	public abstract HashSet<Chunk> chunkSentence(Sentence sentence);
	
	/**
	 * Zwolnienie zasobów wykorzystywanych przez chunker, 
	 * np. zamknięcie zewnętrznych procesów i połączeń.
	 * Jeżeli jest to wymagane, to klasa dziedzicząca powinna przeciążyć
	 * tą metodę. 
	 */
	public void close(){
		
	}
}
