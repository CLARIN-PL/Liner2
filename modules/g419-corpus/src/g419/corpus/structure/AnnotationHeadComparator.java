package g419.corpus.structure;

import java.util.Comparator;

public class AnnotationHeadComparator implements Comparator<Annotation> {

	@Override
	public int compare(Annotation ann1, Annotation ann2) {
		return ((Integer)ann1.getHead()).compareTo((Integer)ann2.getHead());
	}

}
