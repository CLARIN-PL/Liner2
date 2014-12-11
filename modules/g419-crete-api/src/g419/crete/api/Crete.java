package g419.crete.api;

import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.RelationClusterSet;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.annotation.AnnotationDescription;
import g419.crete.api.annotation.AnnotationSelectorFactory;
import g419.crete.api.cluster.AbstractAnnotationClusterer;
import g419.crete.api.cluster.AnnotationClustererFactory;

import java.util.List;


public class Crete {

	public static final String CLUSTERER_PROPERTY = "clusterer";
	public static final String SELECTOR_PROPERTY = "selector";
	private AbstractAnnotationClusterer clusterer;
	private AbstractAnnotationSelector selector;
	
	public Crete(){
		clusterer = AnnotationClustererFactory.getFactory().getClusterer(CreteOptions.getOptions().properties.getProperty(CLUSTERER_PROPERTY));
		String selectorName = CreteOptions.getOptions().properties.getProperty(SELECTOR_PROPERTY);
		selector = AnnotationSelectorFactory.getFactory().getInitializedSelector(selectorName);
        System.out.println("CRETE created");
    }
    
    public void processDocument(Document document){
    	// Process
    	RelationClusterSet relations = clusterer.resolveRelations(document, selector);
    	// Post-process
    	for(Relation relation : relations.getRelationSet(null).getRelations())
    		document.addRelation(relation);
    }

}
