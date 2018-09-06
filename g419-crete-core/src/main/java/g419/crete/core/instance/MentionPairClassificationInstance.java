package g419.crete.core.instance;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.crete.core.structure.IHaveFeatures;
import g419.crete.core.structure.MentionPair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Arrays;
import java.util.List;

public class MentionPairClassificationInstance<L> extends AbstractCreteInstance<L>{

	private MentionPair mentionPair;

	public MentionPairClassificationInstance(){}

	public MentionPairClassificationInstance(Annotation firstAnnotation, Annotation secondAnnotation, L label, List<String> featureNames) {
		super(label, featureNames);
		this.mentionPair = new MentionPair(new ImmutablePair<Annotation, Annotation>(firstAnnotation, secondAnnotation));
		extractFeatures();
	}
	
	public Annotation getAntecedent(){
		return mentionPair.getHolder().getRight();
	}
	
	@Override
	public List<IHaveFeatures<?>> getComponents() {
		return Arrays.asList(new IHaveFeatures<?>[]{this.mentionPair});
	}

	public Relation toRelation(String relationType, String relationSet, Document document) {
		return new Relation(mentionPair.getHolder().getLeft(), mentionPair.getHolder().getRight(), relationType, relationSet, document);
	}
	
	@Override
	public String toString(){
		return getAntecedent().toString();
	}

	public static Class<MentionPairClassificationInstance<?>> getCls(){
		MentionPairClassificationInstance<?> mpci = new MentionPairClassificationInstance<>();
		return (Class<MentionPairClassificationInstance<?>>) mpci.getClass();
	}
}
