package g419.crete.core.cluster;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.AnnotationClusterSet;
import g419.crete.core.annotation.AbstractAnnotationSelector;
import g419.crete.core.annotation.AnnotationSelectorFactory;

import java.util.List;

public class ClosestNamedEntityAnnotationClusterer extends AbstractAnnotationClusterer{

	@Override
	public AnnotationClusterSet resolveRelations(Document document, AbstractAnnotationSelector selector) {
		AnnotationClusterSet clusters = new AnnotationClusterSet();
		AbstractAnnotationSelector namedEntitySelector = AnnotationSelectorFactory.getFactory().getInitializedSelector("named_entity_selector");
		List<Annotation> mentions = selector.selectAnnotations(document);
		List<Annotation> entities = namedEntitySelector.selectAnnotations(document);
		
		for(Annotation mention : mentions){
			for(Annotation namedEntity : entities){
				if(namedEntity.getSentence().equals(mention.getSentence())) clusters.addRelation(new Relation(mention, namedEntity, Relation.COREFERENCE));
			}
		}
		
		return clusters;
	}

}
