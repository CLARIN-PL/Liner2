package g419.toolbox.wordnet.struct;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.*;

public class WordnetPl {

  Map<Integer, LexicalUnit> units = Maps.newHashMap();
  Map<Integer, Synset> synsets = Maps.newHashMap();
  Map<String, List<LexicalRelation>> unitRelations = Maps.newHashMap();

  Map<Integer, Map<Synset, Set<Synset>>> synsetRelations = Maps.newHashMap();

  public void addLexicalUnit(final LexicalUnit unit) {
    units.put(unit.getId(), unit);
  }

  public Collection<LexicalUnit> getLexicalUnits() {
    return units.values();
  }

  public void addSynset(final Synset synset) {
    synsets.put(synset.getId(), synset);
  }

  public Synset getSynset(final int id) {
    return synsets.get(id);
  }

  public LexicalUnit getLexicalUnit(final int id) {
    return units.get(id);
  }

  public void addLexicalRelation(final LexicalRelation relation) {
    List<LexicalRelation> relations = unitRelations.get(relation.getRelation());
    if (relations == null) {
      relations = new ArrayList<>();
      unitRelations.put(relation.getRelation(), relations);
    }
    relations.add(relation);
  }

  public List<LexicalRelation> getLexicalRelations(final String relation) {
    return unitRelations.get(relation);
  }

  public void addSynsetRelation(final int relationType, final Synset child, final Synset parent) {
    synsetRelations
        .computeIfAbsent(relationType, k -> Maps.newHashMap())
        .computeIfAbsent(child, k -> Sets.newHashSet())
        .add(parent);
  }

  public int getSynsetRelationCount() {
    return synsetRelations.values().stream().mapToInt(Map::size).sum();
  }

  public int getLexicalUnitRelationCount() {
    return unitRelations.values().stream().mapToInt(List::size).sum();
  }

  public Set<Integer> getSynsetRelationTypes() {
    return synsetRelations.keySet();
  }
}
