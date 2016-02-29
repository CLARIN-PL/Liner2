package g419.corpus.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


/**
 * Reprezentuje zdanie jako sekwencję tokenów i zbiór anotacji.
 * @author czuk
 *
 */
public class Sentence {
	
	/* Indeks nazw atrybutów */
	TokenAttributeIndex attributeIndex = null;
	
	/* Sekwencja tokenów wchodzących w skład zdania */
	ArrayList<Token> tokens = new ArrayList<Token>();
	
	/* Zbiór anotacji */
	LinkedHashSet<Annotation> chunks = new LinkedHashSet<Annotation>();
	
	/* Identyfikator zdania (unikalny w obrębie paragrafu) */
	String id = null;
	
	/* Tymczasowe obejście braku odniesienia do dokumentu z poziomu klasy Annotation */
	Document document;

    private static Comparator<Annotation> annotationComparator = new Comparator<Annotation>() {
        public int compare(Annotation a, Annotation b) {
            if (a.getTokens().size() == b.getTokens().size()) {
                return String.CASE_INSENSITIVE_ORDER.compare(a.getType(), b.getType());
            }

            return Integer.signum(b.getTokens().size() - a.getTokens().size());
        }
    };

    public Sentence()	{
    	
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
	
	public String getId(){
		return this.id;
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
	
	/*
	 * Zwraca chunk dla podanego indeksu tokenu.
	 * TODO zmienić parametr na token?
	 */
	public ArrayList<Annotation> getChunksAt(int idx, List<Pattern> types) {
        ArrayList<Annotation> returning = new ArrayList<Annotation>();
        Iterator<Annotation> i_chunk = chunks.iterator();
        while (i_chunk.hasNext()) {
            Annotation currentChunk = i_chunk.next();
            if (currentChunk.getTokens().contains(idx)) {
                if(types != null && !types.isEmpty()) {
                    for (Pattern patt : types) {
                        if (patt.matcher(currentChunk.getType()).find()) {
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

    public ArrayList<Annotation> getChunksAt(int idx, List<Pattern> types, boolean sorted){
        ArrayList<Annotation> result = getChunksAt(idx, types);
        if(sorted){
            sortTokenAnnotations(result);
        }
        return result;
    }

    public String getTokenClassLabel(int tokenIdx, List<Pattern> types){
        ArrayList<Annotation> tokenAnnotations = getChunksAt(tokenIdx, types, true);

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

    private void sortTokenAnnotations(ArrayList<Annotation> tokenAnnotations){
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
	
	public ArrayList<Token> getTokens() {
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
	
	public void setId(String id){
		this.id = id;
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

	public void setTokens(ArrayList<Token> newTokens){
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

}
