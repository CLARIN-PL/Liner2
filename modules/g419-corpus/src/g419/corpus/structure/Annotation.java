package g419.corpus.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Klasa reprezentuje anotację jako ciągłą sekwencję tokenów w zdaniu.
 * @author czuk
 *
 */
public class Annotation {
	/**
	 * Typ oznakowania.
	 */
	private String type = null;
	
	/**
	 * Zdanie, do którego należy chunk.
	 */
	private Sentence sentence = null;
	
	private String id = null;
	/**
	 * Indeksy tokenów.
	 */
	private TreeSet<Integer> tokens = new TreeSet<Integer>();
	
	/**
	 * Indeks głowy anotacji.
	 */
	private int head;
	
	/**
	 * Indeks anotacji w kanale.
	 */
	private int channelIdx;
	
	/**
	 * Informacja czy anotacja ma oznaczoną głowę.
	 */
	private boolean hasHead = false;
	
	
	public Annotation(int begin, int end, String type, Sentence sentence){
		for(int i = begin; i <= end; i++)
			this.tokens.add(i);
		this.type = type.toLowerCase();
		this.sentence = sentence;
	}
	
	public Annotation(int begin, String type, Sentence sentence){
		this.tokens.add(begin);
		this.type = type.toLowerCase();
		this.sentence = sentence;
	}
	
	public Annotation(int begin, String type, int channelIdx, Sentence sentence){
		this.tokens.add(begin);
		this.type = type.toLowerCase();
		this.sentence = sentence;
		this.channelIdx = channelIdx;
	}
	
	public void setChannelIdx(int idx){
		this.channelIdx = idx;
	}
	
	public int getChannelIdx(){
		return this.channelIdx;
	}
	
	public boolean hasHead(){
		return this.hasHead;
	}
	
	/**
	 * Przypisuje głowę do anotacji na podst. równoległej anotacji, lub jako pierwszy token.
	 * Do użytku z anotacjami "anafora_wyznacznik" na potrzeby piśnika TEI
	 * @return
	 */
	public void assignHead(){
		if(hasHead()) return;
		
		this.setHead(this.tokens.first());
		if(this.tokens.size() == 1) return;
	
		for(Annotation ann: this.sentence.getChunks()){
			if(ann.hasHead() && this.tokens.equals(ann.tokens) && !this.type.equalsIgnoreCase(ann.type)){
				this.setHead(ann.getHead());
				return;
			}
		}
	}
	
	public int getHead(){
		return this.head;
	}
	
	public void setHead(int idx){
		this.hasHead = true;
		this.head = idx;
	}
	
	public void addToken(int idx){
		if ( !this.tokens.contains(idx) )
			this.tokens.add(idx);
	}
	
	public void replaceTokens(int begin, int end){
		this.tokens.clear();
		for(int i = begin; i <= end; i++)
			this.tokens.add(i);
	}

	@Override
	public boolean equals(Object object) {
		Annotation chunk = (Annotation) object;
		if (!this.tokens.equals(chunk.getTokens()))
			return false;
		else if (!this.getText().equals(chunk.getText()))
			return false;
		else if (!this.type.equals(chunk.getType()))
			return false;
		return true;
	}

    @Override
    public int hashCode() {
        return (this.getText() + this.tokens.toString() + this.getType() + this.getSentence().getId()).hashCode();
    }
    
	public String getId(){
		return this.id;
	}
	
	public int getBegin() {
		return this.tokens.first();
	}
	
	public int getEnd() {
		return this.tokens.last();
	}
	
	public TreeSet<Integer> getTokens(){
		return this.tokens;
	}
	
	public Sentence getSentence() {
		return this.sentence;
	}

    public void setSentence(Sentence sentence){
        this.sentence = sentence;
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
		for (int i : this.tokens) {
			Token token = tokens.get(i);
			text.append(token.getOrth());
			if ((!token.getNoSpaceAfter()) && (i < getEnd()))
				text.append(" ");
		}
		return text.toString();
	}

    public String getBaseText(){
        ArrayList<Token> tokens = this.sentence.getTokens();
        StringBuilder text = new StringBuilder();
        TokenAttributeIndex index = this.sentence.getAttributeIndex();
        for (int i : this.tokens) {
            Token token = tokens.get(i);
            text.append(token.getAttributeValue(index.getIndex("base")));
            if ((!token.getNoSpaceAfter()) && (i < getEnd()))
                text.append(" ");
        }
        return text.toString();
    }
	
	public void setId(String id){
		this.id = id;
	}
	

	public void setType(String type){
		this.type = type.toLowerCase();
	}
	
	public static Annotation[] sortChunks(HashSet<Annotation> chunkSet) {
		int size = chunkSet.size();
		Annotation[] sorted = new Annotation[size];
		int idx = 0;
	    for (Annotation c : chunkSet)
	    	sorted[idx++] = c;
	    for (int i = 0; i < size; i++)
	    	for (int j = i+1; j < size; j++)
	    		if ((sorted[i].getBegin() > sorted[j].getBegin()) ||
	    			((sorted[i].getBegin() == sorted[j].getBegin()) &&
	    			(sorted[i].getEnd() > sorted[j].getEnd()))) {
	    			Annotation aux = sorted[i];
	    			sorted[i] = sorted[j];
	    			sorted[j] = aux;
	    		}
		return sorted;
	}
	
	public String toString(){
		return "[" + getText() + "]_"+getSentence().getId()+"|"+getType();
	}

    public Annotation clone(){
        Annotation cloned = new Annotation(getBegin(), getEnd(), getType(), this.sentence);
        cloned.setId(this.id);
        cloned.setHead(this.head);
        return cloned;
    }
    
    private boolean equalsIndices(ArrayList<Integer> tab1, ArrayList<Integer> tab2){
    	if ( tab1.size() != tab2.size() )
    		return false;
    	for ( int i=0; i<tab1.size(); i++)
    		if ( tab1.get(i) != tab2.get(i) )
    			return false;
    	return true;
    }

}
