package g419.corpus.structure;

import java.util.ArrayList;
import java.util.Comparator;

public class AnnotationTokenListComparator implements Comparator<Annotation> {

	private final boolean sameChannel;
	
	public AnnotationTokenListComparator(){
		this.sameChannel = false;
	}
	
	public AnnotationTokenListComparator(boolean sameChannel){
		this.sameChannel = sameChannel;
	}
	
	@Override
	public int compare(Annotation ann1, Annotation ann2) {
		boolean channelEquality = !sameChannel || (ann1.getType() != null && ann1.getType().equals(ann2.getType()));
		if(ann1.getSentence().getId().equals(ann2.getSentence().getId()) && ann1.getTokens().equals(ann2.getTokens()) && channelEquality) return 0;
		return -1;
	}

}
