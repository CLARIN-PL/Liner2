package g419.liner2.relations.evaluation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.RelationCluster;
import g419.corpus.structure.RelationCluster.ReturnRelationsToDistinctEntities;
import g419.corpus.structure.RelationCluster.ReturningStrategy;
import g419.corpus.structure.RelationClusterSet;
import g419.corpus.structure.TokenAttributeIndex;
import g419.liner2.api.tools.FscoreEvaluator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class ParentEvaluator extends FscoreEvaluator{
	
	//TODO: Kryterium porównywania wzmianek pomiędzy dokumentami
	// 
	
	public static interface RelationUnitCriterion {
		public boolean isSatisfied(Annotation annotation);
	}

	
	public static class NamedEntityCriterion implements RelationUnitCriterion{
		public static final String name = "NamedEntity";

		@Override
		public boolean isSatisfied(Annotation annotation) {
			return annotation.getType().endsWith("nam");
		}
		
		
	}
	
	public static class PronounAndZeroCriterion implements RelationUnitCriterion{
		public static final String name = "PronZero";

		@Override
		public boolean isSatisfied(Annotation annotation) {
			TokenAttributeIndex ai = annotation.getSentence().getAttributeIndex();
			String headPos = ai.getAttributeValue(annotation.getSentence().getTokens().get(annotation.getHead()), "posext");
			
			return "pron".equals(headPos) || "verb".equals(headPos);
		}
	}
	
	public static class AgpPronounAndZeroCriterion implements RelationUnitCriterion{
		public static final String name = "AgPPronZero";

		@Override
		public boolean isSatisfied(Annotation annotation) {
			return "chunk_agp".equals(annotation.getType());
		}
	}
	
	////
	
	public static class AnnotationMapper{
		Comparator<Annotation> comparator;
		public AnnotationMapper(Comparator<Annotation> comparator){
			this.comparator = comparator;
		}
		
		/*
		 * Zwraca mapowanie z anotacji dokumentu systemowego na anotacje w dokumencie referencyjnym
		 */
		private HashMap<Annotation, Annotation> createMapping(Document referenceDocument, Document systemDocument){
			HashMap<Annotation, Annotation> mapping = new HashMap<Annotation, Annotation>();
			
			for(Annotation sysAnnotation: systemDocument.getAnnotations()){
				for(Annotation refAnnotation: referenceDocument.getAnnotations()){
					if(comparator.compare(refAnnotation, sysAnnotation) == 0){
						mapping.put(sysAnnotation, refAnnotation);
						break;
					}
				}
			}
			
			return mapping;
		}
	}
	
	////
	
	private RelationUnitCriterion identifyingUnitsCriteria;
	private RelationUnitCriterion referencingUnitsCriteria;
	private Comparator<Annotation> matcher;
	private AnnotationMapper mapper;
	
	public ParentEvaluator(RelationUnitCriterion identifyingUnitsCriteria, RelationUnitCriterion referencingUnitsCriteria, Comparator<Annotation> annotationMatcher){
		super();
		this.identifyingUnitsCriteria = identifyingUnitsCriteria;
		this.referencingUnitsCriteria = referencingUnitsCriteria;
		this.matcher = annotationMatcher;
		this.mapper = new AnnotationMapper(annotationMatcher);
	}
	
	public Set<Annotation> extractUnits(Document document, RelationUnitCriterion criterion){
		Set<Annotation> units = new HashSet<Annotation>();
		for(Annotation annotation: document.getAnnotations())
			if(criterion.isSatisfied(annotation)) units.add(annotation);
		
		return units;
	}
	
	public Set<Annotation> extractIdentifyingUnits(Document document){
		return extractUnits(document, this.identifyingUnitsCriteria);
	}
	
	public Set<Annotation> extractReferencingUnits(Document document){
		return extractUnits(document, this.referencingUnitsCriteria);
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
	public ArrayList<Annotation> getTwinlessMentions(Document document, Collection<Annotation> parallelAnnotations){
		ArrayList<Annotation> result = new ArrayList<Annotation>();
		ArrayList<Annotation> documentAnnotations = document.getAnnotations();
		
		for(Annotation documentAnnotation: documentAnnotations)
			if(!parallelAnnotations.contains(documentAnnotation))
				result.add(documentAnnotation);
		
		return result;
	}
	
	/**
	 * Dodaje wzmianki występujące tylko w jednym dokumencie jako singletony w drugim
	 * @param document Dokument, do którego są dodawane wzmianki
	 * @param twinlessAnnotations Wzmianki, które należy dodać (występujące w drugim dokumencie)
	 */
	private void addTwinlessMentionsAsSingletons(Document document, ArrayList<Annotation> twinlessAnnotations){
		// TODO: przemyśleć strukturę Sentence-Annotation!!!
		for(Annotation twinlessAnnotation: twinlessAnnotations){
			// TODO: dokończyć
		}
	}
	
	/**
	 * 
	 * @param systemUnits
	 * @param referenceUnits
	 * @param mapping
	 * @return
	 */
	private HashMap<Annotation, Annotation> createAnnotationMapping(Set<Annotation> systemUnits, Set<Annotation>referenceUnits, HashMap<Annotation, Annotation> mapping){
		return null;
	}
	
	/**
	 * Odwraca mapę - zmienia pary (klucz, wartość) na pary (wartość, klucz)
	 * @param mapping
	 * @return
	 */
	private HashMap<Integer, Annotation> reverseMapping(HashMap<Annotation, Integer> mapping){
		HashMap<Integer, Annotation> reversedMapping = new HashMap<Integer, Annotation>();
		for(Entry<Annotation, Integer> entry: mapping.entrySet())
			reversedMapping.put(entry.getValue(), entry.getKey());
		
		return reversedMapping;
	}
	
	/**
	 * 
	 * @param identifyingUnits
	 * @param document
	 * @return
	 */
	private Set<RelationCluster> clusterIdentifyingUnits(Set<Annotation> identifyingUnits, Document document){
		RelationClusterSet documentRelationClusters = RelationClusterSet.fromRelationSet(document.getRelations());
		return documentRelationClusters.getClustersWithAnnotations(identifyingUnits);
	}
	
	/**
	 * 
	 * @param referenceIdentifyingUnitsClusters
	 * @return
	 */
	private HashMap<Annotation, Integer> createDiscourseEntityMappingForIdentifyingUnits(Set<RelationCluster> referenceIdentifyingUnitsClusters){
		HashMap<Annotation, Integer> discourseEntityMapping = new HashMap<Annotation, Integer>();
		int discourseEntityIndex = 0;
		for(RelationCluster identifyingUnitCluster: referenceIdentifyingUnitsClusters){
			discourseEntityIndex++;
			for(Annotation identifyingUnitAnnotation: identifyingUnitCluster.getAnnotations())
				discourseEntityMapping.put(identifyingUnitAnnotation, discourseEntityIndex);
		}
		
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
		int currentEntityId = Collections.max(referenceEntityMapping.values());
		
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
	 * 
	 * @param initialRelationClusterSet
	 * @param entities
	 * @param mentions
	 * @param discourseEntityMapping
	 * @return
	 */
	private Set<Relation> extractDiscourseEntityRelations(RelationClusterSet initialRelationClusterSet, Set<Annotation> entities, Set<Annotation> mentions, HashMap<Annotation, Integer> discourseEntityMapping){
		ReturningStrategy strategy = new ReturnRelationsToDistinctEntities(entities, mentions, discourseEntityMapping);
		return initialRelationClusterSet.getRelationSet(strategy).getRelations();
	}
	
	/**
	 * 
	 * @param systemRelations
	 * @param referenceRelations
	 */
	private void calculateScore(Set<Relation> systemRelations, Set<Relation> referenceRelations, HashMap<Annotation, Annotation> systemToReferenceMapping){
		int localTruePositives = 0;
		int localFalsePositives = 0;
		int localFalseNegatives = 0;
		for(Relation systemRelation : systemRelations){
			Annotation systemFrom = systemToReferenceMapping.get(systemRelation.getAnnotationFrom());
			Annotation systemTo = systemToReferenceMapping.get(systemRelation.getAnnotationTo());
			
			if(systemFrom == null || systemTo == null){
				// Not found
				localFalsePositives++;
				continue;
			}
			
			for(Relation referenceRelation : referenceRelations){
				if(referenceRelation.getAnnotationFrom().equals(systemFrom) && referenceRelation.getAnnotationTo().equals(systemTo)){
					// Found
					localTruePositives++;
					break;
				}
			}
			// Not found
			localFalsePositives++;
		}
		
		localFalseNegatives = referenceRelations.size() - truePositives;
		
		this.truePositives = localTruePositives;
		this.falsePositives = localFalsePositives;
		this.falseNegatives = localFalseNegatives;
		
		float localPrecision = safeDiv(localTruePositives, localTruePositives + localFalsePositives, 0.0f);
		float localRecall = safeDiv(localTruePositives, localTruePositives + localFalseNegatives, 0.0f);
		float localF = safeDiv(2 * localPrecision * localRecall, localPrecision + localRecall, 0.0f);
		
		System.out.println(String.format("Score for document: Precision=%.2f%, Recall=%.2f%, F=%.2f%", localPrecision*100, localRecall*100, localF*100));
	}
	
	
	/**
	 * 
	 * @param systemResult
	 * @param referenceDocument
	 */
	public void evaluate(Document systemResult, Document referenceDocument){
		
		// TODO: parallelization of mentions
		
		// Prepare documents
		// 1. Add twinless system mentions to reference
		// 2. Add twinless reference mentions to system
		
		HashMap<Annotation, Annotation> systemToReferenceMapping = mapper.createMapping(referenceDocument, systemResult);
		
		//TODO: enable twinless completion (jeśli to coś zmieni w ocenie!!!)
//		ArrayList<Annotation> twinlessReference = getTwinlessMentions(referenceDocument, systemToReferenceMapping.values());
//		ArrayList<Annotation> twinlessSystem = getTwinlessMentions(systemResult, systemToReferenceMapping.keySet());
		
//		addTwinlessMentionsAsSingletons(referenceDocument, twinlessSystem);
//		addTwinlessMentionsAsSingletons(systemResult, twinlessReference);
		
		// 1. Divide mentions into 3 classes:
		// 	*Idendifying units* 
		//  *Referencing units*
		//	*Ignored units*
		
		Set<Annotation> referenceIdentifyingUnits = extractIdentifyingUnits(referenceDocument);
		Set<Annotation> referenceReferencingUnits = extractReferencingUnits(referenceDocument);
//		Set<Annotation> referenceIgnoredUnits = extractIgnoredUnits(referenceDocument, referenceIdentifyingUnits, referenceReferencingUnits);
		
		Set<Annotation> systemIdentifyingUnits = extractIdentifyingUnits(referenceDocument);
		Set<Annotation> systemReferencingUnits = extractReferencingUnits(referenceDocument);
//		Set<Annotation> systemIgnoredUnits = extractIgnoredUnits(referenceDocument, systemIdentifyingUnits, systemReferencingUnits);
		
		// X. Create mapping from system referencing units to reference referencing units
//		HashMap<Annotation, Annotation> referencingUnitsMapping = createAnnotationMapping(systemReferencingUnits, referenceReferencingUnits,  systemToReferenceMapping);
		
		// X. Create reference clustering of identifying units
		Set<RelationCluster> referenceIdentifyingUnitsClusters = clusterIdentifyingUnits(referenceIdentifyingUnits, referenceDocument);
		
		// X. Create reference mapping from identifying units to discourse entities
		HashMap<Annotation, Integer> discourseEntityMapping = createDiscourseEntityMappingForIdentifyingUnits(referenceIdentifyingUnitsClusters);
//		HashMap<Integer, Annotation> discourseEntityReverseMapping = reverseMapping(discourseEntityMapping);

		// X. Create system mapping from identifying units to discourse entites according to above
		HashMap<Annotation, Integer> systemDiscourseEntityMapping = createDiscourseEntityMappingForSystemResponse(discourseEntityMapping, systemToReferenceMapping, systemIdentifyingUnits);
		
		// X. Create reference clustering
		RelationClusterSet referenceRelations = RelationClusterSet.fromRelationSet(referenceDocument.getRelations());
		
		// X. Create reference relation set from referencing units to identifying units (discourse entities)
		Set<Relation> referenceDiscourseEntitiesRelations = extractDiscourseEntityRelations(referenceRelations, referenceIdentifyingUnits, referenceReferencingUnits, discourseEntityMapping);
		
		// X. Create system result clustering
		RelationClusterSet systemResultRelations = RelationClusterSet.fromRelationSet(systemResult.getRelations());
		
		// X. Create system relation set from referencing units to identifying units
		Set<Relation> systemDiscourseEntitiesRelations = extractDiscourseEntityRelations(systemResultRelations, systemIdentifyingUnits, systemReferencingUnits, systemDiscourseEntityMapping);
		
		// X. Compare relations
		
		calculateScore(referenceDiscourseEntitiesRelations, systemDiscourseEntitiesRelations, systemToReferenceMapping);
		
		
		
	}
	
}
