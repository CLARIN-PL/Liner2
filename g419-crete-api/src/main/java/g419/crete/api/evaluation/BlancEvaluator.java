package g419.crete.api.evaluation;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Document;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.annotation.AnnotationSelectorFactory;
import g419.liner2.api.tools.FscoreEvaluator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

public class BlancEvaluator extends FscoreEvaluator{

	protected final ParentEvaluator.AnnotationMapper mapper;

	private final String printFormat = "%s:\t Recall: (%s/%s) %.2f%%\t Precision: (%s/%s) %.2f%%\t F1: %.2f%%";

	public BlancEvaluator(ParentEvaluator.AnnotationMapper mapper){
		this.mapper = mapper;
	}

//	public int combinations2(int n){
//		return ((n-1) * n) >> 1;
//	}

	public void evaluate(Document systemResult, Document referenceDocument){

		int truePositive = 0;
		int falsePositive = 0;
		int trueNegative = 0;
		int falseNegative = 0;

		AbstractAnnotationSelector namedEntitySelector = AnnotationSelectorFactory.getFactory().getInitializedSelector("named_entity_selector");
		AnnotationClusterSet goldClusterSet = AnnotationClusterSet.fromRelationSet(referenceDocument.getRelations(), namedEntitySelector.selectAnnotations(referenceDocument));
		AnnotationClusterSet sysClusterSet = AnnotationClusterSet.fromRelationSet(systemResult.getRelations(), namedEntitySelector.selectAnnotations(systemResult));

		HashMap<Annotation, Annotation> mapping = mapper.createMapping(referenceDocument, systemResult);
//		System.out.println(mapping.keySet());
//		System.out.println(mapping.values());
//		System.out.println(mapping);
//		System.out.println(sysClusterSet);
//		System.out.println(systemResult.getRelations());
		// Zlicza każdy link dwa razy zależnie od wyjściowej anotacji

		for(AnnotationCluster mentionSysCluster : sysClusterSet.getClusters()) {
//			System.out.println("MSCLUSTER");
			for (Annotation mention : mentionSysCluster.getAnnotations()) {
//				System.out.println("MS_MENTION");
				Annotation goldMention = mapping.get(mention);
				if(goldMention == null) continue;
//				System.out.println("MS_MENTION_CONTD");
				AnnotationCluster mentionGoldCluster = goldClusterSet.getClusterWithAnnotation(goldMention);


				// Anotacje takie same w obydwu klastrach
				Set<Annotation> truePositiveAnnotations = mentionSysCluster.getAnnotations()
						.parallelStream()
						.filter(m -> !mention.equals(m))
						.filter(m -> mapping.get(m) != null)
						.filter(m -> mentionGoldCluster.getAnnotations().contains(mapping.get(m)))
						.collect(Collectors.toCollection(HashSet::new));
				// Ilość poprawnie odnalezionych linków dla bieżącej anotacji
				int truePositiveLinks = truePositiveAnnotations.size();

				// Anotacje w klastrze systemowym, których nie ma w klastrze wzorcowym
				Set<Annotation> falsePositiveAnnotations = mentionSysCluster.getAnnotations()
						.parallelStream()
						.filter(m -> !mention.equals(m))
						.filter(m -> !truePositiveAnnotations.contains(m))
						.collect(Collectors.toCollection(HashSet::new));
				// Ilość niepoprawnych odnalezionych linków dla bieżącej anotacji
				int falsePositiveLinks = falsePositiveAnnotations.size();

				// Anotacje w klastrze wzorcowym, których nie ma w klastrze systemowym
				Set<Annotation> falseNegativeAnnotations = systemResult.getAnnotations()
						.parallelStream()
						.filter(m -> !mention.equals(m))
						.filter(m -> mapping.get(m) != null)
						.filter(m -> !mentionSysCluster.getAnnotations().contains(m))
						.filter(m -> mentionGoldCluster.getAnnotations().contains(mapping.get(m)))
						.collect(Collectors.toCollection(HashSet::new));
				// Ilość nieodnalezionych poprawnych linków dla bieżącej anotacji
				int falseNegativeLinks = falseNegativeAnnotations.size();

				// Ilość nieodnalezionych niepoprawnych linków dla bieżącej anotacji
				int trueNegativeLinks = referenceDocument.getAnnotations().size() - 1 - truePositiveLinks - falsePositiveLinks - falseNegativeLinks;

				// 5(6){1} + 5(6){1} + 4(5){2}

//				System.out.println(truePositiveLinks);
//				System.out.println(trueNegativeLinks);
//				System.out.println(falsePositiveLinks);
//				System.out.println(falseNegativeLinks);
				// Increase global values
				truePositive += truePositiveLinks;
				falsePositive += falsePositiveLinks;
				trueNegative += trueNegativeLinks;
				falseNegative += falseNegativeLinks;

			}
		}

		System.out.println(truePositive);
		System.out.println(trueNegative);
		System.out.println(falsePositive);
		System.out.println(falseNegative);
		truePositive >>= 1;
		trueNegative >>= 1;
		falsePositive >>= 1;
		falseNegative >>= 1;
		System.out.println(truePositive);
		System.out.println(trueNegative);
		System.out.println(falsePositive);
		System.out.println(falseNegative);


		this.truePositives += truePositive;
		this.trueNegatives += trueNegative;
		this.falsePositives += falsePositive;
		this.falseNegatives += falseNegative;

		float precisionCoref = safeDiv(truePositive, truePositive + falsePositive, 0.0f);
		float precisionNonCoref = safeDiv(trueNegative, trueNegative + falseNegative, 0.0f);
		float recallCoref = safeDiv(truePositive, truePositive + falseNegative, 0.0f);
		float recallNonCoref = safeDiv(trueNegative, trueNegative + falsePositive, 0.0f);


		float f1Coref = safeDiv(2 * precisionCoref * recallCoref, precisionCoref + recallCoref, 0.0f);
		float f1NonCoref = safeDiv(2 * precisionNonCoref * recallNonCoref, precisionNonCoref + recallNonCoref, 0.0f);

		System.out.println();
		System.out.println("DOCUMENT: " + referenceDocument.getName());
		System.out.println(String.format(printFormat, "Coreference links", truePositive, truePositive + falseNegative, recallCoref * 100, truePositive, truePositive + falsePositive, precisionCoref * 100, f1Coref * 100));
		System.out.println(String.format(printFormat, "NonCoreference links",  trueNegative, trueNegative + falsePositive, recallNonCoref * 100, trueNegative, trueNegative + falseNegative, precisionNonCoref * 100, f1Coref * 100));
		System.out.println(String.format(printFormat, "BLANC", (recallCoref + recallNonCoref) / 2, 1f, (recallCoref + recallNonCoref) * 100 / 2, (precisionCoref + precisionNonCoref) / 2, 1f,  (precisionCoref + precisionNonCoref)  * 100 / 2, (f1Coref + f1NonCoref) * 100 / 2));
		printTotal();
	}
	
}
