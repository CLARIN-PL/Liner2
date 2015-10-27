package g419.crete.api.evaluation;

import g419.corpus.structure.AnnotationClusterSet;
import g419.corpus.structure.Document;
import g419.liner2.api.tools.FscoreEvaluator;

public class BlancEvaluator extends FscoreEvaluator{
	
	public void evaluate(Document systemResult, Document referenceDocument){
		AnnotationClusterSet goldClusterSet = AnnotationClusterSet.fromRelationSet(referenceDocument.getRelations());
		AnnotationClusterSet sysClusterSet = AnnotationClusterSet.fromRelationSet(systemResult.getRelations());
	}
	
}
