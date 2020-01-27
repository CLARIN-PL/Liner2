package g419.corpus.structure;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Klasa reprezentująca zbiór relacji zawierająca reprezentacje w postaci
 * - zbioru relacji
 * - relacji indeksowanych anotacją źródłową
 * - relacji indeksowanych anotacją docelową
 *
 * @author Adam Kaczmarek
 */
public class RelationSet implements Iterable<Relation> {

  Set<Relation> relations = Sets.newHashSet();
  // Zbiór relacji indeksowany anotacjami docelowymi
  Map<Annotation, Set<Relation>> incomingRelations = Maps.newHashMap();
  // Zbiór relacji indeksowany anotacjami źródłowymi
  Map<Annotation, Set<Relation>> outgoingRelations = Maps.newHashMap();

  public RelationSet() {

  }

  public RelationSet filterBySet(final String set) {
    if (set == null) {
      return null;
    }
    final RelationSet result = new RelationSet();
    for (final Relation relation : relations) {
      if (set.equals(relation.getSet())) {
        result.addRelation(relation);
      }
    }
    return result;
  }

  public void addRelation(final Relation relation) {
    relations.add(relation);
    addAnnotationRelation(outgoingRelations, relation.getAnnotationFrom(), relation);
    addAnnotationRelation(incomingRelations, relation.getAnnotationTo(), relation);
  }

  public void refresh() {
    incomingRelations = Maps.newHashMap();
    outgoingRelations = Maps.newHashMap();

    for (final Relation relation : relations) {
      addAnnotationRelation(outgoingRelations, relation.getAnnotationFrom(), relation);
      addAnnotationRelation(incomingRelations, relation.getAnnotationTo(), relation);
    }
  }

  private void addAnnotationRelation(final Map<Annotation, Set<Relation>> relationMap, final Annotation indexAnnotation, final Relation relation) {
    if (relationMap.containsKey(indexAnnotation)) {
      relationMap.get(indexAnnotation).add(relation);
    } else {
      relationMap.put(indexAnnotation, newRelationSet(relation));
    }
  }

  private Set<Relation> newRelationSet(final Relation firstRelation) {
    final Set<Relation> set = new TreeSet<>(new RelationAtSameAnnotationComparator());
    set.add(firstRelation);
    return set;
  }

  public Set<Relation> getRelations() {
    return relations;
  }

  public Map<Annotation, Set<Relation>> getIncomingRelations() {
    return incomingRelations;
  }

  public Set<Relation> getIncomingRelations(final Annotation annotation) {
    return incomingRelations.get(annotation) != null ? incomingRelations.get(annotation) : new TreeSet<>();
  }

  public Map<Annotation, Set<Relation>> getOutgoingRelations() {
    return outgoingRelations;
  }

  public Set<Relation> getOutgoingRelations(final Annotation annotation) {
    return outgoingRelations.get(annotation) != null ? outgoingRelations.get(annotation) : new TreeSet<>();
  }

  @Override
  public Iterator<Relation> iterator() {
    return relations.iterator();
  }
}
