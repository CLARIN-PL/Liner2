package g419.crete.core.resolver;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.crete.core.annotation.AbstractAnnotationSelector;

import java.util.List;

public class NullResolver extends AbstractCreteResolver {

	@Override
	protected Document resolveMention(Document document, Annotation mention, List instancesForMention) {
		return document;
	}
	
	@Override
	public Document resolveDocument(Document document, AbstractAnnotationSelector selector, AbstractAnnotationSelector singletonSelector){
		return document;
	}

	@Override
	public Class getModelClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getAbstractInstanceClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getClassifierInstanceClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class getLabelClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
