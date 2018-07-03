package g419.spatial.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import g419.corpus.HasLogger;
import g419.corpus.schema.annotation.NkjpSpejd;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.*;
import g419.liner2.core.features.tokens.ClassFeature;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import g419.spatial.filter.*;
import g419.spatial.pattern.*;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialObjectRegion;
import g419.toolbox.wordnet.NamToWordnet;
import g419.toolbox.wordnet.Wordnet3;
import io.vavr.control.Option;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.maltparser.core.exception.MaltChainedException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpatialRelationRecognizer2 implements HasLogger {

    final private MaltParser malt;
    final List<IRelationFilter> filters;
    final RelationFilterSemanticPattern semanticFilter = new RelationFilterSemanticPattern();

    final private Pattern patternAnnotationNam = Pattern.compile("^nam(_(fac|liv|loc|pro|oth).*|$)");
    final private Set<String> orthConj = Sets.newHashSet("i", "oraz", ",");

    final private Set<String> objectPos = Sets.newHashSet("subst", "ign", "brev");
    final private Set<String> regions = SpatialResources.getRegions();

    /**
     * @param malt    Ścieżka do modelu Maltparsera
     * @param wordnet Ścieżka do wordnetu w formacie PWN
     * @throws IOException
     */
    public SpatialRelationRecognizer2(final MaltParser malt, final Wordnet3 wordnet) throws IOException {
        this.malt = malt;
        filters = Lists.newLinkedList();
        filters.add(new RelationFilterPronoun());
        filters.add(new RelationFilterDifferentObjects());
        filters.add(this.semanticFilter);
        filters.add(new RelationFilterPrepositionBeforeLandmark());
        filters.add(new RelationFilterLandmarkTrajectorException());
        filters.add(new RelationFilterHolonyms(wordnet, new NamToWordnet(wordnet)));
    }

    /**
     * @return
     */
    public List<IRelationFilter> getFilters() {
        return filters;
    }

    /**
     * @return
     */
    public RelationFilterSemanticPattern getSemanticFilter() {
        return semanticFilter;
    }

    public List<SpatialExpression> recognize(final Document document) {
        return document.getSentences().stream()
                .map(this::recognize)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne i zwraca je jako listę obiektów SpatialExpression
     *
     * @param sentence
     * @return
     * @throws MaltChainedException
     */
    public List<SpatialExpression> recognize(final Sentence sentence) {
        return findCandidates(sentence).stream()
                .filter(se -> !getFilterDiscardingRelation(se).isPresent())
                .collect(Collectors.toList());
    }

    /**
     * Passes the spatial expression through the list of filters and return the first filter, for which
     * the expressions was discarded.
     *
     * @param se Spatial expression to test
     * @return
     */
    public Optional<String> getFilterDiscardingRelation(final SpatialExpression se) {
        final Iterator<IRelationFilter> filters = this.getFilters().iterator();
        while (filters.hasNext()) {
            final IRelationFilter filter = filters.next();
            if (!filter.pass(se)) {
                return Optional.ofNullable(filter.getClass().getSimpleName());
            }
        }
        return Optional.ofNullable(null);
    }

    /**
     * @param sentence
     * @return
     * @throws MaltChainedException
     */
    public List<SpatialExpression> findCandidates(final Sentence sentence) {
        NkjpSyntacticChunks.splitPrepNg(sentence);
        final SentenceAnnotationIndexTypePos anIndex = new SentenceAnnotationIndexTypePos(sentence);

        final List<SpatialExpression> relations = Stream.of(sentence)
                .map(s -> malt.parse(s, MappingNkjpToConllPos.get()))
                .peek(MaltSentence::printAsTree)
                .map(s -> findCandidatesByMalt(s, anIndex))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        getPatterns().stream()
                .map(p -> p.match(anIndex))
                .flatMap(Collection::stream)
                .map(this::frameToSpatialExpression)
                .forEach(relations::add);

        return relations.stream()
                .peek(r -> updateLandmarkIfRegion(r, anIndex))
                .peek(r -> replaceSpatialObjectsWithNamedEntities(r, anIndex))
                .collect(Collectors.toList());
    }

    // ToDo: Read patterns from a file after implementig ANTL parser.
    private List<SentencePattern> getPatterns() {
        final List<SentencePattern> patterns = Lists.newArrayList();

        patterns.add(new SentencePattern("NG*_Prep_NG*", new SentencePatternMatchSequence()
                .append(new SentencePatternMatchAnnotationPattern(NkjpSpejd.NGAny).withLabel("trajector"))
                .append(new SentencePatternMatchTokenPos("prep").withLabel("spatial_indicator"))
                .append(new SentencePatternMatchAnnotationPattern(NkjpSpejd.NGAny).withLabel("landmark"))));

        patterns.add(new SentencePattern("NG*_comma_Prep_NG*", new SentencePatternMatchSequence()
                .append(new SentencePatternMatchAnnotationPattern(NkjpSpejd.NGAny).withLabel("trajector"))
                .append(new SentencePatternMatchTokenOrth(","))
                .append(new SentencePatternMatchTokenPos("prep").withLabel("spatial_indicator"))
                .append(new SentencePatternMatchAnnotationPattern(NkjpSpejd.NGAny).withLabel("landmark"))));

        return patterns;
    }

    private SpatialExpression frameToSpatialExpression(final Frame<Annotation> frame) {
        final SpatialExpression se = new SpatialExpression();
        se.setTrajector(frame.getSlot("trajector"));
        se.setLandmark(frame.getSlot("landmark"));
        se.setSpatialIndicator(frame.getSlot("spatial_indicator"));
        se.setType(frame.getType());
        return se;
    }

    private void updateLandmarkIfRegion(final SpatialExpression se, final SentenceAnnotationIndexTypePos anIndex) {
        if (!isLandmarkObjectARegion(se)) {
            return;
        }

        final int i = se.getLandmark().getSpatialObject().getEnd() + 1;
        final List<Token> tokens = se.getLandmark().getSpatialObject().getSentence().getTokens();

        if (i < tokens.size() && anIndex.getAnnotationsOfTypeAtPos(patternAnnotationNam, i) != null) {
            anIndex.getLongestOfTypeAtPos(patternAnnotationNam, i)
                    .peek(an -> getLogger().info("REPLACE_REGION_NEXT_NAME {} => {}", se.getLandmark(), an.toString()))
                    .peek(an -> se.getLandmark().setRegion(se.getLandmark().getSpatialObject()))
                    .forEach(se.getLandmark()::setSpatialObject);
        } else {
            // Znajdż zagnieżdżone NG występujące po regionie
            Annotation ng = null;
            int j = se.getLandmark().getSpatialObject().getHead() + 1;
            while (j <= se.getLandmark().getSpatialObject().getEnd() && ng == null) {
                ng = CollectionUtils.emptyIfNull(anIndex.getAnnotationsOfTypeAtPos(NkjpSpejd.NGAny, j++)).stream()
                        .filter(an -> an.getBegin() > se.getLandmark().getSpatialObject().getHead())
                        .reduce((first, second) -> second).orElse(null);
            }
            if (ng != null) {
                getLogger().info("REPLACE_REGION_INNER_NG" + se.getLandmark().toString() + " => " + ng.toString());
                final Annotation newLandmark = CollectionUtils.emptyIfNull(anIndex.getAnnotationsOfTypeAtPos(NkjpSpejd.NGAny, se.getLandmark().getSpatialObject().getHead())).stream()
                        .filter(an -> an != se.getLandmark().getSpatialObject())
                        .sorted(Comparator.comparing(Annotation::length).reversed())
                        .findFirst()
                        .orElse(new Annotation(se.getLandmark().getSpatialObject().getBegin(), ng.getBegin() - 1, NkjpSpejd.NG, se.getLandmark().getSpatialObject().getSentence()));
                se.getLandmark().setRegion(newLandmark);
                se.getLandmark().setSpatialObject(ng);
            } else {
                // Znajdź pierwszy subst lub ign po prawej stronie
                Integer substOrIgn = null;
                int k = se.getLandmark().getSpatialObject().getHead() + 1;
                while (k <= se.getLandmark().getSpatialObject().getEnd() && substOrIgn == null) {
                    substOrIgn = objectPos.contains(tokens.get(k++).getDisambTag().getPos()) ? k : null;
                }
                if (substOrIgn != null) {
                    Option.of(new Annotation(se.getLandmark().getSpatialObject().getHead(), NkjpSpejd.NG, se.getLandmark().getSpatialObject().getSentence()))
                            .peek(se.getLandmark().getSpatialObject().getSentence()::addChunk)
                            .forEach(se.getLandmark()::setRegion);
                    Option.of(new Annotation(se.getLandmark().getSpatialObject().getHead() + 1, se.getLandmark().getSpatialObject().getEnd(), NkjpSpejd.NG, se.getLandmark().getSpatialObject().getSentence()))
                            .peek(se.getLandmark().getSpatialObject().getSentence()::addChunk)
                            .peek(an -> getLogger().info("REPLACE_REGION_INNER_SUBST_IGN {} => {}", se.getLandmark(), an))
                            .forEach(se.getLandmark()::setSpatialObject);
                }
            }
        }
    }

    private boolean isLandmarkObjectARegion(final SpatialExpression rel) {
        return regions.contains(rel.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase());
    }

    private void replaceSpatialObjectsWithNamedEntities(final SpatialExpression relation, final SentenceAnnotationIndexTypePos anIndex) {
        replaceSpatialObjectWithNamedEntity(relation.getLandmark(), anIndex);
        replaceSpatialObjectWithNamedEntity(relation.getTrajector(), anIndex);
    }

    private void replaceSpatialObjectWithNamedEntity(final SpatialObjectRegion spatialObject, final SentenceAnnotationIndexTypePos anIndex) {
        anIndex.getLongestOfTypeAtPos(patternAnnotationNam, spatialObject.getSpatialObject().getBegin())
                .filter(an -> an != spatialObject.getSpatialObject())
                .peek(an -> getLogger().debug("Replace {} ({}) with nam ({})", spatialObject.getSpatialObject().getType(), spatialObject, an))
                .peek(an -> an.setHead(spatialObject.getSpatialObject().getHead()))
                .forEach(spatialObject::setSpatialObject);
    }

    public List<SpatialExpression> findCandidatesByMalt(final MaltSentence maltSentence, final SentenceAnnotationIndexTypePos anIndex) {
        return IntStream.range(0, maltSentence.getSentence().getTokens().size())
                .mapToObj(pos -> findCandidatesByMalt(pos, maltSentence, anIndex))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<SpatialExpression> findCandidatesByMalt(final int pos, final MaltSentence maltSentence, final SentenceAnnotationIndexTypePos anIndex) {
        final List<SpatialExpression> ses = Lists.newArrayList();
        ses.addAll(findMaltCandidatesRootTrajector(pos, maltSentence, anIndex));
        ses.addAll(findMaltCandidatesRootVerb(pos, maltSentence, anIndex, "subj"));
        ses.addAll(findMaltCandidatesRootVerb(pos, maltSentence, anIndex, "obj"));
        ses.addAll(findMaltCandidatesRootVerb(pos, maltSentence, anIndex, "obj_th"));
        return ses;
    }

    private List<SpatialExpression> findMaltCandidatesRootTrajector(final int pos, final MaltSentence maltSentence, final SentenceAnnotationIndexTypePos anIndex) {
        final List<Token> tokens = maltSentence.getSentence().getTokens();
        final Token root = tokens.get(pos);
        final List<Integer> trajectors = Lists.newArrayList();
        final List<Pair<Integer, List<Integer>>> indicatorLandmark = Lists.newArrayList();

        if (objectPos.contains(root.getDisambTag().getPos())) {
            trajectors.add(pos);
            maltSentence.getLinksByTargetIndex(pos).stream()
                    .filter(link -> "adjunct".equals(link.getRelationType()))
                    .map(link -> tokens.get(link.getSourceIndex()))
                    .filter(token -> "prep".equals(token.getDisambTag().getPos()))
                    .forEach(token -> {
                        final int indicator = tokens.indexOf(token);
                        final List<Integer> landmarks = maltSentence.getLinksByTargetIndex(indicator).stream()
                                .map(MaltSentenceLink::getSourceIndex)
                                .map(nodeIndex -> getElementConjunction(maltSentence, nodeIndex))
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
                        if (landmarks.size() > 0) {
                            indicatorLandmark.add(new ImmutablePair<>(indicator, landmarks));
                        }
                    });
        }
        return componentsToSpatialExpressions(anIndex, trajectors, indicatorLandmark, "Malt_RootTr");
    }

    private List<SpatialExpression> findMaltCandidatesRootVerb(final int pos, final MaltSentence maltSentence, final SentenceAnnotationIndexTypePos anIndex, final String trPos) {
        final List<Token> tokens = maltSentence.getSentence().getTokens();
        if (!ClassFeature.BROAD_CLASSES.get("verb").contains(tokens.get(pos).getDisambTag().getPos())) {
            return Lists.newArrayList();
        }
        final List<Integer> trajectors = Lists.newArrayList();
        final List<Pair<Integer, List<Integer>>> indicatorLandmark = Lists.newArrayList();
        for (final MaltSentenceLink link : maltSentence.getLinksByTargetIndex(pos)) {
            final Token tokenChild = tokens.get(link.getSourceIndex());
            if (trPos.equals(link.getRelationType())) {
                trajectors.addAll(getElementConjunction(maltSentence, link.getSourceIndex()));
            } else if ("prep".equals(tokenChild.getDisambTag().getPos())) {
                final List<Integer> landmarks = maltSentence.getLinksByTargetIndex(link.getSourceIndex()).stream()
                        .map(prepLink -> getElementConjunction(maltSentence, prepLink.getSourceIndex()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                if (landmarks.size() > 0) {
                    indicatorLandmark.add(new ImmutablePair<>(link.getSourceIndex(), landmarks));
                }
            }
        }
        return componentsToSpatialExpressions(anIndex, trajectors, indicatorLandmark, "Malt_RootVerb_" + trPos);
    }

    private List<Integer> getElementConjunction(final MaltSentence maltSentence, final int nodeIndex) {
        final Token token = maltSentence.getSentence().getTokens().get(nodeIndex);
        if (orthConj.contains(token.getOrth().toLowerCase())) {
            return maltSentence.getLinksByTargetIndex(nodeIndex).stream()
                    .map(MaltSentenceLink::getSourceIndex)
                    .map(index -> getObjectOnPath(maltSentence, index))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else if (this.objectPos.contains(token.getDisambTag().getPos())) {
            return getObjectOnPath(maltSentence, nodeIndex);
        } else {
            return Lists.newArrayList();
        }
    }

    private List<Integer> getObjectOnPath(final MaltSentence maltSentence, final int nodeIndex) {
        //final Token token = maltSentence.getSentence().getTokens().get(nodeIndex);
        return Lists.newArrayList(nodeIndex);
    }

    private List<SpatialExpression>
    componentsToSpatialExpressions(final SentenceAnnotationIndexTypePos anIndex, final List<Integer> trajectors,
                                   final List<Pair<Integer, List<Integer>>> indicatorLandmark, final String type) {
        final Sentence sentence = anIndex.getSentence();
        final List<SpatialExpression> srs = Lists.newArrayList();
        final String typeTR = trajectors.size() > 1 ? "_TrSet" : "";
        for (final Integer trajector : trajectors) {
            final Annotation tr = getOrCreateAnnotation(sentence, trajector, "TR", anIndex);
            for (final Pair<Integer, List<Integer>> pair : indicatorLandmark) {
                final Annotation si = anIndex.getLongestOfTypeAtPos(NkjpSpejd.Prep, pair.getLeft())
                        .getOrElse(sentence.createAnnotation(pair.getLeft(), "SI"));
                final String typeLM = pair.getRight().size() > 1 ? "_LmSet" : "";
                for (final Integer landmark : pair.getRight()) {
                    final Annotation lm = getOrCreateAnnotation(sentence, landmark, "LM", anIndex);
                    srs.add(new SpatialExpression(type + typeLM + typeTR, tr, si, lm));
                }
            }
        }
        return srs;
    }


    private Annotation getOrCreateAnnotation(final Sentence sentence, final Integer index, final String type, final SentenceAnnotationIndexTypePos anIndex) {
        return Option.of(anIndex.getAnnotationsOfTypeAtPos(NkjpSpejd.NGAny, index))
                .filter(list -> list.size() > 0)
                .map(list -> list.stream().sorted(Comparator.comparing(Annotation::length)).collect(Collectors.toList()))
                .peek(list -> {
                    if (list.size() > 1) {
                        getLogger().warn("More than one annotation to choose from: {}", list);
                    }
                })
                .map(list -> list.get(0))
                .getOrElse(sentence.createAnnotation(index, type));
    }

}
