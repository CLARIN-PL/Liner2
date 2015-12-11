package g419.crete.api.refine;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.crete.api.CreteOptions;
import g419.crete.api.annotation.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by akaczmarek on 26.10.15.
 */
public class CoverAnnotationDocumentRefiner {

    private AbstractAnnotationSelector relationalSelector;
//    private AbstractAnnotationSelector nonRelationalSelector;

    public static final Pattern namedPattern = Pattern.compile("nam_.*");
    private AbstractAnnotationSelector preFilterSelector;

    public CoverAnnotationDocumentRefiner(AbstractAnnotationSelector preFilterSelector){
        relationalSelector = new AllAnnotationSelector();
        this.preFilterSelector = preFilterSelector;
    }

    public void fixPair(Annotation transformedAnnotation, Annotation finalAnnotation, Document document){
//        System.out.println(transformedAnnotation + " --> " + finalAnnotation);
        document.rewireSingleRelations(transformedAnnotation, finalAnnotation);
    }

    public boolean needsFixing(Annotation a1, Annotation a2){
        if(!a1.getSentence().getId().equals(a2.getSentence().getId())) return false;
        if(!a1.getTokens().equals(a2.getTokens())) return false;
        if(!a1.getText().equals(a2.getText())) return false;
        return true;
    }

    public boolean needsFixing(Annotation a1, Annotation a2, HashMap<String, List<Pattern>> transformations){
        return needsFixing(a1, a2) && transformations.containsKey(a1.getType()) && transformations.get(a1.getType()).stream().anyMatch(p -> p.matcher(a2.getType()).matches());
    }

    public Document refineExpliciteAnnotations2(Document document){
        HashMap<AbstractAnnotationSelector, AbstractAnnotationSelector> transformations = getTransformations();
        List<Annotation> annotationsToRemove = new ArrayList<>();

        for(Map.Entry<AbstractAnnotationSelector, AbstractAnnotationSelector> selectorPair : transformations.entrySet()){
            // Selektor anotacji "złych"
            AbstractAnnotationSelector firstSelector = selectorPair.getKey();
            // Selektor anotacji "dobrych"
            AbstractAnnotationSelector secondSelector = selectorPair.getValue();
            for(Annotation annotation1: firstSelector.selectAnnotations(document)){
                for(Annotation annotation2: secondSelector.selectAnnotations(document)){
                    if(!annotation1.equals(annotation2) && needsFixing(annotation1, annotation2)) {

                        fixPair(annotation1, annotation2, document);
                        annotationsToRemove.add(annotation1);

                    }
                }
            }


        }

        document.filterAnnotationClusters(annotationsToRemove);
        document.removeAnnotations(annotationsToRemove);

        return document;
    }

    public Document refineRestOfAnnotations(Document document){
        List<Annotation> relationalAnnotations = relationalSelector.selectAnnotations(document);
        List<Annotation> toRemove = new ArrayList<>();

        for (Annotation annotation1 : relationalAnnotations){
            for(Annotation annotation2 : relationalAnnotations){
                if(!annotation1.equals(annotation2) && needsFixing(annotation1, annotation2) && !toRemove.contains(annotation2)){
                    fixPair(annotation1, annotation2, document);
                    toRemove.add(annotation1);
                }
            }
        }

        document.filterAnnotationClusters(toRemove);
        document.removeAnnotations(toRemove);

        return document;
    }

    public Document renameNonRelationalNonOverlapping(Document document){
        HashMap<AbstractAnnotationSelector, String> transformations = nonRelationalMapping();

        for(AbstractAnnotationSelector selector : transformations.keySet()){
            String type = transformations.get(selector);
            for(Annotation ann: selector.selectAnnotations(document)){
                boolean covered = false;
                Sentence s = ann.getSentence();
                for(Annotation other : s.getChunks()){
                    if(!other.equals(ann) && ann.getTokens().equals(other.getTokens())){
                        covered = true;
                        if(!document.getRelations().getOutgoingRelations(ann).isEmpty()) {
//                            System.out.println("Error");
                        }
                    }
                }
                if(!covered){
//                    System.out.println("[TRANS-4]: " + ann + " --type--> " + type);
                    ann.setType(type);
                }
            }
        }

        return document;
    }

    public Document refineDocument(Document document){

        // 1. Refine nested person names to one name
    Document document1 = refinePersonNamRelations(document);

        // 2. Refine explicitly defined channel overlaps
        Document document2 = refineExpliciteAnnotations2(document1);

        // 3. Refine overlaps for pairs of relational annotations
        Document document3 = refineRestOfAnnotations(document2);

        // 4. Change type of nonrelational non-overlapping annotations
        Document document4 = renameNonRelationalNonOverlapping(document3);

        // 5. Remove unused annotations such as nam_adj_country etc.
        List<Annotation> adjNames = document4.getAnnotations(Arrays.asList(new Pattern[]{Pattern.compile("nam_adj.*")}));
        document4.filterAnnotationClusters(adjNames);
        document4.removeAnnotations(adjNames);


        // 6. Apply preFilterSelector to remove annotations
        List<Annotation> annotationsToRemove = preFilterSelector.selectAnnotations(document4);
        document4.removeAnnotations(annotationsToRemove);
        document4.filterAnnotationClusters(annotationsToRemove);

        return document4;
    }

    /**
     * Definiuje mapowanie pomiędzy selektorem dla nierelacyjnych anotacji, które
     * mają być zastąpione przez relacyjne anotacje innego typu
     * @TODO: ładowanie z pliku konfiguracji
     * @return
     */
    public HashMap<AbstractAnnotationSelector, String> nonRelationalMapping(){
        HashMap<AbstractAnnotationSelector, String> transformations = new HashMap<>();

        transformations.put(AnnotationSelectorFactory.getFactory().getInitializedSelector("zero_mention_selector"), "anafora_verb_null");

        return transformations;
    }


    public static HashMap<AbstractAnnotationSelector, AbstractAnnotationSelector> getTransformations(){
        HashMap<AbstractAnnotationSelector, AbstractAnnotationSelector> transformations = new HashMap<>();


        // Usuń wszystkie anotacje nam_liv_person oraz nam_liv_god pokrywające się z nam_liv_person
        transformations.put(
           new PatternAnnotationSelector(new String[]{"nam_liv_person_.*", "nam_liv_god", "nam_fac_road"}),
           new PatternAnnotationSelector(new String[]{"nam_liv_person"})
        );

        transformations.put(
            new PatternAnnotationSelector(new String[]{"nam_loc_gpe_admin3"}),
            new PatternAnnotationSelector(new String[]{"nam_loc_gpe_city"})
        );

        transformations.put(
                new PatternAnnotationSelector(new String[]{"anafora_event"}),
                new PatternAnnotationSelector(new String[]{"anafora_wyznacznik", "anafora_verb_null", "anafora_verb_null_in"})
        );

        transformations.put(
                new PatternAnnotationSelector(new String[]{"anafora_wyznacznik"}),
                new PatternAnnotationSelector(new String[]{"anafora_verb_null", "anafora_verb_null_in"})
        );

        transformations.put(
                new PatternAnnotationSelector(new String[]{"nam.*"}),
                new PatternAnnotationSelector(new String[]{"anafora_wyznacznik"})
        );

        return transformations;
    }

    /**
     * Scalanie relacji dla anotacji typu *person_nam* z wewnętrznymi anotacjami
     * typu *person_nam_first*, *person_nam_last* etc. Dodatkowo opcja umożliwiająca
     * usunięcie wewnętrznych anotacji.
     */
    public Document refinePersonNamRelations(Document document){
        List<Pattern> personNam = Arrays.asList(new Pattern[]{Pattern.compile("nam_liv_person$"), Pattern.compile("person_nam")});
        List<Pattern> personLastFirstNam = Arrays.asList(new Pattern[]{
                Pattern.compile("nam_liv_person_add"), Pattern.compile("nam_liv_person_first"), Pattern.compile("nam_liv_person_last"),
                Pattern.compile("person_add_nam"), Pattern.compile("person_first_nam"), Pattern.compile("person_last_nam")
        });

        for(Sentence sentence : document.getSentences()){
            Set<Annotation> lastFirstNam = sentence.getAnnotations(personLastFirstNam);
            Set<Annotation> persNam = sentence.getAnnotations(personNam);

            List<Annotation> toRemove = new ArrayList<>();

            // Dla każdej anotacji bardziej granularnej od nam_liv_person (imię, nazwisko etc.)
            for(Annotation lfAnn : lastFirstNam){
                // Dla każdej anotacji nam_liv_person - nazwa całej osoby / cała nazwa osoby
                for(Annotation pAnn : persNam){
                    // Dla każdego tokenu w "mniejszej" anotacji
                    for(Integer tokenId : lfAnn.getTokens()){
                        // Sprawdź czy zawiera się w "większej"
                        if(pAnn.getTokens().contains(tokenId)){
                            // Przepnij relacje z mniejszej do większej
                            document.rewireSingleRelations(lfAnn, pAnn);
                            toRemove.add(lfAnn);
                            // Przestań sprawdzać kolejne tokeny - anotacje się krzyżują
                            break;
                        }
                    }
                }
            }
            document.removeAnnotations(toRemove);
        }

        return document;
    }

}
