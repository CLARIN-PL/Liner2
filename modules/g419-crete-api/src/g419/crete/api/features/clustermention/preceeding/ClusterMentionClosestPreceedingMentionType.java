package g419.crete.api.features.clustermention.preceeding;

import java.util.Arrays;
import java.util.List;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.features.enumvalues.MentionType;
import g419.crete.api.structure.AnnotationUtil;

import org.apache.commons.lang3.tuple.Pair;

public class ClusterMentionClosestPreceedingMentionType extends AbstractClusterMentionFeature<MentionType>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		if(closestPreceeding == null){
			this.value = MentionType.NONE;
			return;
		}
		
		this.value = AnnotationUtil.getMentionType(closestPreceeding);
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_mention_type";
	}

	@Override
	public Class<MentionType> getReturnTypeClass() {
		return MentionType.class;
	}

	@Override
	public List<MentionType> getAllValues(){
		return Arrays.asList(MentionType.values());
	}
}
