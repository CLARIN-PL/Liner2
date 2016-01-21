package g419.crete.api.resolver.disambiguator;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationPositionComparator;
import g419.crete.api.instance.MentionPairClassificationInstance;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MentionPairClosestDisambiguator implements IDisambiguator<MentionPairClassificationInstance<Integer>>{

	static class MentionPairClosestComparator implements Comparator<MentionPairClassificationInstance>{

		private Comparator<Annotation> internalComparator = new AnnotationPositionComparator();
		
		@Override
		public int compare(MentionPairClassificationInstance instance1,	MentionPairClassificationInstance instance2) {
			return internalComparator.compare(instance1.getAntecedent(), instance2.getAntecedent());
		}
		
	}	
	
	@Override
	public MentionPairClassificationInstance<Integer> disambiguate(List<MentionPairClassificationInstance<Integer>> instances) {
		
		if(instances == null || instances.size() <= 0) return null;
	
		Collections.sort(instances, new MentionPairClosestComparator());
		return instances.get(0);
	}

	

}
