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
	 * Typ oznakowania.
	 */
	private String type = null;
	
	/**
	 * Zdanie, do którego należy chunk.
	 */
	private Sentence sentence = null;
	
	public Chunk(int begin, int end, String type, Sentence sentence){
		this.begin = begin;
		this.end = end;
		this.type = type;
		this.sentence = sentence;
	}
	
	public int getBegin() {
		return this.begin;
	}
	
	public int getEnd() {
		return this.end;
	}
	
	public String getType() {
		return this.type;
	}
	
	/**
	 * TODO
	 * Zwraca treść chunku, jako konkatenaję wartości pierwszych atryutów.
	 * @return
	 */
	public String getText(){
		throw new Error("Not implemented");
	}
	
	public void setEnd(int end) {
		this.end = end;
	}
}
