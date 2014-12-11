package g419.crete.api.cluster;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.RelationClusterSet;
import g419.crete.api.annotation.AbstractAnnotationSelector;
import g419.crete.api.annotation.AnnotationSelectorFactory;

import java.util.List;

public class ClosestNamedEntityAnnotationClusterer extends AbstractAnnotationClusterer{

	@Override
	public RelationClusterSet resolveRelations(Document document, AbstractAnnotationSelector selector) {
		RelationClusterSet clusters = new RelationClusterSet();
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
