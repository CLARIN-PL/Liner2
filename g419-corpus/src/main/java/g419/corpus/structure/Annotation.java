package g419.corpus.structure;

import java.util.*;
import java.util.stream.Collectors;

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
	 * Kategoria anotacji, np. word, group, chunk, ne
	 */
	private String category = null;

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

	private Map<String, String> metadata = new HashMap<String, String>();

	public Annotation(){

	}

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
	 * Przypisuje głowę do anotacji na podst. równoległej anotacji, lub jako pierwszy token.
	 * Do użytku z anotacjami "anafora_wyznacznik" na potrzeby piśnika TEI
	 * @return
	 */
	public void assignHead(boolean force){
		// TODO: dlaczego tworzona jest anotacja dla pustego zdania
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
		if(chunk == null) return false;
		if( this.getSentence().getId() != null
				&& chunk.getSentence().getId() != null
				&& !this.getSentence().getId().equals(chunk.getSentence().getId()))
			return false;
		else if (!this.tokens.equals(chunk.getTokens()))
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
	
	public String getCategory() {
		return this.category;
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
		ArrayList<Token> tokens = this.sentence.getTokens();
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
	
	public void setCategory(String category){
		this.category = category;
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

//    public List<Annotation> getExactOverlappingAnnotations(){
//    	return getExactOverlappingAnnotations(Arrays.asList(new Pattern[]{Pattern.compile("*")}));
//    }
//    
//    public List<Annotation> getExactOverlappingAnnotations(List<Pattern> patterns){
//    	List<Annotation> overlapping = new ArrayList<Annotation>();
//    	
//    	for(Annotation potentialOverlap : getSentence().getAnnotations(patterns)){
//    		// NIe istnieje dokładne pokrycie taką samą anotacją
//    		if(potentialOverlap.getChannelIdx() != getChannelIdx()){
//    			if(getTokens().equals(potentialOverlap.getTokens())){
//    				overlapping.add(potentialOverlap);
//    			}
//    		}
//    	}
//    	
//    	return overlapping;
//    }
//    
//    public boolean isOverlappedByAll(List<Pattern> overlapPatterns){
//    	// TODO: incomplete implementation
//		boolean overlapByAll = false;
//		List<Annotation> overlapping = getExactOverlappingAnnotations(overlapPatterns);
//		
//		return overlapByAll;
//	}
//	
//	public boolean isOverlappedByAny(List<Pattern> overlapPatterns){
//		List<Annotation> overlapping = getExactOverlappingAnnotations(overlapPatterns);
//		return overlapping.size() > 0;
//	}
}
