package g419.serel.wordnet;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import java.util.*;
import java.util.stream.Collectors;

public class WordnetPl {

  Map<Integer, LexicalUnit> units = Maps.newHashMap();
  Map<Integer, Synset> synsets = Maps.newHashMap();
  Map<String, List<LexicalRelation>> unitRelations = Maps.newHashMap();

  Map<Integer, Map<Synset, Set<Synset>>> synsetRelations = Maps.newHashMap();
  Map<String, Set<Synset>> lemmaToSynset = Maps.newHashMap();

  Map<Synset, Map<Synset, Double>> buffor = Maps.newHashMap();

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
    // System.out.println("Adding LR= " + relation.getRelation());
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

  public void updateLemmaToSynsetIndex() {
    lemmaToSynset.clear();
    synsets.values().forEach(synset ->
        synset.getLexicalUnits()
            .forEach(lu -> lemmaToSynset
                .computeIfAbsent(lu.getName(), n -> Sets.newHashSet())
                .add(synset)));
  }

  public void updateSynsetDepth() {
    synsets.values().forEach(s -> s.setDepth(0));
    final Queue<Synset> queue = Queues.newConcurrentLinkedQueue();
    getRoots().stream()
        .peek(s -> s.setDepth(1))
        .map(this::getDirectHiponyms)
        .forEach(queue::addAll);
    while (!queue.isEmpty()) {
      final Synset synset = queue.poll();
      if (synset.getDepth() == 0) {
        final int depth = getDirectHypernyms(synset).stream()
            .mapToInt(Synset::getDepth)
            .filter(d -> d > 0)
            .min().orElse(0);
        synset.setDepth(depth + 1);
      }
      queue.addAll(getDirectHiponyms(synset).stream()
          .filter(s -> s.getDepth() == 0)
          .collect(Collectors.toSet()));
    }
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

  public Set<Synset> getSynsetsWithLemma(final String lemma) {
    return lemmaToSynset.getOrDefault(lemma, Collections.emptySet());
  }

  public Set<Synset> getAllHypernyms(final Synset synset) {
    final Queue<Synset> queue = Queues.newConcurrentLinkedQueue(getDirectHypernyms(synset));
    final Set<Synset> hypernyms = Sets.newHashSet(queue);
    while (!queue.isEmpty()) {
      final Synset next = queue.poll();
      getDirectHypernyms(next)
          .stream()
          .filter(s -> !hypernyms.contains(s))
          .peek(hypernyms::add)
          .forEach(queue::add);
    }
    return hypernyms;
  }

  public Set<Synset> getDirectHypernyms(final Synset synset) {
    return synsetRelations
        .getOrDefault(11, Collections.emptyMap())
        .getOrDefault(synset, Collections.emptySet());
  }


  public Set<Synset> getAllHiponyms(final Synset synset) {
    final Queue<Synset> queue = Queues.newConcurrentLinkedQueue(getDirectHiponyms(synset));
    final Set<Synset> hiponyms = Sets.newHashSet(queue);
    while (!queue.isEmpty()) {
      final Synset next = queue.poll();
      getDirectHiponyms(next)
          .stream()
          .filter(s -> !hiponyms.contains(s))
          .peek(hiponyms::add)
          .forEach(queue::add);
    }
    return hiponyms;
  }


  public Set<Synset> getDirectHiponyms(final Synset synset) {
    return synsetRelations
        .getOrDefault(10, Collections.emptyMap())
        .getOrDefault(synset, Collections.emptySet());
  }

  public Set<Synset> getCommonHypernyms(final Synset synset1, final Synset synset2) {
    final Set<Synset> synset1Hypernyms = getAllHypernyms(synset1);
    synset1Hypernyms.add(synset1);
    final Set<Synset> synset2Hypernyms = getAllHypernyms(synset2);
    synset2Hypernyms.add(synset2);
    return synset1Hypernyms.stream().filter(synset2Hypernyms::contains).collect(Collectors.toSet());
  }

  public int getShortestDistanceFromSynsetToHypernym(final Synset synset, final Synset hypernym) {
    if (synset == hypernym) {
      return 0;
    }
    int distance = 1;
    Set<Synset> synsets = Sets.newHashSet(synset);
    while (!synsets.isEmpty()) {
      final Set<Synset> hypernyms = synsets.stream()
          .map(this::getDirectHypernyms).flatMap(Collection::stream).collect(Collectors.toSet());
      if (hypernyms.contains(hypernym)) {
        return distance;
      } else {
        distance += 1;
        synsets = hypernyms;
      }
    }
    return -1;
  }

  public OptionalInt getShortestPathDistance(final Synset synset1, final Synset synset2) {
    final Set<Synset> commonHypernyms = getCommonHypernyms(synset1, synset2);
    return commonHypernyms.stream()
        .mapToInt(hypernym -> getShortestDistanceFromSynsetToHypernym(synset1, hypernym)
            + getShortestDistanceFromSynsetToHypernym(synset2, hypernym))
        .min();
  }

  public Set<Synset> getRoots() {
    return synsets.values().stream()
        .filter(s -> getDirectHypernyms(s).size() == 0)
        .collect(Collectors.toSet());
  }

  public Optional<Synset> getLeastCommonSubsummer(final Synset s1, final Synset s2) {
    return getCommonHypernyms(s1, s2).stream()
        .sorted(Comparator.comparing(Synset::getDepth).reversed())
        .findFirst();
  }

  public double simWuPalmer(final Synset s1, final Synset s2) {
    final Optional<Double> simBuffor = getSimFromBuffor(s1, s2);
    if (simBuffor.isPresent()) {
      return simBuffor.get();
    }
    final Optional<Synset> lcs = getLeastCommonSubsummer(s1, s2);
    // INFO: 1 is added to depth to simulare the common root node
    final double lcsDepth = lcs.isPresent() ? lcs.get().getDepth() + 1 : 1;
    final double s1Depth = s1.getDepth() + 1;
    final double s2Depth = s2.getDepth() + 1;
    final double s12len = getShortestPathDistance(s1, s2).orElse((int) (s1Depth + s2Depth));
    final double sim = s1Depth + s2Depth == 0 ? 0 : ((2.0 * lcsDepth) / (s12len + 2.0 * lcsDepth));
    addSimToBuffor(s1, s2, sim);
    return sim;
  }

  synchronized private void addSimToBuffor(final Synset s1, final Synset s2, final double sim) {
    buffor.computeIfAbsent(s1, v -> Maps.newHashMap()).put(s2, sim);
    buffor.computeIfAbsent(s2, v -> Maps.newHashMap()).put(s1, sim);
  }

  private Optional<Double> getSimFromBuffor(final Synset s1, final Synset s2) {
    final Map<Synset, Double> synsetDoubleMap = buffor.get(s1);
    if (synsetDoubleMap == null) {
      return Optional.empty();
    }
    final Double sim = synsetDoubleMap.get(s2);
    if (sim == null) {
      return Optional.empty();
    }
    return Optional.of(sim);
  }


  //------------------------------------------------------------------------
  //                          SEREL additions
  //------------------------------------------------------------------------
  public Set<String> getLemmasFromSynsetId(final int sstId) {
    final Synset sst = synsets.get(sstId);
    return sst.getLexicalUnits().stream().map(lu -> lu.getName()).collect(Collectors.toSet());
  }

  public Set<String> getLemmasFromSynset(final Synset sst) {
    return sst.getLexicalUnits().stream().map(lu -> lu.getName()).collect(Collectors.toSet());
  }

  public Set<String> getLemmasFromSynsets(final Set<Synset> ssts) {
    return ssts.stream().flatMap(sst -> getLemmasFromSynset(sst).stream()).collect(Collectors.toSet());
  }

  public Set<Synset> getSynsetsWithLexicalUnit(final LexicalUnit lexicalUnit) {
    return synsets.values().stream().filter(sst -> sst.lexicalUnits.contains(lexicalUnit)).collect(Collectors.toSet());
  }


  //public LexicalUnit getLexicalUnitByName(final String name) { return units.values().stream().filter(u -> u.getName().equals(name)).findAny().get();}

  // TODO - many results for one lemma ?
  private LexicalUnit getLexicalUnitByName(final String lemma) {
    for (int i = 0; i < this.getLexicalUnits().size(); i++) {
      final LexicalUnit lu = this.getLexicalUnit(i);
      if (lu == null) {
        continue;
      }
      if (lu.getName().equals(lemma)) {
        return lu;
      }
    }
    return null;
  }

  private List<LexicalUnit> getFemineLexicalUnit(final int masculineLexicalUnitId) {

    final String FEMINE_VARIANT_RELATION_ID = "53";
    final List<LexicalRelation> relations = this.unitRelations.get(FEMINE_VARIANT_RELATION_ID);

    final List<LexicalUnit> result = new ArrayList<>();

    for (int i = 0; i < relations.size(); i++) {
      final LexicalRelation lr = relations.get(i);
      if (lr.getChild().getId() == masculineLexicalUnitId) {
        result.add(lr.getParent());
      }
    }

    return result;
  }

  public List<String> getFemineLemmas(final String masculineLemma) {

    final LexicalUnit masculineLexicalUnit = getLexicalUnitByName(masculineLemma);
    if (masculineLexicalUnit != null) {

      return getFemineLexicalUnit(masculineLexicalUnit.getId())
          .stream()
          .map(lu -> lu.getName())
          .collect(Collectors.toList());
    }

    return new ArrayList<>();
  }


  public List<String> getSynonimsForLemma(final String lemma) {
    final Set<Synset> lemmaSynsets = getSynsetsWithLemma(lemma);
    return lemmaSynsets.stream().flatMap(lst -> getLemmasFromSynset(lst).stream()).collect(Collectors.toList());
  }

  public List<String> getAllHiponymsForLexicalUnitName(final String luName) {
    System.out.println("Searching Lu by name ='" + luName + "'");
    final LexicalUnit lu = this.getLexicalUnitByName(luName);
    return getAllHiponymsForLexicalUnitId(lu.getId());
  }

  public List<String> getAllHiponymsForLexicalUnitId(final int luId) {

    final LexicalUnit lu = this.getLexicalUnit(luId);
    System.out.println("root LU =" + lu);

    final Set<Synset> ssts = this.getSynsetsWithLexicalUnit(lu);

    final Set<Synset> allHiponyms = ssts.stream().flatMap(sst -> this.getAllHiponyms(sst).stream()).collect(Collectors.toSet());

    final Set<String> lemmas = this.getLemmasFromSynsets(allHiponyms);

    return new ArrayList<>(lemmas);
  }


}