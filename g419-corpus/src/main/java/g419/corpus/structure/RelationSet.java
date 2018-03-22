package g419.corpus.structure;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;


/**
 * Klasa reprezentująca zbiór relacji zawierająca reprezentacje w postaci
 * - zbioru relacji
 * - relacji indeksowanych anotacją źródłową
 * - relacji indeksowanych anotacją docelową
 * @author Adam Kaczmarek
 *
 */
public class RelationSet {

	Set<Relation> relations = Sets.newHashSet();
	// Zbiór relacji indeksowany anotacjami docelowymi
	Map<Annotation, Set<Relation>> incomingRelations = Maps.newHashMap();
	// Zbiór relacji indeksowany anotacjami źródłowymi
	Map<Annotation, Set<Relation>> outgoingRelations = Maps.newHashMap();
	
	public RelationSet(){
		
	}
	
	public RelationSet filterBySet(String set){
		if(set == null) return null;
		RelationSet result = new RelationSet();
		for(Relation relation : relations)
			if(set.equals(relation.getSet()))
				result.addRelation(relation);
		return result;
	}
	
	public void addRelation(Relation relation){
		this.relations.add(relation);
		addAnnotationRelation(this.outgoingRelations, relation.getAnnotationFrom(), relation);
		addAnnotationRelation(this.incomingRelations, relation.getAnnotationTo(), relation);
	}

	public void refresh(){
		incomingRelations = Maps.newHashMap();
		outgoingRelations = Maps.newHashMap();
		
		for(Relation relation : this.relations){
			addAnnotationRelation(this.outgoingRelations, relation.getAnnotationFrom(), relation);
			addAnnotationRelation(this.incomingRelations, relation.getAnnotationTo(), relation);
		}
	}
	
	private void addAnnotationRelation(Map<Annotation, Set<Relation>> relationMap, Annotation indexAnnotation, Relation relation){
		if(relationMap.containsKey(indexAnnotation)) {
			relationMap.get(indexAnnotation).add(relation);
		} else {
			relationMap.put(indexAnnotation, newRelationSet(relation));
		}
	}
	
	private Set<Relation> newRelationSet(Relation firstRelation){
		Set<Relation> set = new TreeSet<Relation>(new RelationAtSameAnnotationComparator());
		set.add(firstRelation);
		return set;
	}
	
	public Set<Relation> getRelations(){
		return relations;
	}
	
	public Map<Annotation, Set<Relation>> getIncomingRelations(){
		return incomingRelations;
	}
	
	public Set<Relation> getIncomingRelations(Annotation annotation){
		return incomingRelations.get(annotation) != null ? incomingRelations.get(annotation) : new TreeSet<Relation>();
	}
	
	public Map<Annotation, Set<Relation>> getOutgoingRelations(){
		return outgoingRelations;
	}
	
	public Set<Relation> getOutgoingRelations(Annotation annotation){
		return outgoingRelations.get(annotation) != null ? outgoingRelations.get(annotation) : new TreeSet<Relation>();
	}
}
