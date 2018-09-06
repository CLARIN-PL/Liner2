package g419.spatial.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.schema.kpwr.KpwrSpatial;
import g419.corpus.schema.tagset.MappingNkjpToConllPos;
import g419.corpus.structure.*;
import g419.liner2.core.features.tokens.ClassFeature;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import g419.spatial.filter.*;
import g419.spatial.structure.SpatialExpression;
import g419.spatial.structure.SpatialRelationSchema;
import g419.toolbox.wordnet.NamToWordnet;
import g419.toolbox.wordnet.Wordnet3;
import org.apache.log4j.Logger;
import org.maltparser.core.exception.MaltChainedException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpatialRelationRecognizer extends ISpatialRelationRecognizer {

    private MaltParser malt = null;

    private final Pattern annotationsPrep = Pattern.compile("^PrepNG.*$");
    private final Pattern annotationsNg = Pattern.compile("^NG.*$");
    private final Pattern patternAnnotationNam = Pattern.compile("^nam(_(fac|liv|loc|pro|oth).*|$)");

    private final Set<String> objectPos = Sets.newHashSet();
    private final Set<String> regions = SpatialResources.getRegions();

    private final Logger logger = Logger.getLogger(this.getClass());


    /**
     * @param malt    Ścieżka do modelu Maltparsera
     * @param wordnet Ścieżka do wordnetu w formacie PWN
     * @throws IOException
     */
    public SpatialRelationRecognizer(final MaltParser malt, final Wordnet3 wordnet) throws IOException {
        this.malt = malt;

        this.objectPos.add("subst");
        this.objectPos.add("ign");
        this.objectPos.add("brev");

        filters.add(new RelationFilterPronoun());
        filters.add(new RelationFilterDifferentObjects());
        filters.add(semanticFilter);
        filters.add(new RelationFilterPrepositionBeforeLandmark());
        filters.add(new RelationFilterLandmarkTrajectorException());
        filters.add(new RelationFilterHolonyms(wordnet, new NamToWordnet(wordnet)));
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne i dodaje je do dokumentu jako obiekty Frame o type "spatial"
     *
     * @param document
     * @throws MaltChainedException
     */
    public void recognizeInPlace(final Document document) {
        try {
            for (final Paragraph paragraph : document.getParagraphs()) {
                for (final Sentence sentence : paragraph.getSentences()) {
                    for (final SpatialExpression rel : this.recognize(sentence)) {
                        final Frame<Annotation> f = SpatialRelationRecognizer.convertSpatialToFrame(rel);
                        document.getFrames().add(f);
                    }
                }
            }
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne i zwraca je jako listę obiektów SpatialExpression
     *
     * @param sentence
     * @return
     * @throws MaltChainedException
     */
    public List<SpatialExpression> recognize(final Sentence sentence) throws MaltChainedException {
        final List<SpatialExpression> candidateRelations = this.findCandidates(sentence);
        final List<SpatialExpression> finalRelations = new ArrayList<>();
        if (candidateRelations.size() > 0) {
            for (final SpatialExpression rel : candidateRelations) {

                boolean pass = true;

                for (final IRelationFilter filter : filters) {
                    if (!filter.pass(rel)) {
                        pass = false;
                        break;
                    }
                }

                if (pass) {
                    finalRelations.add(rel);
                }
            }
        }
        return finalRelations;
    }

    /**
     * Konwertuje strukturę SpatialRelation do uniwersalnego formatu Frame.
     *
     * @param relation
     * @return
     */
    public static Frame<Annotation> convertSpatialToFrame(final SpatialExpression relation) {
        final Frame<Annotation> f = new Frame<>(KpwrSpatial.SPATIAL_FRAME_TYPE);
        f.set(KpwrSpatial.SPATIAL_INDICATOR, relation.getSpatialIndicator());
        f.set(KpwrSpatial.SPATIAL_LANDMARK, relation.getLandmark().getSpatialObject());
        f.set(KpwrSpatial.SPATIAL_TRAJECTOR, relation.getTrajector().getSpatialObject());
        f.set(KpwrSpatial.SPATIAL_REGION, relation.getLandmark().getRegion());

        f.setSlotAttribute(KpwrSpatial.SPATIAL_TRAJECTOR, "sumo", String.join(", ", relation.getTrajectorConcepts()));
        f.setSlotAttribute(KpwrSpatial.SPATIAL_LANDMARK, "sumo", String.join(", ", relation.getLandmarkConcepts()));
        f.setSlotAttribute("debug", "pattern", relation.getType());

        final Set<String> schemas = new HashSet<>();
        for (final SpatialRelationSchema schema : relation.getSchemas()) {
            schemas.add(schema.getName());
        }
        f.setSlotAttribute("debug", "schema", String.join("; ", schemas));

        return f;
    }

    private Map<Integer, Annotation> createAnnotationIndex(final Collection<Annotation> annotations) {
        final Map<Integer, Annotation> index = Maps.newHashMap();
        annotations.stream().forEach(
                an -> IntStream.rangeClosed(an.getBegin(), an.getEnd()).forEach(n -> index.put(n, an)));
        return index;
    }

    private Map<Integer, Annotation> createAnnotationIndex(final Collection<Annotation> annotations, final String annotationType) {
        return createAnnotationIndex(annotations.stream().filter(an -> annotationType.equals(an.getType())).collect(Collectors.toList()));
    }

    /**
     * @param sentence
     * @return
     * @throws MaltChainedException
     */
    @Override
    public List<SpatialExpression> findCandidates(final Sentence sentence) {
        NkjpSyntacticChunks.splitPrepNg(sentence);
        final MaltSentence maltSentence = malt.parse(sentence, MappingNkjpToConllPos.get());

        /* Zaindeksuj różne typy fraz */
        final Map<Integer, Annotation> chunkNpTokens = createAnnotationIndex(sentence.getChunks(), "chunk_np");
        final Map<Integer, Annotation> chunkVerbfinTokens = createAnnotationIndex(sentence.getChunks(), "Verbfin");
        final Map<Integer, Annotation> chunkPrepTokens = createAnnotationIndex(sentence.getChunks(), "Prep");
        final Map<Integer, Annotation> chunkPpasTokens = createAnnotationIndex(sentence.getChunks(), "Ppas");
        final Map<Integer, Annotation> chunkPactTokens = createAnnotationIndex(sentence.getChunks(), "Pact");
        final Map<Integer, Annotation> chunkNamesTokens = createAnnotationIndex(sentence.getAnnotations(this.patternAnnotationNam));
        final Map<Integer, List<Annotation>> chunkNgTokens = Maps.newHashMap();

        /* Zaindeksuj pierwsze tokeny anotacji NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsNg)) {
            for (Integer n = an.getBegin(); n <= an.getEnd(); n++) {
                chunkNgTokens.computeIfAbsent(n, p -> Lists.newLinkedList()).add(an);
            }
        }

        final List<SpatialExpression> relations = new LinkedList<>();

        // Second Iteration only
        relations.addAll(this.findCandidatesFirstNgAnyPrepNg(sentence, chunkNgTokens, chunkNpTokens, chunkPrepTokens));
        relations.addAll(this.findCandidatesByMalt(sentence, maltSentence, chunkPrepTokens, chunkNamesTokens, chunkNgTokens));

        relations.addAll(this.findCandidatesNgPpasPrepNg(
                sentence, chunkNgTokens, chunkNpTokens, chunkPpasTokens, chunkPrepTokens));
        relations.addAll(this.findCandidatesNgPactPrepNg(
                sentence, chunkNgTokens, chunkNpTokens, chunkPactTokens, chunkPrepTokens));


        // Sprawdź, czy landmarkiem jest region. Jeżeli tak, to przesuń landmark na najbliższych ign lub subst
        for (final SpatialExpression rel : relations) {
            if (this.regions.contains(rel.getLandmark().getSpatialObject().getHeadToken().getDisambTag().getBase())) {
                final int i = rel.getLandmark().getSpatialObject().getEnd() + 1;
                final List<Token> tokens = rel.getLandmark().getSpatialObject().getSentence().getTokens();

                if (i < tokens.size() && chunkNamesTokens.get(i) != null) {
                    final Annotation an = chunkNamesTokens.get(i);
                    this.logger.info("REPLACE_REGION_NEXT_NAME " + rel.getLandmark().toString() + " => " + an.toString());
                    rel.getLandmark().setRegion(rel.getLandmark().getSpatialObject());
                    rel.getLandmark().setSpatialObject(an);
                } else {
                    // Znajdż zagnieżdżone NG występujące po regionie
                    Annotation ng = null;
                    int j = rel.getLandmark().getSpatialObject().getHead() + 1;
                    while (j <= rel.getLandmark().getSpatialObject().getEnd() && ng == null) {
                        final List<Annotation> innerNgs = chunkNgTokens.get(j);
                        if (innerNgs != null) {
                            for (final Annotation an : innerNgs) {
                                if (an.getBegin() > rel.getLandmark().getSpatialObject().getHead()) {
                                    ng = an;
                                }
                            }
                        }
                        j++;
                    }
                    if (ng != null) {
                        this.logger.info("REPLACE_REGION_INNER_NG" + rel.getLandmark().toString() + " => " + ng.toString());
                        Annotation newLandmark = null;
                        final List<Annotation> newLandmakrs = chunkNgTokens.get(rel.getLandmark().getSpatialObject().getHead());
                        if (newLandmakrs != null) {
                            for (final Annotation an : newLandmakrs) {
                                if (an != rel.getLandmark().getSpatialObject() && (newLandmark == null || newLandmark.getTokens().size() < an.getTokens().size())) {
                                    newLandmark = an;
                                }
                            }
                        }
                        if (newLandmark == null) {
                            newLandmark = new Annotation(rel.getLandmark().getSpatialObject().getBegin(), ng.getBegin() - 1, "NG", sentence);
                        }
                        rel.getLandmark().setRegion(newLandmark);
                        rel.getLandmark().setSpatialObject(ng);
                    } else {
                        // Znajdź pierwszy subst lub ign po prawej stronie
                        Integer subst_or_ign = null;
                        int k = rel.getLandmark().getSpatialObject().getHead() + 1;
                        while (k <= rel.getLandmark().getSpatialObject().getEnd() && subst_or_ign == null) {
                            if (this.objectPos.contains(tokens.get(k).getDisambTag().getPos())) {
                                subst_or_ign = k;
                            }
                            k++;
                        }
                        if (subst_or_ign != null) {
                            // Wszystko po prawje staje się nową anotacją NG
                            final Annotation newRegion = new Annotation(rel.getLandmark().getSpatialObject().getHead(), "NG", sentence);
                            final Annotation newLandmark = new Annotation(rel.getLandmark().getSpatialObject().getHead() + 1, rel.getLandmark().getSpatialObject().getEnd(), "NG", sentence);
                            sentence.addChunk(newRegion);
                            sentence.addChunk(newLandmark);
                            this.logger.info("REPLACE_REGION_INNER_SUBST_IGN" + rel.getLandmark().toString() + " => " + newLandmark);
                            rel.getLandmark().setSpatialObject(newLandmark);
                            rel.getLandmark().setRegion(newRegion);
                        }
                    }
                }
            }
        }

        // Usuń kandydatów, dla których spatial indicator jest częścią nazwy
        final List<SpatialExpression> toRemove = new ArrayList<>();
        for (final SpatialExpression spatial : relations) {
            if (chunkNamesTokens.get(spatial.getSpatialIndicator().getHead()) != null) {
                toRemove.add(spatial);
            }
        }
        relations.removeAll(toRemove);

        // Jeżeli frazy NG pokrywają się z nam_, to podmień anotacje
        this.replaceNgWithNames(sentence, relations, chunkNamesTokens);

        return relations;
    }

    /**
     * Dla fraz NG, które pokrywają się z nazwą własną, zmienia NG na nazwę.
     *
     * @param sentence
     * @param relations
     */
    private void replaceNgWithNames(final Sentence sentence, final List<SpatialExpression> relations, final Map<Integer, Annotation> names) {
        for (final SpatialExpression relation : relations) {
            // Sprawdź landmark
            //String landmarkKey = String.format("%d:%d",relation.getLandmark().getBegin(), relation.getLandmark().getEnd());
            final Integer landmarkKey = relation.getLandmark().getSpatialObject().getBegin();
            final Annotation landmarkName = names.get(landmarkKey);
            if (landmarkName != null && landmarkName != relation.getLandmark().getSpatialObject()) {
                Logger.getLogger(this.getClass()).info(String.format("Replace %s (%s) with nam (%s)", relation.getLandmark().getSpatialObject().getType(), relation.getLandmark(), landmarkName));
                landmarkName.setHead(relation.getLandmark().getSpatialObject().getHead());
                relation.getLandmark().setSpatialObject(landmarkName);
            }
            // Sprawdź trajector
            //String trajectorKey = String.format("%d:%d",relation.getTrajector().getBegin(), relation.getTrajector().getEnd());
            final Integer trajectorKey = relation.getTrajector().getSpatialObject().getBegin();
            final Annotation trajectorName = names.get(trajectorKey);
            if (trajectorName != null && trajectorName != relation.getTrajector().getSpatialObject()) {
                Logger.getLogger(this.getClass()).info(String.format("Replace %s (%s) with nam (%s)", relation.getTrajector().getSpatialObject().getType(), relation.getTrajector(), trajectorName));
                trajectorName.setHead(relation.getTrajector().getSpatialObject().getHead());
                relation.getTrajector().setSpatialObject(trajectorName);
            }
        }
    }


    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgAnyPrepNg(final Sentence sentence,
                                                             final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                             final Map<Integer, Annotation> chunkNpTokens,
                                                             final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców NG* prep NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            Integer trajectorPrevId = an.getBegin() - 1;

            boolean breakSearchPrev = false;
            while (!breakSearchPrev && trajectorPrevId >= 0 && mapTokenIdToAnnotations.get(trajectorPrevId) == null) {
                /* Przecinek i nawias zamykający przerywają poszykiwanie */
                final String orth = sentence.getTokens().get(trajectorPrevId).getOrth();
                if (orth.equals(",") || orth.equals(")")) {
                    breakSearchPrev = true;
                }
                trajectorPrevId--;
            }
            final Integer trajectorId = trajectorPrevId;

            if (chunkNpTokens.get(landmarkId) != null && !breakSearchPrev && chunkNpTokens.get(trajectorPrevId) == chunkNpTokens.get(landmarkId)) {
                String type = "";
                if (trajectorPrevId + 1 == preposition.getBegin()) {
                    type = "<NG|PrepNG>";
                } else {
                    type = "<NG|...|PrepNG>";
                }
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgPrepNgNoNp(final Sentence sentence,
                                                              final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                              final Map<Integer, Annotation> chunkNpTokens,
                                                              final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców NG* prep NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            final Integer trajectorId = an.getBegin() - 1;

            if (chunkNpTokens.get(landmarkId) == null
                    && chunkNpTokens.get(trajectorId) == null) {
                final String type = "NG|PrepNG";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                if (trajectors != null) {
                    relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
                }
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgPrepNgDiffNp(final Sentence sentence,
                                                                final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                final Map<Integer, Annotation> chunkNpTokens,
                                                                final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców NG* prep NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            final Integer trajectorId = an.getBegin() - 1;

            if (chunkNpTokens.get(landmarkId) != null
                    && chunkNpTokens.get(trajectorId) != null
                    && chunkNpTokens.get(trajectorId) != chunkNpTokens.get(landmarkId)) {
                final String type = "<NG><PrepNG>";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                if (trajectors != null) {
                    relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
                }
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesPrepNgNgDiffNp(final Sentence sentence,
                                                                final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                final Map<Integer, Annotation> chunkNpTokens,
                                                                final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców NG* prep NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            final Integer trajectorId = an.getEnd() + 1;

            if (chunkNpTokens.get(landmarkId) != null
                    && chunkNpTokens.get(trajectorId) != null
                    && chunkNpTokens.get(trajectorId) != chunkNpTokens.get(landmarkId)) {
                final String type = "<PrepNG><NG>";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgPrepNgPpasPrepNg(final Sentence sentence,
                                                                    final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                    final Map<Integer, Annotation> chunkNpTokens,
                                                                    final Map<Integer, Annotation> chunkPrepTokens,
                                                                    final Map<Integer, Annotation> chunkPpasTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców NG* prep NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            if (an.getBegin() - 1 <= 0 || !sentence.getTokens().get(an.getBegin() - 1).getOrth().equals(",")) {
                continue;
            }

            final Annotation ppas = chunkPpasTokens.get(an.getBegin() - 1);

            if (ppas == null) {
                continue;
            }

            final List<Annotation> ngs = mapTokenIdToAnnotations.get(ppas.getBegin() - 1);
            if (ngs == null) {
                continue;
            }

            final Annotation prep = chunkPrepTokens.get(ngs.get(0).getBegin() - 1);
            if (prep == null) {
                continue;
            }

            final Integer trajectorId = prep.getBegin() - 1;

            if (chunkNpTokens.get(landmarkId) != null && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {
                final String type = "<NG|PrepNG|Ppas|PrepNG>";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                if (trajectors != null) {
                    relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
                }
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgPrepNgCommaPrepNg(final Sentence sentence,
                                                                     final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                     final Map<Integer, Annotation> chunkNpTokens,
                                                                     final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców NG* prep NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            final Integer commaId = preposition.getBegin() - 1;

            if (commaId <= 0 || !sentence.getTokens().get(commaId).getOrth().equals(",")) {
                continue;
            }

            Integer prepId = null;
            final List<Annotation> ngs = mapTokenIdToAnnotations.get(commaId - 1);
            if (ngs == null) {
                continue;
            } else {
                for (final Annotation a : ngs) {
                    if (prepId == null) {
                        prepId = a.getBegin() - 1;
                    } else {
                        prepId = Math.min(prepId, a.getBegin() - 1);
                    }
                }
            }

            final Annotation prep = chunkPrepTokens.get(prepId);
            if (prep == null) {
                continue;
            }

            final Integer trajectorId = prep.getBegin() - 1;

            if (chunkNpTokens.get(landmarkId) != null
                    && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {
                final String type = "<NG|PrepNG|Comma|PrepNG>";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                if (trajectors != null) {
                    relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
                }
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgPrepNgPrepNg(final Sentence sentence,
                                                                final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                final Map<Integer, Annotation> chunkNpTokens,
                                                                final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców NG* prep NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();

            final List<Annotation> ngs = mapTokenIdToAnnotations.get(an.getBegin() - 1);
            Integer prepId = null;
            if (ngs == null) {
                continue;
            } else {
                for (final Annotation a : ngs) {
                    if (prepId == null) {
                        prepId = a.getBegin() - 1;
                    } else {
                        prepId = Math.min(prepId, a.getBegin() - 1);
                    }
                }
            }

            final Annotation prep = chunkPrepTokens.get(prepId);
            if (prep == null) {
                continue;
            }

            final Integer trajectorId = prep.getBegin() - 1;

            if (chunkNpTokens.get(landmarkId) != null
                    && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {
                final String type = "<NG|PrepNG|PrepNG>";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgNgPrepNg(final Sentence sentence,
                                                            final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                            final Map<Integer, Annotation> chunkNpTokens,
                                                            final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców NG* prep NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();

            final List<Annotation> ngs = mapTokenIdToAnnotations.get(preposition.getBegin() - 1);
            Integer trajectorId = null;
            if (ngs == null) {
                continue;
            } else {
                for (final Annotation a : ngs) {
                    if (trajectorId == null) {
                        trajectorId = a.getBegin() - 1;
                    } else {
                        trajectorId = Math.min(trajectorId, a.getBegin() - 1);
                    }
                }
            }

            if (chunkNpTokens.get(landmarkId) != null && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {
                final String type = "<NG|NG|PrepNG>";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                if (trajectors != null) {
                    relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
                }
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesFirstNgAnyPrepNg(final Sentence sentence,
                                                                  final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                  final Map<Integer, Annotation> chunkNpTokens,
                                                                  final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców NG* prep NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            Integer trajectorId = an.getBegin() - 1;

            boolean breakSearchPrev = false;
            while (!breakSearchPrev && trajectorId >= 0 && mapTokenIdToAnnotations.get(trajectorId) == null) {
                /* Przecinek i nawias zamykający przerywają poszykiwanie */
                final String orth = sentence.getTokens().get(trajectorId).getOrth();
                if (orth.equals(",") || orth.equals(")")) {
                    breakSearchPrev = true;
                }
                trajectorId--;
            }

            if (chunkNpTokens.get(landmarkId) != null && !breakSearchPrev && chunkNpTokens.get(trajectorId) == chunkNpTokens.get(landmarkId)) {

                while (trajectorId > 0
                        && mapTokenIdToAnnotations.get(trajectorId - 1) != null
                        && chunkNpTokens.get(trajectorId - 1) == chunkNpTokens.get(landmarkId)
                        ) {
                    trajectorId = mapTokenIdToAnnotations.get(trajectorId - 1).get(0).getBegin();
                }

                String type = "";
                if (trajectorId + 1 == preposition.getBegin()) {
                    type = "<FirstNG|PrepNG>";
                } else {
                    type = "<FirstNG|...|PrepNG>";
                }

                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesPrepNgNg(final Sentence sentence,
                                                          final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                          final Map<Integer, Annotation> chunkNpTokens,
                                                          final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców prep NG* NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            final Integer trajectorId = an.getEnd() + 1;

            if (chunkNpTokens.get(landmarkId) != null
                    && chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId)) {
                final String type = "<PrepNG|NG>";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgPrepNg(final Sentence sentence,
                                                          final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                          final Map<Integer, Annotation> chunkNpTokens,
                                                          final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców prep NG* NG* */
        for (final Annotation landmark : sentence.getAnnotations(this.annotationsNg)) {

            final Integer prepId = landmark.getBegin() - 1;
            if (prepId <= 0
                    || !sentence.getTokens().get(prepId).getDisambTag().equals("prep")
                    || chunkPrepTokens.get(prepId) != null) {
                continue;
            }

            final Integer landmarkId = landmark.getBegin();
            final Integer trajectorId = prepId - 1;

            if (chunkNpTokens.get(landmarkId) != null
                    && chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId)) {
                final String type = "<NG|prep|NG>";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                relations.addAll(this.generateAllCombinations(type, trajectors, landmarks,
                        new Annotation(prepId, "Prep", sentence)));
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesPrepNgVerbfinNg(final Sentence sentence,
                                                                 final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                 final Map<Integer, Annotation> chunkVerbfinTokens,
                                                                 final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców prep NG* NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            final Integer verbfinId = an.getEnd() + 1;
            final Annotation verbfin = chunkVerbfinTokens.get(verbfinId);

            if (verbfin != null) {
                final Integer trajectorId = verbfin.getEnd() + 1;
                final String type = "PrepNG|Verbfin|NG";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgVerbfinPrepNg(final Sentence sentence,
                                                                 final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                                 final Map<Integer, Annotation> chunkVerbfinTokens,
                                                                 final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców prep NG* NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            final Integer verbfinId = preposition.getBegin() - 1;
            final Annotation verbfin = chunkVerbfinTokens.get(verbfinId);

            if (verbfin != null) {
                final Integer trajectorId = verbfin.getBegin() - 1;
                final String type = "NG|Verbfin|PrepNG";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                if (trajectors != null) {
                    relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
                }
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgPpasPrepNg(final Sentence sentence,
                                                              final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                              final Map<Integer, Annotation> chunkNpTokens,
                                                              final Map<Integer, Annotation> chunkPpasTokens,
                                                              final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców prep NG* NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            final Integer verbfinId = preposition.getBegin() - 1;
            final Annotation ppas = chunkPpasTokens.get(verbfinId);
            if (ppas == null) {
                continue;
            }
            final Integer trajectorId = ppas.getBegin() - 1;

            if (ppas != null && chunkNpTokens.get(landmarkId) != null
                    && chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId)) {
                final String type = "<NG|Ppas|PrepNG>";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                if (trajectors != null) {
                    relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
                }
            }
        }
        return relations;
    }

    /**
     * Rozpoznaje wyrażenia przestrzenne występujące we wzrocu NG* prep NG*
     *
     * @param sentence
     */
    public List<SpatialExpression> findCandidatesNgPactPrepNg(final Sentence sentence,
                                                              final Map<Integer, List<Annotation>> mapTokenIdToAnnotations,
                                                              final Map<Integer, Annotation> chunkNpTokens,
                                                              final Map<Integer, Annotation> chunkPactTokens,
                                                              final Map<Integer, Annotation> chunkPrepTokens) {
        final List<SpatialExpression> relations = new LinkedList<>();
        /* Szukaj wzorców prep NG* NG* */
        for (final Annotation an : sentence.getAnnotations(this.annotationsPrep)) {
            final Annotation preposition = chunkPrepTokens.get(an.getBegin());
            if (preposition == null) {
                this.logger.warn("Prep annotation for PrepNG not found: " + an.toString());
                continue;
            }

            final Integer landmarkId = an.getBegin() + preposition.getTokens().size();
            final Integer verbfinId = preposition.getBegin() - 1;
            final Annotation pact = chunkPactTokens.get(verbfinId);
            if (pact == null) {
                continue;
            }
            final Integer trajectorId = pact.getBegin() - 1;

            if (pact != null && chunkNpTokens.get(landmarkId) != null
                    && chunkNpTokens.get(landmarkId) == chunkNpTokens.get(trajectorId)) {
                final String type = "<NG|Pact|PrepNG>";
                final List<Annotation> trajectors = mapTokenIdToAnnotations.get(trajectorId);
                final List<Annotation> landmarks = mapTokenIdToAnnotations.get(landmarkId);
                if (trajectors != null) {
                    relations.addAll(this.generateAllCombinations(type, trajectors, landmarks, preposition));
                }
            }
        }
        return relations;
    }

    /**
     * @param sentence
     * @param maltSentence
     * @return
     */
    public List<SpatialExpression> findCandidatesByMalt(final Sentence sentence, final MaltSentence maltSentence,
                                                        final Map<Integer, Annotation> chunkPrepTokens, final Map<Integer, Annotation> chunkNamesTokens, final Map<Integer, List<Annotation>> mapTokenIdToAnnotations) {
        final List<SpatialExpression> srs = new ArrayList<>();
        for (int i = 0; i < sentence.getTokens().size(); i++) {
            final Token token = sentence.getTokens().get(i);
            final List<Integer> landmarks = new ArrayList<>();
            final List<Integer> trajectors = new ArrayList<>();
            Integer indicator = null;
            final String type = "MALT";
            String typeLM = "";
            String typeTR = "";
            if (ClassFeature.BROAD_CLASSES.get("verb").contains(token.getDisambTag().getPos())) {
                final List<MaltSentenceLink> links = maltSentence.getLinksByTargetIndex(i);
                for (final MaltSentenceLink link : links) {
                    final Token tokenChild = sentence.getTokens().get(link.getSourceIndex());
                    if (link.getRelationType().equals("subj")) {
                        if (tokenChild.getDisambTag().getBase().equals("i")
                                || tokenChild.getDisambTag().getBase().equals("oraz")) {
                            typeTR = "_TRconj";
                            for (final MaltSentenceLink trLink : maltSentence.getLinksByTargetIndex(link.getSourceIndex())) {
                                landmarks.add(trLink.getSourceIndex());
                            }
                        } else if (this.objectPos.contains(tokenChild.getDisambTag().getPos())) {
                            trajectors.add(link.getSourceIndex());
                        }
                    } else if (tokenChild.getDisambTag().getPos().equals("prep")) {
                        indicator = link.getSourceIndex();
                        for (final MaltSentenceLink prepLink : maltSentence.getLinksByTargetIndex(link.getSourceIndex())) {
                            final Token lm = sentence.getTokens().get(prepLink.getSourceIndex());
                            if (lm.getOrth().equals(",")) {
                                typeLM = "_LMconj";
                                for (final MaltSentenceLink prepLinkComma : maltSentence.getLinksByTargetIndex(prepLink.getSourceIndex())) {
                                    landmarks.add(prepLinkComma.getSourceIndex());
                                }
                            } else if (this.objectPos.contains(lm.getDisambTag().getPos())) {
                                landmarks.add(prepLink.getSourceIndex());
                            }
                        }
                    }
                }
            }

            if (landmarks.size() > 0 && trajectors.size() > 0 && indicator != null) {
                for (final Integer landmark : landmarks) {
                    for (final Integer trajector : trajectors) {
                        /* Ustal spatial indicator */
                        Annotation si = chunkPrepTokens.get(indicator);
                        if (si == null) {
                            si = new Annotation(indicator, "SI", sentence);
                            sentence.addChunk(si);
                        }

                        /* Ustal trajector */
                        Annotation tr = null;
                        final List<Annotation> trs = mapTokenIdToAnnotations.get(trajector);
                        if (trs != null) {
                            tr = trs.get(0);
                        }
                        if (tr == null) {
                            tr = new Annotation(trajector, "TR", sentence);
                            sentence.addChunk(tr);
                        }

                        /* Ustal landmark */
                        Annotation lm = null;
                        final List<Annotation> lms = mapTokenIdToAnnotations.get(landmark);
                        if (lms != null) {
                            lm = lms.get(0);
                        }
                        if (lm == null) {
                            lm = new Annotation(landmark, "LM", sentence);
                            sentence.addChunk(lm);
                        }

                        srs.add(new SpatialExpression(type + typeLM + typeTR, tr, si, lm));
                    }
                }
            }
        }
        return srs;
    }

    /**
     * @param type
     * @param trajectors
     * @param landmarks
     * @param preposition
     * @return
     */
    private List<SpatialExpression> generateAllCombinations(final String type, final List<Annotation> trajectors, final List<Annotation> landmarks, final Annotation preposition) {
        final List<SpatialExpression> relations = new ArrayList<>();
        if (trajectors != null && landmarks != null) {
            for (final Annotation trajector : trajectors) {
                for (final Annotation landmark : landmarks) {
                    final SpatialExpression sr = new SpatialExpression(type, trajector, preposition, landmark);
                    relations.add(sr);
                }
            }
        }
        return relations;
    }

}
