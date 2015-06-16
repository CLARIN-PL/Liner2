package g419.corpus.structure;

import java.util.*;
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
	
	public void removeAnnotations(List<Annotation> annotations){
		for(Annotation annotation : annotations)
			annotation.getSentence().getChunks().remove(annotation);
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
    
    public void filterAnnotationClusters(List<Annotation> annotations){
    	AnnotationClusterSet clusterSet = AnnotationClusterSet.fromRelationSet(relations);
    	clusterSet.removeAnnotations(annotations);
    	this.relations = clusterSet.getRelationSet(new AnnotationCluster.ReturnRelationsToHead());
    }

    /**
     * Przepięcie relacji z anotacji źródłowej do anotacji docelowej
     * @param source
     * @param dest
     */
	public void rewireSingleRelations(Annotation source, Annotation dest) {
			List<Relation> rewired = new ArrayList<Relation>();
		
			if(this.relations.incomingRelations.containsKey(source)){
				for(Relation incoming : this.relations.incomingRelations.get(source)){
					if(incoming.getAnnotationTo().equals(dest)) continue;
					Relation rwRel = new Relation(incoming.getAnnotationFrom(), dest, incoming.getType(), incoming.getSet(), this);
 					rewired.add(rwRel);
					this.relations.relations.remove(incoming);
				}
				this.relations.refresh();
//				this.relations.incomingRelations.remove(source);
			}
				
			if(this.relations.outgoingRelations.containsKey(source)){
				for(Relation outgoing : this.relations.outgoingRelations.get(source)){
					if(outgoing.getAnnotationFrom().equals(dest)) continue;
					Relation rwRel = new Relation(dest, outgoing.getAnnotationTo(), outgoing.getType(), outgoing.getSet(), this);
					rewired.add(rwRel);
					this.relations.relations.remove(outgoing);
				}
//				this.relations.outgoingRelations.remove(source);
			}
			
			for(Relation relation : rewired) this.relations.addRelation(relation);
			this.relations.refresh();
	}
	
	public void rewireRelations(List<Annotation> relAnnotations, List<Annotation> targetAnnotations, boolean removeNonRelational){
 		
		
		List<Annotation> toRemove = new ArrayList<Annotation>();
 		
 		for(Annotation rAnn : relAnnotations){
 			boolean found = false;
 			for(Annotation potentialTarget : rAnn.getSentence().getChunks()){
 				if(potentialTarget.getTokens().equals(rAnn.getTokens()) && targetAnnotations.contains(potentialTarget)){
 					found  = true;
 					rewireSingleRelations(rAnn, potentialTarget);
 					if(removeNonRelational) toRemove.add(rAnn);
 				}
 			}
// 			if(!found) {
//				rAnn.setType("anafora_verb_null");
//			}
 		}
 		
 		if(removeNonRelational) removeAnnotations(toRemove);
 	}
	
	
	/**
	 * Scalanie relacji dla anotacji typu *person_nam* z wewnętrznymi anotacjami 
	 * typu *person_nam_first*, *person_nam_last* etc. Dodatkowo opcja umożliwiająca
	 * usunięcie wewnętrznych anotacji.
	 * @param removeInnerAnnotations opcja usuwania wewnętrznych anotacji
	 */
	public void refinePersonNamRelations(boolean removeInnerAnnotations){
		List<Pattern> personNam = Arrays.asList(new Pattern[]{Pattern.compile("nam_liv_person$"), Pattern.compile("person_nam")});
		List<Pattern> personLastFirstNam = Arrays.asList(new Pattern[]{
				Pattern.compile("nam_liv_person_add"), Pattern.compile("nam_liv_person_first"), Pattern.compile("nam_liv_person_last"),
				Pattern.compile("person_add_nam"), Pattern.compile("person_first_nam"), Pattern.compile("person_last_nam")
		});
		
		for(Sentence sentence : getSentences()){
			Set<Annotation> lastFirstNam = sentence.getAnnotations(personLastFirstNam); 
			Set<Annotation> persNam = sentence.getAnnotations(personNam);
			
			List<Annotation> toRemove = new ArrayList<>();
			
			// Dla każdej anotacji bardziej granularnej od nam_liv_person (imię, nazwisko etc.)
			for(Annotation lfAnn : lastFirstNam){
				// Dla każdej anotacji nam_liv_person - nazwa całej osoby / cała nazwa osoby
				for(Annotation pAnn : persNam){
//					if(pAnn.getType().equalsIgnoreCase(lfAnn.getText()))
						
					// Dla każdego tokenu w "mniejszej" anotacji
					for(Integer tokenId : lfAnn.getTokens()){
						// Sprawdź czy zawiera się w "większej"
						if(pAnn.getTokens().contains(tokenId)){
							// Przepnij relacje z mniejszej do większej
							rewireSingleRelations(lfAnn, pAnn);
							toRemove.add(lfAnn);
							// Przestań sprawdzać kolejne tokeny - anotacje się krzyżują
							break;
						}
					}
				}
			}
			removeAnnotations(toRemove);
		}
	}
    
}
