package g419.corpus.structure;

import java.util.*;


/**
 * Klaster dla opakowania zbioru relacji, które są przechodnie 
 * tj. relacja zachodzi pomiędzy każdą dowolną parą anotacji w zbiorze
 * 
 * Opakowuje anotacje dla pojedynczej encji
 * 
 * @author Adam Kaczmarek
 *
 */
public class AnnotationCluster {
	private Document document;
	private SortedSet<Annotation> annotations;
	private String type;
	private String set;
	private Annotation headAnnotation;
	private ReheadingStrategy defaultReheadStrategy = new ReheadToFirst();
	private ReturningStrategy defaultReturningStrategy  = new ReturnRelationsToPredecessor();
	
	public AnnotationCluster(String type, String set){
		this.annotations = new TreeSet<Annotation>(new AnnotationPositionComparator());
		this.type = type;
		this.set = set;
	}
	
	public String getType(){return this.type;}
	public String getSet(){return this.set;}
	
	public Document getDocument(){
		return document;
	}
	
	public AnnotationCluster getFilteredCluster(List<Annotation> removeMentions){
		AnnotationCluster filteredCluster = new AnnotationCluster(this.type, this.set);
		filteredCluster.setDocument(this.document);
		
		for(Annotation annotation : getAnnotations())
			if(!removeMentions.contains(annotation))
				filteredCluster.addAnnotation(annotation);
		
		return filteredCluster;
	}
	
	public AnnotationCluster getPreceedingCluster(Annotation mention, List<Annotation> selectedAnnotations){
		AnnotationPositionComparator comparator = new AnnotationPositionComparator();
		AnnotationCluster preceedingCluster = new AnnotationCluster(this.type, this.set);
		preceedingCluster.setDocument(this.document);
		
		for(Annotation annotation : getAnnotations()){
			// Sprawdź czy anotacja występuje przed wzmianką -- dodatkowo usuwaj tylko wzmianki tego samego typu
			// Zakładamy bowiem, że wzmianki innych typów pochodzą z wcześniejszych etapów klasyfikacji
			if(selectedAnnotations.contains(annotation)){
				// Jeśli anotacja jest tego samego typu co wzmianka, to sprawdź pozycję
				if(comparator.compare(annotation, mention) < 0){
					preceedingCluster.addAnnotation(annotation);
				}
			}
			else{
				// W p. p. dodaj wzmiankę do klastra
				preceedingCluster.addAnnotation(annotation);
			}
		}
		
		return preceedingCluster;
	}
	
	public void addRelation(Relation relation){
		Annotation source = relation.getAnnotationFrom();
		Annotation target = relation.getAnnotationTo();
		
		this.annotations.add(target);
		this.annotations.add(source);
		
		this.document = relation.getDocument();
	}
	
	public void addAnnotation(Annotation annotation){
		this.annotations.add(annotation);
	}
	
	public SortedSet<Annotation> getAnnotations(){
		return this.annotations;
	}
	
	public Annotation getHead(){
		if(headAnnotation == null) rehead(this.defaultReheadStrategy);
		return headAnnotation;
	}
	
	public void rehead(ReheadingStrategy s){
		this.headAnnotation = s.rehead(this.annotations);
	}
	
	public Set<Relation> getRelations(){
		return this.defaultReturningStrategy.returnRelations(getHead(), this.annotations, this.type, this.set, this.document);
	}
	
	public Set<Relation> getRelations(ReturningStrategy strategy){
		if(strategy == null) return getRelations();
		return strategy.returnRelations(getHead(), this.annotations, this.type, this.set, this.document);
	}
	
	public Set<Relation> getRelationsToHead(){
		return getRelations(new ReturnRelationsToHead());
	}
	
	public Set<Relation> getRelationsToPredecessor(){
		return getRelations(new ReturnRelationsToPredecessor());
	}
	
	
	public String toString(){
		String out = this.headAnnotation != null ? "{" + this.headAnnotation.getText() + "(H)}" : "";
		for(Annotation annotation : this.annotations)
			if(this.headAnnotation == null || !annotation.equals(this.headAnnotation))
				out += "{" + annotation + "}";
			
		return "[" + out + "]";
			
	}
	
	//--------------------------- INNER CLASSES AND INTERFACES ------------------------------------- //
	
	/**
	 * Interfejs strategii przypisywania głowy dla klastra
	 * @author Adam Kaczmarek
	 *
	 */
	public static interface ReheadingStrategy{
		Annotation rehead(SortedSet<Annotation> annotationSet);
	}

	public static class ReheadToFirst implements ReheadingStrategy{

		@Override
		public Annotation rehead(SortedSet<Annotation> annotationSet) {
			return annotationSet.first();
		}
		
	}

	public static class ReheadToFirstProperName implements ReheadingStrategy{

		@Override
		public Annotation rehead(SortedSet<Annotation> annotationSet) {
			
			for(Annotation currentAnnotation : annotationSet){
				if (currentAnnotation.getType().endsWith("nam")){
					return currentAnnotation;
				}
			}
			return null;
		}
		
	}
	
	
	/**
	 * Interfejs strategii zwracania relacji w klastrze
	 * @author Adam Kaczmarek
	 *
	 */
	public static interface ReturningStrategy{
		Set<Relation> returnRelations(Annotation headAnnotation, SortedSet<Annotation> annotationSet, String relationType, String relationSet, Document relDocument);
	}

	/**
	 * Strategia zwracania relacji "do głowy"
	 * Dla każdej anotacji w klastrze tworzona jest relacja z tej anotacji do głowy klastra
	 * @author Adam Kaczmarek
	 *
	 */
	public static class ReturnRelationsToHead implements ReturningStrategy{
		
		public Set<Relation> returnRelations(Annotation headAnnotation, SortedSet<Annotation> annotationSet, String relationType, String relationSet, Document relDocument){
			Set<Relation> relationsToHead = new HashSet<Relation>();
			for(Annotation ann : annotationSet)
				if(!ann.equals(headAnnotation)) 
					relationsToHead.add(new Relation(ann, headAnnotation, relationType, relationSet, relDocument));
			return relationsToHead;
		}
	}

	public static class ReturnRelationsToPredecessor implements ReturningStrategy{

		@Override
		public Set<Relation> returnRelations(Annotation headAnnotation, SortedSet<Annotation> annotationSet, String relationType, String relationSet, Document relDocument) {
			Set<Relation> relationsToPredecessor = new HashSet<Relation>();
			Annotation predecessor = null;
			for(Annotation ann : annotationSet){
				if(predecessor != null) relationsToPredecessor.add(new Relation(ann, predecessor, relationType, relationSet, relDocument));
				predecessor = ann;
			}
				
			return relationsToPredecessor;
		}
		
	}

	public static class ReturnRelationsToDistinctEntities implements ReturningStrategy{
		
		private Map<Annotation, Integer> mentionEntityMapping;
		private Set<Annotation> entities;
		private Set<Annotation> references;
//		private int numEntities;
		
		public ReturnRelationsToDistinctEntities(Set<Annotation> entities, Set<Annotation> references, Map<Annotation, Integer> mapping){
			this.mentionEntityMapping = mapping;
			this.entities = entities;
			this.references = references;
//			this.numEntities = numEntities;
		}
		
		
		@Override
		public Set<Relation> returnRelations(Annotation headAnnotation, SortedSet<Annotation> annotationSet, String relationType, String relationSet, Document relDocument) {
			Set<Relation> relationsToEntities = new HashSet<Relation>();
			Set<Integer> entitiesFound = new HashSet<Integer>();
//			boolean[] entitiesFound = new boolean[this.numEntities];
			Set<Annotation> distinctEntityAnnotations = new HashSet<Annotation>();
			Set<Annotation> referenceAnnotations = new HashSet<Annotation>();
			
			for(Annotation annotation : annotationSet){
				if(this.entities.contains(annotation)){
					if(!entitiesFound.contains(this.mentionEntityMapping.get(annotation))){
						distinctEntityAnnotations.add(annotation);
						entitiesFound.add(this.mentionEntityMapping.get(annotation));
					}
				}
				else if(this.references.contains(annotation)){
					referenceAnnotations.add(annotation);
				}
			}
			
			for(Annotation entityAnnotation: distinctEntityAnnotations){
				for(Annotation referenceAnnotation : referenceAnnotations){
					relationsToEntities.add(new Relation(referenceAnnotation, entityAnnotation ,Relation.COREFERENCE));
				}
			}
			
			return relationsToEntities;
		}
	}

	public void setDocument(Document document2) {
		this.document = document2;
	}

	public void removeAnnotations(List<Annotation> toRemove) {
		annotations.removeAll(toRemove);
	}
}
