package g419.crete.core.features.clustermention.preceeding;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.crete.core.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.core.features.enumvalues.Person;
import g419.crete.core.structure.AnnotationUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class ClusterMentionClosestPreceedingMentionPerson extends AbstractClusterMentionFeature<Person>{

	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		if(closestPreceeding == null){
			this.value = Person.UNDEFINED;
		}
		else{
			closestPreceeding.assignHead();
			Token headToken = closestPreceeding.getSentence().getTokens().get(closestPreceeding.getHead());
			
			TokenAttributeIndex ai = closestPreceeding.getSentence().getAttributeIndex();
					
			this.value = Person.fromValue(ai.getAttributeValue(headToken, "person"));
		}
	}

	@Override
	public String getName() {
		return "clustermention_closest_preceeding_person";
	}

	@Override
	public Class<Person> getReturnTypeClass() {
		return Person.class;
	}
	
	@Override
	public int getSize() {
		return Person.values().length;
	}

	@Override
	public List<Person> getAllValues(){
		return Arrays.asList(Person.values());
	}	
	
}
