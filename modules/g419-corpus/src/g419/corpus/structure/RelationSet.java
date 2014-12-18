package g419.corpus.structure;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.TreeSet;


/**
 * Klasa reprezentująca zbiór relacji zawierająca reprezentacje w postaci
 * - zbioru relacji
 * - relacji indeksowanych anotacją źródłową
 * - relacji indeksowanych anotacją docelową
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 */
public class RelationSet {

	LinkedHashSet<Relation> relations = new LinkedHashSet<Relation>();
	// Zbiór relacji indeksowany anotacjami docelowymi
	HashMap<Annotation, TreeSet<Relation>> incomingRelations = new HashMap<Annotation, TreeSet<Relation>>();
	// Zbiór relacji indeksowany anotacjami źródłowymi
	HashMap<Annotation, TreeSet<Relation>> outgoingRelations = new HashMap<Annotation, TreeSet<Relation>>();
	
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
	
	private void addAnnotationRelation(HashMap<Annotation, TreeSet<Relation>> relationMap, Annotation indexAnnotation, Relation relation){
		if(relationMap.containsKey(indexAnnotation))
			relationMap.get(indexAnnotation).add(relation);
		else
			relationMap.put(indexAnnotation, newRelationSet(relation));
	}
	
	private TreeSet<Relation> newRelationSet(Relation firstRelation){
		TreeSet<Relation> set = new TreeSet<Relation>(new RelationAtSameAnnotationComparator());
		set.add(firstRelation);
		return set;
	}
	
	public LinkedHashSet<Relation> getRelations(){
		return relations;
	}
	
	public HashMap<Annotation, TreeSet<Relation>> getIncomingRelations(){
		return incomingRelations;
	}
	
	public TreeSet<Relation> getIncomingRelations(Annotation annotation){
		//return incomingRelations.getOrDefault(annotation, new TreeSet<Relation>());
		return incomingRelations.get(annotation) != null ? incomingRelations.get(annotation) : new TreeSet<Relation>();
	}
	
	public HashMap<Annotation, TreeSet<Relation>> getOutgoingRelations(){
		return outgoingRelations;
	}
	
	public TreeSet<Relation> getOutgoingRelations(Annotation annotation){
		// return outgoingRelations.getOrDefault(annotation, new TreeSet<Relation>());
		return outgoingRelations.get(annotation) != null ? outgoingRelations.get(annotation) : new TreeSet<Relation>();		
	}
}
