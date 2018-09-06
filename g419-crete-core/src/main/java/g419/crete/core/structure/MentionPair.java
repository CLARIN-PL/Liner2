package g419.crete.core.structure;

import g419.corpus.structure.Annotation;
import org.apache.commons.lang3.tuple.Pair;


public class MentionPair extends IHaveFeatures<Pair<Annotation, Annotation>>{

	public MentionPair(Pair<Annotation, Annotation> holder) {
		super(holder);
	}

}