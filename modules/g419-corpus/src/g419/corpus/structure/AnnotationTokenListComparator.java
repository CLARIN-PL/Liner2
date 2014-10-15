package g419.corpus.structure;

import java.util.ArrayList;
import java.util.Comparator;

public class AnnotationTokenListComparator implements Comparator<Annotation> {

	@Override
	public int compare(Annotation ann1, Annotation ann2) {
		if(ann1.getTokens().equals(ann2.getTokens())) return 0;
		return -1;
	}

}
