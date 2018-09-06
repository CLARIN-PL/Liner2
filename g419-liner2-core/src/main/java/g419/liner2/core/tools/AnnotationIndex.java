package g419.liner2.core.tools;

import g419.corpus.structure.Annotation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Index a set of annotation according to different criteria in order to fast look-up.
 * 
 * @author Michał Marcińczuk
 *
 */
public class AnnotationIndex {

	Map<String, Annotation> indexByRange = new HashMap<String, Annotation>();
	
	/**
	 * 
	 * @param annotations collection of annotations to index
	 */
	public AnnotationIndex(Collection<Annotation> annotations){
		for ( Annotation an : annotations ){
			String key = String.format("%d:%d", an.getBegin(), an.getEnd());
			this.indexByRange.put(key, an);
		}
	}
	
	/**
	 * Return an annotation for given range (from <code>begin</code> to <code>end</code>).
	 * @param begin
	 * @param end
	 * @return
	 */
	public Annotation get(int begin, int end){
		String key = String.format("%d:%d", begin, end);
		return this.indexByRange.get(key);
	}
	
}
