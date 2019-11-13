package g419.liner2.core.chunker;

import g419.corpus.schema.kpwr.KpwrNer;
import g419.corpus.structure.*;
import g419.liner2.core.features.annotations.AnnotationAtomicFeature;
import g419.liner2.core.features.annotations.AnnotationFeatureSubstList;
import g419.liner2.core.features.annotations.AnnotationFeatureSubstModifierAfter;
import g419.liner2.core.features.annotations.AnnotationFeatureSubstModifierBefore;
import g419.liner2.core.tools.FrequencyCounter;
import g419.liner2.core.tools.TypedDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Combines two models (name and top8) into a single unified model top8.
 * The annotations recognized by the name model are used to improve the recall.
 * A set of global NE features is used to improve the final precision of classification.
 *
 * @author Michał Marcińczuk
 */
public class AnnotationTop8NameClassifierChunker extends Chunker {

  private Chunker inputChunker = null;
  private final List<AnnotationAtomicFeature> substFeatures = new ArrayList<>();
  private TypedDictionary categoryIndicators = null;
  final private Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * @param inputChunker
   */
  public AnnotationTop8NameClassifierChunker(final Chunker inputChunker, final TypedDictionary categoryIndicators) {
    this.inputChunker = inputChunker;
    this.categoryIndicators = categoryIndicators;
    substFeatures.add(new AnnotationFeatureSubstList());
    substFeatures.add(new AnnotationFeatureSubstModifierAfter());
    substFeatures.add(new AnnotationFeatureSubstModifierBefore());
  }

  @Override
  public Map<Sentence, AnnotationSet> chunk(final Document ps) {
    final Map<Sentence, AnnotationSet> inputChunks = inputChunker.chunk(ps);

    /** Remove redundant NAM annotations */
    for (final AnnotationSet sets : inputChunks.values()) {
      removeRedundantNamAnnotations(sets.chunkSet());
    }

    /** Group annotations by base forms */
    classifyAnnotationsByGroups(inputChunks);

    /** Remove nam annotations */
    for (final AnnotationSet sets : inputChunks.values()) {
      removeNamAnnotations(sets.chunkSet());
    }

    return inputChunks;
  }

  /**
   * Removes nam annotations which are duplications of other annotations.
   *
   * @param ans
   */
  private void removeRedundantNamAnnotations(final Collection<Annotation> ans) {
    final Set<String> fineGrained = new HashSet<>();
    final List<Annotation> toRemove = new ArrayList<>();
    final List<Annotation> toFilter = new ArrayList<>();
    for (final Annotation an : ans) {
      if (an.getType().equals(KpwrNer.NER)) {
        toFilter.add(an);
      } else {
        fineGrained.add(String.format("%d:%d", an.getBegin(), an.getEnd()));
      }
    }
    for (final Annotation an : toFilter) {
      final String key = String.format("%d:%d", an.getBegin(), an.getEnd());
      if (fineGrained.contains(key)) {
        toRemove.add(an);
      }
    }
    ans.removeAll(toRemove);
  }

  /**
   * @param ans
   */
  private void classifyAnnotationsByGroups(final Map<Sentence, AnnotationSet> inputChunks) {
    final Map<String, List<Annotation>> groupedAnnotations = new HashMap<>();
    for (final AnnotationSet sets : inputChunks.values()) {
      for (final Annotation an : sets.chunkSet()) {
        final String groupedText = an.getBaseText().toLowerCase();
        List<Annotation> group = groupedAnnotations.get(groupedText);
        if (group == null) {
          group = new ArrayList<>();
          groupedAnnotations.put(groupedText, group);
        }
        group.add(an);
      }
    }

    for (final String groupText : groupedAnnotations.keySet()) {
      final Set<String> types = new HashSet<>();
      final List<Annotation> anns = groupedAnnotations.get(groupText);

      final FrequencyCounter<String> typeFrequency = new FrequencyCounter<>();

      /* Zlicz typy anotacji przypisane przez model CRF */
      for (final Annotation an : groupedAnnotations.get(groupText)) {
        /* Count type frequency expect NAM */
        if (!an.getType().equals(KpwrNer.NER)) {
          typeFrequency.add(an.getType());
        }
        types.add(an.getType());
      }

      /* Dodaj typy wynikację z przesłanek w kontekście */
      final List<String> indicators = getAnnotationGroupCategoryIndicators(anns);

      /* Dodaj głowę frazy jako przesłankę */
      indicators.add(anns.get(0).getHeadToken().getDisambTag().getBase());

      /* Dodaj każdy subst w nazwie jako przesłankę */
      for (final Token t : anns.get(0).getTokenTokens()) {
        if (t.getDisambTag().getPos().equals("subst")) {
          indicators.add(t.getDisambTag().getBase());
        }
      }

      for (final String indicator : indicators) {
        final Set<String> indicatorTypes = categoryIndicators.getTypes(indicator);
        if (indicatorTypes != null) {
          // Categories for indicators has double weight
          typeFrequency.addAll(indicatorTypes);
          typeFrequency.addAll(indicatorTypes);
        }
      }

      logger.debug(String.format("MIXED TYPES: # %s [%3d]", groupText, groupedAnnotations.get(groupText).size()));
      anns.stream().map(an -> String.format("MIXED TYPES:    %s:%s (confidence=%4.2f)", an.getType(), an.getText(), an.getConfidence())).forEach(logger::debug);
      logger.debug(String.format("MIXED TYPES: %s", String.join(", ", indicators)));
      if (types.size() > 1) {
        logger.debug(String.format("MIXED TYPES: MULTITYPE"));
      }

      /* Ustaw najliczniejszą kategorię */
      final Set<String> mostFrequentTypes = typeFrequency.getMostFrequent();
      if (mostFrequentTypes.size() == 1) {
        final String type = mostFrequentTypes.iterator().next();
        setCategory(anns, type);
      } else if (types.size() > 1 || types.contains(KpwrNer.NER)) {
        final FrequencyCounter<String> indicatorTypeFreq = new FrequencyCounter<>();
        for (final String indicator : indicators) {
          final Set<String> indicatorTypes = categoryIndicators.getTypes(indicator);
          if (indicatorTypes != null) {
            indicatorTypeFreq.addAll(indicatorTypes);
          }
        }
        final Set<String> topIndicatorTypes = indicatorTypeFreq.getMostFrequent();
        if (topIndicatorTypes.size() == 1) {
          setCategory(anns, topIndicatorTypes.iterator().next());
        } else if (topIndicatorTypes.size() == 0 && indicators.size() > 0) {
          logger.debug("MIXED TYPES INDICATOR: " + String.join(", ", indicatorTypeFreq.getMostFrequent()));
        }
      }
    }

  }

  /**
   * Remove NAM annotations
   *
   * @param ans
   */
  private void removeNamAnnotations(final Collection<Annotation> ans) {
    final List<Annotation> toRemove = new ArrayList<>();
    for (final Annotation an : ans) {
      if (an.getType().equals(KpwrNer.NER)) {
        toRemove.add(an);
      }
    }
    ans.removeAll(toRemove);
  }

  /**
   * @param ans
   * @return
   */
  private List<String> getAnnotationGroupCategoryIndicators(final Collection<Annotation> ans) {
    final List<String> indicators = new ArrayList<>();
    for (final AnnotationAtomicFeature aaf : substFeatures) {
      for (final Annotation an : ans) {
        final String subst = aaf.generate(an);
        if (subst != null) {
          indicators.add(subst);
        }
      }
    }
    return indicators;
  }

  /**
   * @param anns
   * @param type
   */
  private void setCategory(final Collection<Annotation> anns, final String type) {
    for (final Annotation an : anns) {
      an.setType(type);
    }
  }
}
