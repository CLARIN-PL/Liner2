package g419.crete.core.features.clustermention.preceeding;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.core.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.core.structure.AnnotationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;



public class ClusterMentionClosestPreceedingMentionDistance extends AbstractClusterMentionFeature<Integer>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		if(closestPreceeding == null){
			this.value = 10000;
			return;
		}
		
		List<Pattern> patterns = new ArrayList<Pattern>();
		patterns.add(Pattern.compile("anafora_wyznacznik"));
		patterns.add(Pattern.compile("nam.*"));
		patterns.add(Pattern.compile("anafora_verb_null.*"));
		
		
		List<Annotation> annotationsFollowing = AnnotationUtil.annotationsBetweenAnnotations(closestPreceeding, mention, cluster.getDocument(), patterns);
		this.value = annotationsFollowing.size();		
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_mention_distance";
	}

	@Override
	public Class<Integer> getReturnTypeClass() {
		return Integer.class;
	}

}
