package g419.corpus.structure;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Klasa reprezentuje anotację jako ciągłą sekwencję tokenów w zdaniu.
 * 
 * @author Michał Marcińczuk
 *
 */
public class Annotation {
	/** Annotation type, i.e. name of category.  */
	private String type = null;
	
	/** Name of the group to which belongs the annotation type, ex. word, group, chunk, ne .*/
	private String group = null;

	/** Sentence to which the annotation belongs. */
	private Sentence sentence = null;

	/** Unique identifier of the annotation */
	private String id = null;
	
	/** Indices of tokens which form the annotation. */
	private TreeSet<Integer> tokens = new TreeSet<Integer>();

	/** Index of a token that is the head of the annotation */
	private int head;
	
	/** Lemmatized form of the annotation */
	private String lemma = null;	

	/**
	 * Indeks anotacji w kanale.
	 */
	private int channelIdx;

	/**
	 * Informacja czy anotacja ma oznaczoną głowę.
	 */
	private boolean hasHead = false;
	
	/**
	 * Wartość określająca pewność co do istnienia anotacji. 
	 * Głównie używane przy autoamtycznym rozpoznawaniu anotacji w tekście.
	 */
	private double confidence = 1.0;
	

	private Map<String, String> metadata = new HashMap<String, String>();

	public Annotation(int begin, int end, String type, Sentence sentence){
		for(int i = begin; i <= end; i++){
			this.tokens.add(i);
		}
		this.type = type;
		this.sentence = sentence;
		this.assignHead();
	}

	public Annotation(int tokenIndex, String type, Sentence sentence){
		this.tokens.add(tokenIndex);
		this.type = type;
		this.sentence = sentence;
		this.head = tokenIndex;
	}

	public Annotation(int begin, String type, int channelIdx, Sentence sentence){
		this.tokens.add(begin);
		this.type = type;
		this.sentence = sentence;
		this.channelIdx = channelIdx;
	}

	public Annotation(TreeSet<Integer> tokens, String type, Sentence sentence){
		this.tokens = tokens;
		this.type = type;
		this.sentence = sentence;
		this.assignHead();
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

	public void assignHead(){
		this.assignHead(false);
	}

	/**
	 * Set the value of annotation lemma.
	 * @param lemma
	 */
	public void setLemma(String lemma){
		this.lemma = lemma;
	}
	
	/**
	 * Get the value of annotation lemma.
	 * @return
	 */
	public String getLemma(){
		if ( this.lemma == null ){
			return this.getText();
		}
		else{
			return this.lemma;
		}
	}
	
	/**
	 * Ustaw pewność co do istnienia anotacji.
	 * @param confidence
	 */
	public void setConfidence(double confidence){
		this.confidence = confidence;
	}

	/**
	 * Zwraca wartość określającą pewność istnienia anotacji.
	 * @return
	 */
	public double getConfidence(){
		return this.confidence;
	}
	
	/**
	 * Przypisuje głowę do anotacji na podst. równoległej anotacji, lub jako pierwszy token.
	 * Do użytku z anotacjami "anafora_wyznacznik" na potrzeby piśnika TEI
	 * @return
	 */
	public void assignHead(boolean force){
		if( !force && hasHead() ){
			return;
		}		

		int head = -1;
		for ( int i : this.getTokens() ){
			Token t = this.sentence.getTokens().get(i);
			if ( t.getDisambTag().getPos().equals("subst") ){
				head = i;
				break;
			}
		}
		
		if ( head == -1 ){
			for ( int i : this.getTokens() ){
				Token t = this.sentence.getTokens().get(i);
				if ( t.getDisambTag().getPos().equals("ign") ){
					head = i;
					break;
				}
			}
		}
		
		if ( head == -1 ){
			head = this.tokens.first();
		}
		
		this.setHead(head);
		
		if(this.tokens.size() == 1){ 
			return;
		}

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
	
	/**
	 * Zwraca token będący głową frazy.
	 * @return
	 */
	public Token getHeadToken(){
		return this.sentence.getTokens().get(this.head);
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
		if (chunk == null){
			return false;
		}
		if( this.getSentence().getId() != null
				&& chunk.getSentence().getId() != null
				&& !this.getSentence().getId().equals(chunk.getSentence().getId())){
			return false;
		} else if (!this.tokens.equals(chunk.getTokens())) {
			return false;
		} else if (!this.getText().equals(chunk.getText())) {
			return false;
		} else if (!this.type.equals(chunk.getType())) {
			return false;
		}
		return true;
	}



    @Override
    public int hashCode() {
        return (this.getText() + this.tokens.toString() + this.getType() + this.getSentence().getId()).hashCode();
    }

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getMetadata(String key) {
		return metadata.get(key);
	}

	public void setMetadata(String key, String val) {
		metadata.put(key, val);
	}

	public boolean metaDataMatches(Annotation other){
		return this.metadata.equals(other.metadata);
	}

	public boolean metaDataMatchesKey(String key, Annotation other){
		return this.metadata.getOrDefault(key, "none1").equals(other.metadata.getOrDefault(key, "none2"));
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

	/**
	 * We should rename this function and 'getTokens()'
	 * @return
	 */
	public List<Token> getTokenTokens(){return this.tokens.stream().map(i -> this.sentence.getTokens().get(i)).collect(Collectors.toList());}

	public Sentence getSentence() {
		return this.sentence;
	}

    public void setSentence(Sentence sentence){
        this.sentence = sentence;
    }

	public String getType() {
		return this.type;
	}
	
	public String getGroup() {
		return this.group;
	}

	/**
	 * Zwraca treść chunku, jako konkatenację wartości pierwszych atrybutów.
	 * @return
	 */
	public String getText(){
		return this.getText(false);
	}

	/**
	 * Zwraca treść chunku, jako konkatenację wartości pierwszych atrybutów.
	 * @param markHead Jeżeli true, to głowa anotacji zostanie wypisana w nawiasach klamrowych.
	 * @return
	 */
	public String getText(boolean markHead){
		List<Token> tokens = this.sentence.getTokens();
		if ( tokens == null ){
			return "NO_TOKEN_IN_SENTENCE";
		}
		StringBuilder text = new StringBuilder();
		for (int i : this.tokens) {
			Token token = tokens.get(i);
			if ( markHead && this.head == i ){
				text.append("{");
			}
			text.append(token.getOrth());
			if ( markHead && this.head == i ){
				text.append("}");
			}
			if ((!token.getNoSpaceAfter()) && (i < getEnd())){
				text.append(" ");
			}
		}
		return text.toString();
	}
	
    public String getBaseText(){
        List<Token> tokens = this.sentence.getTokens();
        StringBuilder text = new StringBuilder();
        TokenAttributeIndex index = this.sentence.getAttributeIndex();
        for (int i : this.tokens) {
            Token token = tokens.get(i);
            text.append(token.getAttributeValue(index.getIndex("base")));
            if ((!token.getNoSpaceAfter()) && (i < getEnd())){
                text.append(" ");
            }
        }
        return text.toString();
    }

	// Returns space-separated chain of bases
	public String getSimpleBaseText(){
		List<Token> tokens = this.sentence.getTokens();
		List<String> text = new LinkedList<>();
		TokenAttributeIndex index = this.sentence.getAttributeIndex();
		for (int i : this.tokens) {
			Token token = tokens.get(i);
			text.add(token.getAttributeValue(index.getIndex("base")));
		}
		return StringUtils.join(text, " ");
	}

	public void setId(String id){
		this.id = id;
	}


	public void setType(String type){
		this.type = type.toLowerCase();
	}
	
	public void setCategory(String category){
		this.group = category;
	}

	public static Annotation[] sortChunks(Set<Annotation> chunkSet) {
		int size = chunkSet.size();
		Annotation[] sorted = new Annotation[size];
		int idx = 0;
	    for (Annotation c : chunkSet)
	    	sorted[idx++] = c;
	    for (int i = 0; i < size; i++){
	    	for (int j = i+1; j < size; j++){
	    		if ((sorted[i].getBegin() > sorted[j].getBegin()) ||
	    			((sorted[i].getBegin() == sorted[j].getBegin()) &&
	    			(sorted[i].getEnd() > sorted[j].getEnd()))) {
	    			Annotation aux = sorted[i];
	    			sorted[i] = sorted[j];
	    			sorted[j] = aux;
	    		}
	    	}
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
		cloned.setMetadata(new HashMap<>(this.getMetadata()));
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
