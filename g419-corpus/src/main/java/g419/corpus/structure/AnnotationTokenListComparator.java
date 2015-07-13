package g419.corpus.structure;

import java.util.Comparator;

public class AnnotationTokenListComparator implements Comparator<Annotation> {

	private final boolean sameChannel;
	private Document refDocument;
	
	public AnnotationTokenListComparator(){
		this.sameChannel = false;
	}
	
	public AnnotationTokenListComparator(boolean sameChannel){
		this.sameChannel = sameChannel;
	}
	
	@Override
	public int compare(Annotation ann1, Annotation ann2) {
		boolean channelEquality = !sameChannel || (ann1.getType() != null && ann1.getType().equals(ann2.getType()));
		// TODO: Refaktoring mapowania nazw własnych tei-ccl
		//ann1.getSentence().getOrd() == ann2.getSentence().getOrd() && 
		if(ann1.getTokens().equals(ann2.getTokens()) && channelEquality) return 0;
		return -1;
	}

}
