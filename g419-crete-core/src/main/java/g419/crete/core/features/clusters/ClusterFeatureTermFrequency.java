package g419.crete.core.features.clusters;

import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.crete.core.features.AbstractFeature;

public class ClusterFeatureTermFrequency extends AbstractFeature<AnnotationCluster, Float>{

	@Override
	public void generateFeature(AnnotationCluster input) {
		int clusterSize = input.getAnnotations().size();
		int totalTokens = 0;
		
		Document doc = input.getDocument();
		for(Paragraph paragraph : doc.getParagraphs())
			for(Sentence sentence : paragraph.getSentences())
				totalTokens += sentence.getTokenNumber();
		
		this.value =  ((float) clusterSize) / ((float) totalTokens);
	}

	@Override
	public String getName() {
		return "cluster_term_frequency";
	}

	@Override
	public Class<AnnotationCluster> getInputTypeClass() {
		return AnnotationCluster.class;
	}

	@Override
	public Class<Float> getReturnTypeClass() {
		return Float.class;
	}

}
