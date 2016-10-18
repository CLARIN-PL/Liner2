package g419.crete.api.cluster;

import java.util.HashMap;

public class AnnotationClustererFactory {
	
	private static class FactoryHolder {
        private static final AnnotationClustererFactory FACTORY = new AnnotationClustererFactory();
    }
	
	private HashMap<String, AbstractAnnotationClusterer> clusterers;
	
	private AnnotationClustererFactory(){
		clusterers = new HashMap<String, AbstractAnnotationClusterer>();
		clusterers.put("closest_ne_clusterer", new ClosestNamedEntityAnnotationClusterer());
	}
	
	public static AnnotationClustererFactory getFactory(){
		return FactoryHolder.FACTORY;
	}
	
	public AbstractAnnotationClusterer getClusterer(String name){
		return clusterers.get(name);
	}
}
