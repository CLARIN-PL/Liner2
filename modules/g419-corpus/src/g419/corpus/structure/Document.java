package g419.corpus.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 
 * @author Michał Marcińczuk
 *
 */
public class Document{

	String name = null;
	TokenAttributeIndex attributeIndex = null;
	ArrayList<Paragraph> paragraphs = new ArrayList<Paragraph>();
	
	/* Zbiór relacji */
	RelationSet relations = new RelationSet();
	
	public Document(String name, TokenAttributeIndex attributeIndex){
		this.name = name;
		this.attributeIndex = attributeIndex;
	}
	
	public Document(String name, ArrayList<Paragraph> paragraphs, TokenAttributeIndex attributeIndex){
		this.name = name;
		this.paragraphs = paragraphs;
		this.attributeIndex = attributeIndex;
	}
	
	public Document(String name, ArrayList<Paragraph> paragraphs, TokenAttributeIndex attributeIndex, RelationSet relations){
		this.name = name;
		this.paragraphs = paragraphs;
		this.attributeIndex = attributeIndex;
		this.relations = relations;
	}
	
	/**
	 * Get the name of document source. If the document was read from a file, 
	 * it is a path to the file. 
	 * @return source of the document
	 */
	public String getName(){
		return this.name;
	}
	
	public RelationSet getRelations(){
		return this.relations;
	}
	
	public RelationSet getRelations(String set){
		return this.relations.filterBySet(set);
	}
	
	public void setRelations(RelationSet relations){
		this.relations = relations;
	}
	
	public void addParagraph(Paragraph paragraph) {
		paragraphs.add(paragraph);
		if (paragraph.getAttributeIndex() == null)
			paragraph.setAttributeIndex(this.attributeIndex);
	}
	
	public TokenAttributeIndex getAttributeIndex() {
		return this.attributeIndex;
	}
	
	public ArrayList<Paragraph> getParagraphs() {
		return this.paragraphs;
	}
	
	public void setAttributeIndex(TokenAttributeIndex attributeIndex) {
		this.attributeIndex = attributeIndex;
		for (Paragraph p : this.paragraphs)
			p.setAttributeIndex(this.attributeIndex);
	}

	/**
	 * Creates a copy of collections of annotations. A new collection is created. The annotation
	 * and sentence are not copied. 
	 * @return
	 */
	public HashMap<Sentence, AnnotationSet> getChunkings() {
		HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences()){
				LinkedHashSet<Annotation> annotations = new LinkedHashSet<Annotation>();
				annotations.addAll(sentence.getChunks());
				chunkings.put(sentence, new AnnotationSet(sentence, annotations));
			}
		return chunkings;
	}

	/**
	 * Add annotations to sentences.
	 * @param chunkings
	 */
	public void addAnnotations(HashMap<Sentence, AnnotationSet> chunkings) {
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				sentence.addAnnotations(chunkings.get(sentence));
	}

	/**
	 * Discard existing annotations and set given set.
	 * @param chunkings
	 */
	public void setAnnotations(HashMap<Sentence, AnnotationSet> chunkings) {
		for ( Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				sentence.setAnnotations(chunkings.get(sentence));
	}

	public ArrayList<Sentence> getSentences() {
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		for ( Paragraph paragraph : this.paragraphs )
			sentences.addAll(paragraph.getSentences());
		return sentences;
	}

	/**
	 * Removes all anotation with given name.
	 * @param annotation
	 */
	public void removeAnnotations(String annotation) {
		for (Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				sentence.removeAnnotations(annotation);
		
	}

    /**
     * Removes all anotation
     */
    public void removeAnnotations() {
        for (Paragraph paragraph : this.paragraphs)
            for (Sentence sentence : paragraph.getSentences())
                sentence.chunks = new LinkedHashSet<Annotation>();

    }

    /**
     * Retreives Annotation given sentence id, channel and annotation index in channel
     */
    public Annotation getAnnotation(String sentenceId, String channelName, int annotationIdx){
    	for (Paragraph paragraph : this.paragraphs)
			for (Sentence sentence : paragraph.getSentences())
				if(sentence.getId().equals(sentenceId))
					return sentence.getAnnotationInChannel(channelName, annotationIdx);
    	return null;
    }
    
    public ArrayList<Annotation> getAnnotations(List<Pattern> types){
    	ArrayList<Annotation> annotations = new ArrayList<Annotation>();
    	for(Sentence sentence: getSentences()){
    		for(Annotation sentenceAnnotation: sentence.getAnnotations(types)){
    			annotations.add(sentenceAnnotation);
    		}
    	}
    	
    	return annotations;
    }
    
    public ArrayList<Annotation> getAnnotations(){
    	ArrayList<Annotation> annotations = new ArrayList<Annotation>();
    	
    	for(Sentence sentence: getSentences()){
    		for(Annotation sentenceAnnotation: sentence.getChunks()){
    			annotations.add(sentenceAnnotation);
    		}
    	}
    	
    	return annotations;
    }

    public Document clone(){
        Document copy = new Document(name, attributeIndex.clone());
        for(Paragraph p: paragraphs){
            copy.addParagraph(p.clone());
        }
        return copy;

    }
    
    public void addRelation(Relation relation){
    	this.relations.addRelation(relation);
    }
}
