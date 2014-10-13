package g419.corpus.structure;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;


interface ReheadingStrategy{
	Annotation rehead(SortedSet<Annotation> annotationSet);
}

class ReheadToFirst implements ReheadingStrategy{

	@Override
	public Annotation rehead(SortedSet<Annotation> annotationSet) {
		return annotationSet.first();
	}
	
}

class ReheadToFirstProperName implements ReheadingStrategy{

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

interface ReturningStrategy{
	Set<Relation> returnRelations(Annotation headAnnotation, SortedSet<Annotation> annotationSet, String relationType);
}

/**
 * Strategia zwracania relacji "do głowy"
 * Dla każdej anotacji w klastrze tworzona jest relacja z tej anotacji do głowy klastra
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 */
class ReturnRelationsToHead implements ReturningStrategy{
	
	public Set<Relation> returnRelations(Annotation headAnnotation, SortedSet<Annotation> annotationSet, String relationType){
		Set<Relation> relationsToHead = new HashSet<Relation>();
		for(Annotation ann : annotationSet)
			if(!ann.equals(headAnnotation)) 
				relationsToHead.add(new Relation(ann, headAnnotation, relationType));
		return relationsToHead;
	}
}

class ReturnRelationsToPredecessor implements ReturningStrategy{

	@Override
	public Set<Relation> returnRelations(Annotation headAnnotation, SortedSet<Annotation> annotationSet, String relationType) {
		Set<Relation> relationsToPredecessor = new HashSet<Relation>();
		Annotation predecessor = null;
		for(Annotation ann : annotationSet){
			if(predecessor != null) relationsToPredecessor.add(new Relation(ann, predecessor, relationType));
			predecessor = ann;
		}
			
		return relationsToPredecessor;
	}
	
}


/**
 * Klaster dla opakowania zbioru relacji, które są przechodnie 
 * tj. relacja zachodzi pomiędzy każdą dowolną parą anotacji w zbiorze
 * 
 * Opakowuje anotacje dla pojedynczej encji
 * 
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 */
public class RelationCluster {
	private SortedSet<Annotation> annotations;
	private String type;
	private Annotation headAnnotation;
	private ReheadingStrategy defaultReheadStrategy = new ReheadToFirst();
	private ReturningStrategy defaultReturningStrategy  = new ReturnRelationsToPredecessor();
	
	public RelationCluster(String type){
		this.annotations = new TreeSet<Annotation>(new AnnotationPositionComparator());
		this.type = type;
	}
	
	public void addRelation(Relation relation){
		Annotation source = relation.getAnnotationFrom();
		Annotation target = relation.getAnnotationTo();
		
		this.annotations.add(target);
		this.annotations.add(source);
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
		return this.defaultReturningStrategy.returnRelations(getHead(), this.annotations, this.type);
	}
	
	public Set<Relation> getRelations(ReturningStrategy strategy){
		if(strategy == null) return getRelations();
		return strategy.returnRelations(getHead(), this.annotations, this.type);
	}
	
	public Set<Relation> getRelationsToHead(){
		return getRelations(new ReturnRelationsToHead());
	}
	
	public Set<Relation> getRelationsToPredecessor(){
		return getRelations(new ReturnRelationsToPredecessor());
	}
	
	
	public String toString(){
		String[] annotationTexts = new String[this.annotations.size()];
		annotationTexts[0] = "{" + this.headAnnotation.getText() + "(H)}";
		int i = 0;
		for(Annotation annotation : this.annotations)
			if(!annotation.equals(this.headAnnotation))
				annotationTexts[++i] = "{" + this.headAnnotation.getText() + "}";
			
		return "[" + StringUtils.join(" ", annotationTexts) + "]";
			
	}
}
