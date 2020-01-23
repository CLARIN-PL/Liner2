package g419.spatial.structure;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Token;
import g419.toolbox.sumo.Sumo;
import io.vavr.control.Option;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpatialRelationSchemaMatcher {

  private final List<SpatialRelationSchema> patterns;
  private final Sumo sumo;

  public SpatialRelationSchemaMatcher(final List<SpatialRelationSchema> patterns, final Sumo sumo) {
    this.patterns = patterns;
    this.sumo = sumo;
  }

  public List<SpatialRelationSchema> matchAll(final SpatialExpression relation) {
    return patterns.stream()
        .filter(pattern -> SpatialRelationSchemaMatcher.matches(relation, pattern, sumo))
        .collect(Collectors.toList());
  }

  public static boolean matches(final SpatialExpression relation, final SpatialRelationSchema pattern, final Sumo sumo) {

    if (relation.getSpatialIndicator() == null) {
      return false;
    }

    String preposition = relation.getSpatialIndicator().getText().toLowerCase();
    if (relation.getLandmark().getRegion() != null
        && relation.getLandmark().getRegion().getHeadToken().getDisambTag().getBase().equals("teren")) {
      // Zamiana przyimka z "na" na "w" dla region=teren
      preposition = "w";
    }

    if (!pattern.getIndicators().contains(preposition)) {
      return false;
    }

    final Token checkTokenPos = Option.of(relation.getLandmark().getRegion())
        .map(Annotation::getHeadToken)
        .getOrElse(relation.getLandmark().getSpatialObject().getHeadToken());

    final String[] parts = checkTokenPos.getDisambTag().getCtag().split(":");
    if (parts.length > 2 && !parts[2].equals(pattern.getCase())) {
      return false;
    }

    return isSubconceptOf(pattern.getTrajectorConcepts(), relation.getTrajectorConcepts(), sumo)
        && isSubconceptOf(pattern.getLandmarkConcepts(), relation.getLandmarkConcepts(), sumo);
  }

  private static boolean isSubconceptOf(final Collection<String> patternConcepts,
                                        final Collection<String> elementConcepts,
                                        final Sumo sumo) {
    final Set<String> conceptsWithChildren = collectConceptswithSubclasses(patternConcepts, sumo);
    return elementConcepts.stream()
        .map(String::toLowerCase)
        .filter(conceptsWithChildren::contains)
        .count() > 0;
  }

  private static Set<String> collectConceptswithSubclasses(final Collection<String> concepts, final Sumo sumo) {
    final Set<String> set = concepts.stream()
        .map(String::toLowerCase)
        .collect(Collectors.toSet());
    concepts.stream()
        .map(String::toLowerCase)
        .map(sumo::getSubclasses)
        .forEach(set::addAll);
    return set;
  }
}
