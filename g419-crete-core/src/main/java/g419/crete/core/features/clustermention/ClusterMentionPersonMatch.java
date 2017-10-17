package g419.crete.core.features.clustermention;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionPersonMatch extends AbstractClusterMentionFeature<Float>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		String mentionPerson = AnnotationUtil.getAnnotationHeadAttribute(mention, "person");
		
		if(mentionPerson == null){
			this.value = 0.0f;
		}
		else{
			int totalMentions = 0;
			int totalMatches = 0;
			
			for(Annotation annotation : cluster.getAnnotations()){
				totalMentions++;
				String annPerson = AnnotationUtil.getAnnotationHeadAttribute(annotation, "person");
				if(mentionPerson.equalsIgnoreCase(annPerson)) totalMatches++;
			}
			
			this.value = ((float) totalMatches) / ((float) totalMentions);
		}
	}
	
		@Override
	public String getName() {
		return "clustermention_person_match";
	}

	@Override
	public Class<Float> getReturnTypeClass() {
		return Float.class;
	}

	
	
}
