package g419.corpus.structure;

import java.util.*;
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
	
	/* Identyfikator zdania */
	String id = null;

    private static Comparator<Annotation> annotationComparator = new Comparator<Annotation>() {
        public int compare(Annotation a, Annotation b) {
            if (a.getTokens().size() == b.getTokens().size()) {
                return String.CASE_INSENSITIVE_ORDER.compare(a.getType(), b.getType());
            }

            return Integer.signum(b.getTokens().size() - a.getTokens().size());
        }
    };

    public Sentence()	{}
	
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

    public String getTokenClassLabel(int tokenIdx, List<Pattern> types){
        ArrayList<Annotation> tokenAnnotations = getChunksAt(tokenIdx, types);
        sortTokenAnnotations(tokenAnnotations);

        if (tokenAnnotations.isEmpty())
            return "O";
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
	
	public LinkedHashSet<Annotation> getChunks() {
		return this.chunks;
	}
	
	public int getAttributeIndexLength() {
		return this.attributeIndex.getLength();
	}
	
	public TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	/*
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
	}

	public void setAnnotations(AnnotationSet chunking) {
		this.chunks = (LinkedHashSet<Annotation>) chunking.chunkSet();
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
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (Token t : this.tokens){
			sb.append(t.getOrth());
			sb.append(t.getNoSpaceAfter() ? "" : " ");
		}
		return sb.toString().trim();
	}

    public void setTokens(ArrayList<Token> newTokens){
        tokens = newTokens;
    }

    public Sentence clone(){
        Sentence copy = new Sentence();
        for(Token t: tokens){
            copy.addToken(t.clone());
        }
        for(Annotation a: chunks){
            copy.addChunk(a.clone());
        }
        return copy;
    }


}
