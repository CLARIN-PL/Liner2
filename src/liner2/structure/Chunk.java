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
	
	public boolean equals(Chunk chunk) {
		if (this.begin != chunk.getBegin())
			return false;
		else if (this.end != chunk.getEnd())
			return false;
//		else if (this.sentence != chunk.getSentence())
//			return false;
		else if (!this.type.equals(chunk.getType()))
			return false;
		return true;
	}
	
	public int getBegin() {
		return this.begin;
	}
	
	public int getEnd() {
		return this.end;
	}
	
	public Sentence getSentence() {
		return this.sentence;
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
