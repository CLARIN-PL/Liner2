package liner2.structure;

import java.util.ArrayList;

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
	 * Zwraca treść chunku, jako konkatenację wartości pierwszych atrybutów.
	 * @return
	 */
	public String getText(){
		ArrayList<Token> tokens = this.sentence.getTokens();
		StringBuilder text = new StringBuilder();
		for (int i = this.begin; i <= this.end; i++) {
			Token token = tokens.get(i);
			text.append(token.getFirstValue());
			if ((!token.getNoSpaceAfter()) && (i < this.end))
				text.append(" ");
		}
		return text.toString();
	}
	
	public void setEnd(int end) {
		this.end = end;
	}
}
