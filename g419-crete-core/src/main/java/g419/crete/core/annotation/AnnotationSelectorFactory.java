package g419.crete.core.annotation;

import g419.crete.core.CreteOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnnotationSelectorFactory {

	private static class FactoryHolder {
        private static final AnnotationSelectorFactory FACTORY = new AnnotationSelectorFactory();
    }
	
	private HashMap<String, AbstractAnnotationSelectorFactoryItem> selectors;
	private HashMap<String, String> selectorNameMapping;
	
	private AnnotationSelectorFactory(){
		selectors = new HashMap<String, AbstractAnnotationSelectorFactoryItem>();
		selectors.put("all_annotation_selector", new AllAnnotationSelectorItem());
		selectors.put("configurable_annotation_selector", new ConfigurableAnnotationSelectorItem());
		
		selectorNameMapping = new HashMap<String, String>();
	}
	
	public void addMapping(String selectorName, String selectorTypeName){
		selectorNameMapping.put(selectorName, selectorTypeName);
	}
	
	public static AnnotationSelectorFactory getFactory(){
		return FactoryHolder.FACTORY;
	}
	
	public AbstractAnnotationSelector getInitializedSelector(String name){
		List<AnnotationDescription> descriptions = CreteOptions.getOptions().getSelectors().get(name);
		if(descriptions == null) descriptions = new ArrayList<AnnotationDescription>();
		return getInitializedSelector(name, descriptions);
	}
	
	public AbstractAnnotationSelector getInitializedSelector(String name, List<AnnotationDescription> annotationDescriptions){
		AbstractAnnotationSelectorFactoryItem selectorItem = selectors.get(name);
		if(selectorItem == null){
			String nameMapping = selectorNameMapping.get(name);
			if(nameMapping == null) return null;
			selectorItem = selectors.get(nameMapping);
			if(selectorItem == null) return null;
		}
		return selectorItem.getSelector(annotationDescriptions);
	}
	
//	public AbstractAnnotationSelector getSelector(String name){
//		return selectors.get(name);
//	}
}
