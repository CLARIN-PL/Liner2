package g419.crete.api.evaluation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationCluster.ReturnRelationsToDistinctEntities;
import g419.corpus.structure.AnnotationCluster.ReturningStrategy;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.liner2.api.tools.FscoreEvaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Ewaluator metryki PARENT dla koreferencji
 * 
 * @author Adam Kaczmarek
 * TODO:
 * 1. Parametryzacja kryterium porównywania wzmianek pomiędzy dokumentami: co najmniej dwie metody:
 * - Dokładna zgodność
 * - Zgodność głów (+heurystyka przypisania głowy
 * 2. Zmiana RelationUnitCriterion na AnnotationSelector konfigurowalny w modelu podawanym na wejście
 */


public class ParentEvaluator extends FscoreEvaluator{
	
	List<Pattern> personLastFirstNam;
	List<Pattern> personNam;
	
	public static interface RelationUnitCriterion {
		public boolean isSatisfied(Annotation annotation);
	}

	
	public static class NamedEntityCriterion implements RelationUnitCriterion{
		public static final String name = "NamedEntity";

		@Override
		public boolean isSatisfied(Annotation annotation) {
			return annotation.getType().startsWith("nam") || annotation.getType().endsWith("nam");
		}
		
		
	}
	
	public static class NonZeroCriterion implements RelationUnitCriterion{
		public static final String name = "NonZero";

		@Override
		public boolean isSatisfied(Annotation annotation) {
			if(!"anafora_wyznacznik".equalsIgnoreCase(annotation.getType())) return false; // Tylko anafora_wyznacznik (bez *_nam)
			if(annotation.getTokens().size() > 1) return true; // Zał.: tylko AgP mają więcej niż 1 token
			TokenAttributeIndex ai = annotation.getSentence().getAttributeIndex();
			int headIndex = annotation.getTokens().first();
			String headPos = "";
			try{
				headPos = ai.getAttributeValue(annotation.getSentence().getTokens().get(headIndex), "pos");
			}
			catch(IndexOutOfBoundsException ex){
				headPos = ai.getAttributeValue(annotation.getSentence().getTokens().get(headIndex), "tagTool");
			}
			
			
			return !"verb".equals(headPos); //&& !"fin".equals(headPos) && !"praet".equals(headPos) && !"winien".equals(headPos) && !"bedzie".equals(headPos);
		}
	}
	
	public static class PronounAndZeroCriterion implements RelationUnitCriterion{
		public static final String name = "PronZero";

		@Override
		public boolean isSatisfied(Annotation annotation) {
			TokenAttributeIndex ai = annotation.getSentence().getAttributeIndex();
			//TODO: uwagaa na getHead() -> może zwracać 0 co powoduje błędy
			String headPos = ai.getAttributeValue(annotation.getSentence().getTokens().get(annotation.getHead()), "pos");
			
			return "pron".equals(headPos) || "verb".equals(headPos);
		}
	}
	
	public static class AgpPronounAndZeroCriterion implements RelationUnitCriterion{
		public static final String name = "AgPPronZero";

		@Override
		public boolean isSatisfied(Annotation annotation) {
			return "anafora_wyznacznik".equals(annotation.getType()) || "anafora_verb_null".equalsIgnoreCase(annotation.getType()) || "anafora_verb_null_in".equalsIgnoreCase(annotation.getType());
		}
	}
	
	public static class ZeroCriterion implements RelationUnitCriterion{
		public static final String name = "Zero";
		public static final NonZeroCriterion nonZero = new NonZeroCriterion();
		
		@Override
		public boolean isSatisfied(Annotation annotation) {
			if("anafora_verb_null".equalsIgnoreCase(annotation.getType())) return true;
			if("anafora_verb_null_in".equalsIgnoreCase(annotation.getType())) return true;
			if(Pattern.matches("anafora_verb_null.*", annotation.getType())) return true;
			
			
			if(!"anafora_wyznacznik".equalsIgnoreCase(annotation.getType())) return false; // Tylko anafora_wyznacznik (bez *_nam)
			if(annotation.getTokens().size() > 1) return false; // Zał.: tylko AgP mają więcej niż 1 token
			TokenAttributeIndex ai = annotation.getSentence().getAttributeIndex();
			int headIndex = annotation.getTokens().first();
			String headPos = "";
			try{
				headPos = ai.getAttributeValue(annotation.getSentence().getTokens().get(headIndex), "pos");
			}
			catch(IndexOutOfBoundsException ex){
				headPos = ai.getAttributeValue(annotation.getSentence().getTokens().get(headIndex), "tagTool");
			}
			
			
			return "verb".equals(headPos);
//			return false;
		}
			
		
	}
	
	public static class AnnotationMapper{
		Comparator<Annotation> comparator;
		List<Pattern> annotationTypes;
		boolean teiRemap;
		
		public AnnotationMapper(Comparator<Annotation> comparator, List<Pattern> annotationTypes){
			this.annotationTypes = annotationTypes;
			this.comparator = comparator;
			this.teiRemap = true;
		}
		
		
		/*
		 * Zwraca mapowanie z anotacji dokumentu systemowego na anotacje w dokumencie referencyjnym
		 */
		private HashMap<Annotation, Annotation> createMapping(Document referenceDocument, Document systemDocument){
			HashMap<Annotation, Annotation> mapping = new HashMap<Annotation, Annotation>();
			
			for(Annotation sysAnnotation: systemDocument.getAnnotations(annotationTypes)){
				for(Annotation refAnnotation: referenceDocument.getAnnotations(annotationTypes)){
					if(comparator.compare(refAnnotation, sysAnnotation) == 0){
						// Przeniesione porównanie zdań
						if(systemDocument.getSentences().indexOf(sysAnnotation.getSentence()) == referenceDocument.getSentences().indexOf(refAnnotation.getSentence())){
							if(this.teiRemap && refAnnotation.getType().endsWith("nam")){
								sysAnnotation.setType(refAnnotation.getType());
							}
							mapping.put(sysAnnotation, refAnnotation);
							break;
						}
					}
				}
			}
			
			return mapping;
		}
	}
	
	////
	
//	private RelationUnitCriterion identifyingUnitsCriteria;
//	private RelationUnitCriterion referencingUnitsCriteria;

	private AbstractAnnotationSelector identifyingSelector;
	private AbstractAnnotationSelector referencingSelector;
	private AnnotationMapper mapper;
	
	public ParentEvaluator(AbstractAnnotationSelector identifyingSelector, AbstractAnnotationSelector referencingSelector, Comparator<Annotation> annotationMatcher){
		super();

		this.identifyingSelector = identifyingSelector;
		this.referencingSelector = referencingSelector;

		// TODO: refactor
		ArrayList<Pattern> annotationTypes = new ArrayList<Pattern>();
		annotationTypes.add(Pattern.compile("nam.*"));
		annotationTypes.add(Pattern.compile(".*nam"));
		annotationTypes.add(Pattern.compile("anafora_wyznacznik"));
		annotationTypes.add(Pattern.compile("anafora_verb_null.*"));
		annotationTypes.add(Pattern.compile("mention"));
		this.mapper = new AnnotationMapper(annotationMatcher, annotationTypes);
		

	}

	public Set<Annotation> extractIgnoredUnits(Document document, Set<Annotation> identifyingUnits, Set<Annotation> referencingUnits){
		Set<Annotation> ignoredUnits = new HashSet<Annotation>();
		for(Annotation annotation: document.getAnnotations())
			if(!identifyingUnits.contains(ignoredUnits) && !referencingUnits.contains(annotation)) ignoredUnits.add(annotation);
		
		return ignoredUnits;
	}
	
	/**
	 * Znajduje wzmianki, które występują tylko w jednym dokumencie z dwóch używanych do ewaluacji 
	 * @param document Dokument, dla którego wyszukiwane są wzmianki
	 * @param parallelAnnotations Kolekcja anotacji, które występują w obydwu dokumentach
	 * @return Wzmianki występujące tylko w podanym dokumencie
	 */
//	public ArrayList<Annotation> getTwinlessMentions(Document document, Collection<Annotation> parallelAnnotations){
//		ArrayList<Annotation> result = new ArrayList<Annotation>();
//		ArrayList<Annotation> documentAnnotations = document.getAnnotations();
//
//		for(Annotation documentAnnotation: documentAnnotations)
//			if(!parallelAnnotations.contains(documentAnnotation))
//				result.add(documentAnnotation);
//
//		return result;
//	}
	
	/**
	 * 
	 * @param identifyingUnits
	 * @param document
	 * @return
	 */
	private Set<AnnotationCluster> clusterIdentifyingUnits(Set<Annotation> identifyingUnits, Document document){
		AnnotationClusterSet documentRelationClusters = AnnotationClusterSet.fromRelationSet(document.getRelations(Relation.COREFERENCE));
		return documentRelationClusters.getClustersWithAnnotations(identifyingUnits);
	}
	
	/**
	 * 
	 * @param referenceIdentifyingUnitsClusters
	 * @return
	 */
	private HashMap<Annotation, Integer> createDiscourseEntityMappingForIdentifyingUnits(Set<AnnotationCluster> referenceIdentifyingUnitsClusters, Set<Annotation> referenceIdentifyingUnits){
		HashMap<Annotation, Integer> discourseEntityMapping = new HashMap<Annotation, Integer>();
		int discourseEntityIndex = 0;
		
		// Encje dla klastrów C t.że |C| >= 2
		for(AnnotationCluster identifyingUnitCluster: referenceIdentifyingUnitsClusters){
			if(identifyingUnitCluster == null) continue;
//			System.out.println(identifyingUnitCluster);
			discourseEntityIndex++;
			for(Annotation identifyingUnitAnnotation: identifyingUnitCluster.getAnnotations()){
				if(referenceIdentifyingUnits.contains(identifyingUnitAnnotation)){
					discourseEntityMapping.put(identifyingUnitAnnotation, discourseEntityIndex);
				}
			}
		}
		
		// Uzupełnij o encje dla klastrów singletonowych
		for(Annotation annotation : referenceIdentifyingUnits)
			if(discourseEntityMapping.get(annotation) == null)
				discourseEntityMapping.put(annotation, ++discourseEntityIndex);
		
		return discourseEntityMapping;
	}
	
	/**
	 * 
	 * @param referenceEntityMapping
	 * @param annotationSystemToReferenceMapping
	 * @param systemIdentifyingMentions
	 * @return
	 */
	private HashMap<Annotation, Integer> createDiscourseEntityMappingForSystemResponse(HashMap<Annotation, Integer> referenceEntityMapping, HashMap<Annotation, Annotation> annotationSystemToReferenceMapping, Set<Annotation> systemIdentifyingMentions){
		HashMap<Annotation, Integer> systemResponseDiscourseEntityMapping = new HashMap<Annotation, Integer>();
		int currentEntityId = 0;
		if(referenceEntityMapping.values() != null && referenceEntityMapping.values().size() > 0) currentEntityId = Collections.max(referenceEntityMapping.values());
		
		
		for(Annotation annotation: systemIdentifyingMentions){
			if(annotationSystemToReferenceMapping.containsKey(annotation)){
				systemResponseDiscourseEntityMapping.put(annotation, referenceEntityMapping.get(annotationSystemToReferenceMapping.get(annotation)));
			}
			else{
				systemResponseDiscourseEntityMapping.put(annotation, ++currentEntityId);
			}
		}

		return systemResponseDiscourseEntityMapping;
	}


	/**
	 * Extracting units of given type from system document given units from referencing document.
	 *
	 * Identifying units in system document is a sum of intersection of referencing and system documents' identifying units and twinless system identifying units
	 * Referencing units in system document is a sum of intersection of referencing and system documents' referencing units and twinless system referencing units
	 *
	 * That is mentions which are in reference document and are marked as non-identifying cannot be marked as identifying in system response and the same holds for referencing mentions
	 *
	 * @param systemDocumentUnits Mentions of given type from system document
	 * @param referenceDocumentUnits Mentions of given type from reference document
	 * @param systemToReferenceMapping Mapping of mentions from system document to reference document
	 * @return set of mentions that belong to given type consistently with the type annotation for reference document
	 */
	private Set<Annotation> extractUnits(Set<Annotation> systemDocumentUnits, Set<Annotation> referenceDocumentUnits, HashMap<Annotation, Annotation> systemToReferenceMapping){
		HashSet<Annotation> unitsSet = new HashSet<>();

		// Wzmianki danego rodzaju w dokumencie systemowym, które nie występują w dokumencie referencyjnym - twinless mentions
		Set<Annotation> systemTwinlessUnits = systemDocumentUnits.stream().filter(annotation -> !systemToReferenceMapping.containsKey(annotation)).collect(Collectors.toSet());

		// Zmapowane wzmianki systemowe danego rodzaju, które nie są twinless mentions
		Set<Annotation> systemNonTwinlessMapped = systemDocumentUnits.stream()
				.filter(annotation -> systemToReferenceMapping.containsKey(annotation))
				.map(annotation -> systemToReferenceMapping.get(annotation))
				.collect(Collectors.toSet());

		// Wzmianki danego rodzaju w dokumencie systemowym, które są tego samego rodzaju w dokumencie wzorcowym
		systemNonTwinlessMapped.retainAll(referenceDocumentUnits);

		// Dodaj oba zbiory
		unitsSet.addAll(systemTwinlessUnits);
		unitsSet.addAll(systemNonTwinlessMapped);

		return unitsSet;
	}


	/**
	 * 
	 * @param initialRelationClusterSet
	 * @param entities
	 * @param mentions
	 * @param discourseEntityMapping
	 * @return
	 */
	private Set<Relation> extractDiscourseEntityRelations(AnnotationClusterSet initialRelationClusterSet, Set<Annotation> entities, Set<Annotation> mentions, HashMap<Annotation, Integer> discourseEntityMapping){
		ReturningStrategy strategy = new ReturnRelationsToDistinctEntities(entities, mentions, discourseEntityMapping);
		return initialRelationClusterSet.getRelationSet(strategy).getRelations();
	}
	
	
	/**
	 * 
	 * @param systemRelations
	 * @param referenceRelations
	 */
	private void calculateScore(String docId, Set<Relation> systemRelations, Set<Relation> referenceRelations, HashMap<Annotation, Annotation> systemToReferenceMapping, HashMap<Annotation, Integer> discourseEntityMapping, HashMap<Annotation, Integer> systemDiscourseEntityMapping){
		int localTruePositives = 0;
		int localFalsePositives = 0;
		int localFalseNegatives = 0;
		boolean found = false;
		for(Relation systemRelation : systemRelations){
			Annotation systemFrom = systemToReferenceMapping.get(systemRelation.getAnnotationFrom());
			Annotation systemTo = systemToReferenceMapping.get(systemRelation.getAnnotationTo());
			
			if(systemFrom == null || systemTo == null){
				// Not found
				localFalsePositives++;
				continue;
			}
			found = false;
			
			Integer systemToEntityId = systemDiscourseEntityMapping.get(systemRelation.getAnnotationTo());

			for(Relation referenceRelation : referenceRelations){
				Integer referenceToEntityId = discourseEntityMapping.get(referenceRelation.getAnnotationTo());
				if(referenceRelation.getAnnotationFrom().equals(systemFrom) && systemToEntityId == referenceToEntityId){
					// Found
					localTruePositives++;
					found = true;
//					System.out.println(systemRelation);
					break;
				}
			}
			
			// Not found
			if(!found) localFalsePositives++;
		}
		
		localFalseNegatives = referenceRelations.size() - localTruePositives;
		
		this.truePositives += localTruePositives;
		this.falsePositives += localFalsePositives;
		this.falseNegatives += localFalseNegatives;
		
		float localPrecision = safeDiv(localTruePositives, localTruePositives + localFalsePositives, 0.0f);
		float localRecall = safeDiv(localTruePositives, localTruePositives + localFalseNegatives, 0.0f);
		float localF = safeDiv(2 * localPrecision * localRecall, localPrecision + localRecall, 0.0f);
		
		System.out.println(String.format("Score for document " + docId + ": \tPrecision = %.2f \t (%d/%d),  \t Recall=%.2f \t  (%d/%d), \t F=%.2f", localPrecision*100, localTruePositives, localTruePositives+localFalsePositives, localRecall*100, localTruePositives, localTruePositives + localFalseNegatives, localF*100));
	}
	
	/**
	 * 
	 * @param systemResult
	 * @param referenceDocument
	 */
	public void evaluate(Document systemResult, Document referenceDocument){

		
		/**
		 *  Dodawanie koreferencji pomiędzy częściami i całościami nazw osób
		 *  - dla zagnieżdżonych person_first_nam w person_nam
		 *  - dla zagnieżdżonych person_last_nam w person_nam
		 */
		
//		completePersonNamRelations(referenceDocument);
		
		/**
		 *  TODO: parallelization of mentions
		 *  Obecnie: Uprzednie przygotowanie dokumentów
		 *  
		 *  Problemy:
		 *  - Kryterium wzmianki identyfikacyjnej i referencyjnej dla oceny dokumentów tei(system) dla korpusu ccl(reference)
		 */
		
		HashMap<Annotation, Annotation> systemToReferenceMapping = mapper.createMapping(referenceDocument, systemResult);
		
		/**
		 * Dodawanie wzmianek twinless nie zmienia niczego w ocenie:
		 * 1. Dodanie wzmianki referencyjnej w odpowiedzi ocenianego systemu
		 * - Zmniejszenie precyzji - nadmiarowa relacja (tak jak po dodaniu twinless)
		 * 2. Pominięcie wzmianki referencyjnej w odpowiedzi ocenianego systemu
		 * - Zmniejszenie kompletności - pominięcie relacji (tak jak po dodaniu twinless)
		 * 3. Dodanie wzmianki identyfikacyjnej w odpowiedzi ocenianego systemu
		 * - Zmniejszenie precyzji - nadmiarowa relacja (tak jak po dodaniu twinless)
		 * 4. Pominięcie wzmianki identyfikacyjnej w odpowiedzi ocenianego systemu
		 * - Zmniejszenie kompletności, tylko w przypadku pominięcia wszystkich wzmianek (twinless mogłoby działać niezgodnie z zamierzeniem) (?)
		 */
		
		// 1. Divide mentions into 3 classes:
		// 	*Idendifying units* 
		//  *Referencing units*
		//	*Ignored units* - not needed
		
		Set<Annotation> referenceIdentifyingUnits = new HashSet<>(identifyingSelector.selectAnnotations(referenceDocument));
		Set<Annotation> referenceReferencingUnits = new HashSet<>(referencingSelector.selectAnnotations(referenceDocument));
		
		Set<Annotation> systemIdentifyingUnits = extractUnits(new HashSet<>(identifyingSelector.selectAnnotations(systemResult)), referenceIdentifyingUnits, systemToReferenceMapping);
		Set<Annotation> systemReferencingUnits = extractUnits(new HashSet<>(referencingSelector.selectAnnotations(systemResult)), referenceReferencingUnits, systemToReferenceMapping);

		// 2. Create reference clustering of identifying units
		Set<AnnotationCluster> referenceIdentifyingUnitsClusters = clusterIdentifyingUnits(referenceIdentifyingUnits, referenceDocument);
		
		// 3. Create reference mapping from identifying units to discourse entities
		HashMap<Annotation, Integer> discourseEntityMapping = createDiscourseEntityMappingForIdentifyingUnits(referenceIdentifyingUnitsClusters, referenceIdentifyingUnits);

		// 4. Create system mapping from identifying units to discourse entites according to above
		HashMap<Annotation, Integer> systemDiscourseEntityMapping = createDiscourseEntityMappingForSystemResponse(discourseEntityMapping, systemToReferenceMapping, systemIdentifyingUnits);
		
		// 5. Create reference clustering
		AnnotationClusterSet referenceRelations = AnnotationClusterSet.fromRelationSet(referenceDocument.getRelations(Relation.COREFERENCE));
//		System.out.println(referenceRelations);
		// 6. Create reference relation set from referencing units to identifying units (discourse entities)
		Set<Relation> referenceDiscourseEntitiesRelations = extractDiscourseEntityRelations(referenceRelations, referenceIdentifyingUnits, referenceReferencingUnits, discourseEntityMapping);
		
		// 7. Create system result clustering
		AnnotationClusterSet systemResultRelations = AnnotationClusterSet.fromRelationSet(systemResult.getRelations()); //Relation.COREFERENCE --ikar ustawia domyślnie pusty atrybut set=""
//		System.out.println(systemResultRelations);
		// 8. Create system relation set from referencing units to identifying units
		Set<Relation> systemDiscourseEntitiesRelations = extractDiscourseEntityRelations(systemResultRelations, systemIdentifyingUnits, systemReferencingUnits, systemDiscourseEntityMapping);
		
		// 9. Compare relations
		calculateScore(systemResult.getName(), systemDiscourseEntitiesRelations, referenceDiscourseEntitiesRelations, systemToReferenceMapping, discourseEntityMapping, systemDiscourseEntityMapping);
		
		
	}
	
}
