package g419.corpus.structure;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;


/**
 * Reprezentuje zdanie jako sekwencję tokenów i zbiór anotacji.
 * @author czuk
 *
 */
public class Sentence extends IdentifiableElement {
	
	/* Indeks nazw atrybutów */
	TokenAttributeIndex attributeIndex = null;
	
	/* Sekwencja tokenów wchodzących w skład zdania */
	List<Token> tokens = new ArrayList<Token>();
	
	/* Zbiór anotacji */
	LinkedHashSet<Annotation> chunks = new LinkedHashSet<Annotation>();

	/* Tymczasowe obejście braku odniesienia do dokumentu z poziomu klasy Annotation */
	Document document;

	/* Paragraf w którym jest zdanie*/
	Paragraph paragraph;

    private static Comparator<Annotation> annotationComparator = new Comparator<Annotation>() {
        public int compare(Annotation a, Annotation b) {
            if (a.getTokens().size() == b.getTokens().size()) {
                return String.CASE_INSENSITIVE_ORDER.compare(a.getType(), b.getType());
            }

            return Integer.signum(b.getTokens().size() - a.getTokens().size());
        }
    };

    public Sentence() {}

    public Sentence(TokenAttributeIndex attrIndex)	{
    	this.attributeIndex = attrIndex;
    }

	public void addChunk(Annotation chunk) {
		chunks.add(chunk);
	}
	
	public void addAnnotations(AnnotationSet chunking) {
		if ( chunking != null)
			for (Annotation chunk : chunking.chunkSet())
				addChunk(chunk);
	}
	
	public void addToken(Token token) {
		tokens.add(token);
	}
	
	/**
	 * Zwraca pozycję zdania w dokumencie.
	 * @return
	 */
	public int getOrd(){
		if ( this.document != null ){
			return this.document.getSentences().indexOf(this);
		}
		else{
			return -1;
		}
	}
	
	/**
	 * Return true if the sentence has an assigned identifier.
	 * @return True if the sentence identifier is set.
	 */
    public boolean hasId(){ 
    	return id != null; 
    }

	/**
	 * Return a list of annotations which contain a token with given index.
	 */
	public List<Annotation> getChunksAt(int idx) {
        List<Annotation> returning = new ArrayList<Annotation>();
        Iterator<Annotation> i_chunk = chunks.iterator();
        while (i_chunk.hasNext()) {
            Annotation currentChunk = i_chunk.next();
            if (currentChunk.getTokens().contains(idx)) {
            	returning.add(currentChunk);
            }
        }
        return returning;
    }
	
	/**
	 * Return a list of annotations which contain a token with given index.
	 */
	public List<Annotation> getChunksAt(int idx, List<Pattern> types) {
        List<Annotation> returning = new ArrayList<Annotation>();
        Iterator<Annotation> i_chunk = chunks.iterator();
        while (i_chunk.hasNext()) {
            Annotation currentChunk = i_chunk.next();
            if (currentChunk.getTokens().contains(idx)) {
                if(types != null) {
                    for (Pattern patt : types) {
                        if (patt.matcher(currentChunk.getType()).matches()) {
                            returning.add(currentChunk);
                            break;
                        }
                    }
                }
                else{
                    returning.add(currentChunk);
                }
            }
        }
        return returning;
    }

	/*
	Sprawdza, czy token o podanym indeksie jest chunkiem typu 'type'
	 */
	public boolean isChunkAt(int idx, String type) {
		Iterator<Annotation> i_chunk = chunks.iterator();
		while (i_chunk.hasNext()) {
			Annotation currentChunk = i_chunk.next();
			if (currentChunk.getTokens().contains(idx) && currentChunk.getType().equals(type))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param idx
	 * @param types
	 * @param sorted
	 * @return
	 */
    public List<Annotation> getChunksAt(int idx, List<Pattern> types, boolean sorted){
        List<Annotation> result = getChunksAt(idx, types);
        if(sorted){
            sortTokenAnnotations(result);
        }
        return result;
    }

    /**
     * 
     * @param tokenIdx
     * @return
     */
    public String getTokenClassLabel(int tokenIdx){
        List<Annotation> tokenAnnotations = getChunksAt(tokenIdx);

        if (tokenAnnotations.isEmpty()){
            return "O";
        }
        else {
            List<String> classLabels = new ArrayList<String>();
            sortTokenAnnotations(tokenAnnotations);
            for(Annotation ann: tokenAnnotations){
                String classLabel = "";
                if (ann.getBegin() == tokenIdx) {
                    classLabel += "B-";
                }
                else {
                    classLabel += "I-";
                }
                classLabel += ann.getType();
                classLabels.add(classLabel);
            }
            return  StringUtils.join(classLabels, "#");
        }
    }
    
    
    public String getTokenClassLabel(int tokenIdx, List<Pattern> types){
        List<Annotation> tokenAnnotations = getChunksAt(tokenIdx, types, true);

        if (tokenAnnotations.isEmpty()){
            return "O";
        }
        else {
            ArrayList<String> classLabels = new ArrayList<String>();
            sortTokenAnnotations(tokenAnnotations);
            for(Annotation ann: tokenAnnotations){
                String classLabel = "";
                if (ann.getBegin() == tokenIdx) {
                    classLabel += "B-";
                }
                else {
                    classLabel += "I-";
                }
                classLabel += ann.getType();
                classLabels.add(classLabel);

            }
            return  StringUtils.join(classLabels, "#");
        }

    }

    private void sortTokenAnnotations(List<Annotation> tokenAnnotations){
        Collections.sort(tokenAnnotations, annotationComparator);
    }

    /**
     * Return a set of annotations with a type matching the pattern `type`.
     * @param type Pattern of annotation type.
     * @return Set of annotations.
     */
	public LinkedHashSet<Annotation> getAnnotations(Pattern type){
		LinkedHashSet<Annotation> annotationsForTypes = new LinkedHashSet<Annotation>();
		for(Annotation annotation : this.chunks){
			if(type.matcher(annotation.getType()).find()){
				annotationsForTypes.add(annotation);
			}
		}
		
		return annotationsForTypes;
	}

	public LinkedHashSet<Annotation> getAnnotations(String type){
		LinkedHashSet<Annotation> annotationsForTypes = new LinkedHashSet<Annotation>();
		for(Annotation annotation : this.chunks){
			if(type.equals(annotation.getType())){
				annotationsForTypes.add(annotation);
			}
		}
		
		return annotationsForTypes;
	}

	public LinkedHashSet<Annotation> getAnnotations(List<Pattern> types){
		LinkedHashSet<Annotation> annotationsForTypes = new LinkedHashSet<Annotation>();
		for(Annotation annotation : this.chunks){
			for(Pattern type : types)
				if(type.matcher(annotation.getType()).find())
					annotationsForTypes.add(annotation);
		}
		
		return annotationsForTypes;
	}
	
	/**
	 * Return a set of all annotations assigned to the sentence.
	 * @return Set of all annotations.
	 */
	public LinkedHashSet<Annotation> getChunks() {
		return this.chunks;
	}
	
	public int getAttributeIndexLength() {
		return this.attributeIndex.getLength();
	}
	
	public TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	/**
	 * Zwraca ilość tokenów.
	 */
	public int getTokenNumber() {
		return tokens.size();
	}
	
	public List<Token> getTokens() {
		return tokens;
	}
	
	public void setAttributeIndex(TokenAttributeIndex attributeIndex) {
		this.attributeIndex = attributeIndex;
        for(Token t: tokens){
            t.setAttributeIndex(attributeIndex);
        }
	}

	public void setAnnotations(AnnotationSet chunking) {
		this.chunks = chunking.chunkSet();
	}
	
    public String annotationsToString(){
        StringBuilder output = new StringBuilder();
        for(Annotation chunk: chunks)
            output.append(chunk.getType()+" | "+chunk.getText()+"\n");
        return output.toString();
    }

	public void removeAnnotations(String annotation) {
		Set<Annotation> toRemove = new HashSet<Annotation>();
		for (Annotation an : this.chunks)
			if ( an.getType().equals(annotation) )
				toRemove.add(an);
		this.chunks.removeAll(toRemove);		
	}
	
	public Annotation getAnnotationInChannel(String channelName, int annotationIdx){
		for(Annotation annotation : this.chunks)
			if(annotation.getType().equalsIgnoreCase(channelName) && annotation.getChannelIdx() == annotationIdx) return annotation;
		
		return null;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (Token t : this.tokens){
			sb.append(t.getOrth());
			sb.append(t.getNoSpaceAfter() ? "" : " ");
		}
		return sb.toString().trim();
	}

	public String toBaseString(){
		StringBuilder sb = new StringBuilder();
		for (Token t : this.tokens){
			sb.append(t.getAttributeValue("base"));
			sb.append(t.getNoSpaceAfter() ? "" : " ");
		}
		return sb.toString().trim();
	}

	public void setTokens(List<Token> newTokens){
        tokens = newTokens;
    }

    public Sentence clone(){
        Sentence copy = new Sentence();
        copy.attributeIndex = attributeIndex.clone();
        copy.setId(this.getId());
        for(Token t: tokens){
			Token newT = t.clone();
			newT.attrIdx = copy.attributeIndex;
            copy.addToken(newT);
        }
        for(Annotation a: chunks){
            copy.addChunk(a.clone());
        }
        return copy;
    }

	public void setDocument(Document document) {
		this.document = document;
	}
	
	public Document getDocument(){
		return this.document;
	}

	public void setParagraph(Paragraph p){
		this.paragraph = p;
	}

	public Paragraph getParagraph(){
		return this.paragraph;
	}

}
