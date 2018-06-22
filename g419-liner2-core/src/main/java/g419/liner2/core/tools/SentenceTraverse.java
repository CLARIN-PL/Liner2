package g419.liner2.core.tools;

import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.List;
import java.util.Set;

/**
 * Wraps a sentence and allow an easy navigation over the sentence tokens. The nagivation includes:
 * <ul>
 * 	<li>skipping tokens with specific attributes,</li>
 *  <li>skipping tokens until finding token with specific attribute,</li>
 * </ul>
 * 
 * @author czuk
 *
 */
public class SentenceTraverse {

	private Sentence sentence = null;
	private List<Token> tokens = null;
	private int pointer = 0;
	
	public SentenceTraverse(Sentence sentence){
		this.sentence = sentence;
		this.tokens = sentence.getTokens();
		this.pointer = 0;
	}
	
	/**
	 * Set token pointer to the specific value
	 * @param index
	 */
	public void setPointer(int index){
		this.pointer = index;
	}
	
	/**
	 * Returns current pointer value
	 * @return
	 */
	public int getPointer(){
		return this.pointer;
	}
	
	/**
	 * Moves the pointer backward until finds a token with given base. If the token is found the pointer
	 * is set to the token. If the token is not found then the pointer stay unchanged.
	 * @param base
	 * @return
	 */
	public int backwardUnitFindBase(String base){
		int skipped = 0;
		int backupPointer = this.pointer;
		while ( this.pisb() && !this.matchesByBase(base) ) {
			this.pointer--;		
			skipped++;
		}
		if ( this.pisb() && this.matchesByBase(base) ){
			return skipped;
		} else{
			this.pointer = backupPointer;
			return 0;
		}
	}	
	
	public boolean backwardAny(){
		this.pointer--;
		return true;
	}
	
	/**
	 * Moves the pointer forward and stops on a first token which pos does not match the given value.
	 * @param pos Tokens with given pos will be skipped
	 * @return Number of skipped tokens
	 */
	public int forwardUnitPosMatches(String pos){
		int skipped = 0;
		while ( this.matchesByPos(pos) ) {
			this.pointer++;		
			skipped++;
		}
		return skipped;
	}
	
	/**
	 * Moves the pointer to the next token if the current token's base is in the bases set.
	 * @param bases
	 * @return
	 */
	public boolean consumeByBase(Set<String> bases){
		if ( this.pisb() && bases.contains(this.getCurrentToken().getDisambTag().getBase() )){
			this.pointer++;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Check if the current token pos matches given value. 
	 * 
	 * @param pos
	 * @return
	 */
	public boolean matchesByPos(String pos){
		return this.pisb() && this.getCurrentToken().getDisambTag().getPos().contentEquals(pos);
	}

	public boolean matchesByPos(Set<String> poses){
		return this.pisb() && poses.contains(this.getCurrentToken().getDisambTag().getPos());
	}

	public boolean matchesByBase(String base){
		return this.pisb() && this.getCurrentToken().getDisambTag().getBase().contentEquals(base);
	}

	/**
	 * Check if the current token case matches given value. 
	 * 
	 * @param pos
	 * @return
	 */
	public boolean matchesByCase(Set<String> cases){
		return this.pisb() && cases.contains(this.getCurrentToken().getDisambTag().getCase());
	}

	/**
	 * 
	 * @return
	 */
	public Token getCurrentToken(){
		if ( this.pisb() ){
			return sentence.getTokens().get(this.pointer);
		} else {
			return null;
		}
	}
	
	/**
	 * Checks if the pointer is inside the sentence boundaries.
	 * @return true if the pointer is inside sentence
	 */
	public boolean pisb(){
		return this.pointer >=0 && this.pointer < this.tokens.size();
	}
}
