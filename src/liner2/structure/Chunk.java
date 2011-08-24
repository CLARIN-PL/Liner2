package liner2.structure;

/**
 * Klasa reprezentuje anotację jako ciągłą sekwencję tokenów w zdaniu.
 * @author czuk
 *
 */
public class Chunk {

	/**
	 * Indeks pierwszego tokenu.
	 */
	private int begin = -1;
	
	/**
	 * Indeks ostatniego tokenu.
	 */
	private int end = -1;
	
	/**
	 * Zdanie, do którego należy chunk.
	 */
	private Sentence sentence = null;
	
	public Chunk(int begin, int end, Sentence sentence){
		this.begin = begin;
		this.end = end;
		this.sentence = sentence;
	}
	
	/**
	 * TODO
	 * Zwraca treść chunku, jako konkatenaję wartości pierwszych atryutów.
	 * @return
	 */
	public String getText(){
		throw new Error("Not implemented");
	}
}
