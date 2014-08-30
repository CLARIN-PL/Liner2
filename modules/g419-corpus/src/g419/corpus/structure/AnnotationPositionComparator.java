package g419.corpus.structure;

import java.util.Comparator;


/**
 * Porównywarka anotacji służąca do sortowania wg. pozycji w tekście
 * @author Adam Kaczmarek<adamjankaczmarek@gmail.com>
 *
 */
public class AnnotationPositionComparator implements Comparator<Annotation>{

	/**
	 * Anotacje są porównywane wg. pozycji w tekście, a następnie wg. długości
	 */
	@Override
	public int compare(Annotation ann1, Annotation ann2) {
		int result = Integer.signum(ann1.getBegin() - ann2.getBegin());
		if(result != 0) return result;
		
		return Integer.signum(ann1.getTokens().size() - ann2.getTokens().size());
	}
	
}
