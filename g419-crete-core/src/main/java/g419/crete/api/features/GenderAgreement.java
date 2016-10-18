package g419.crete.api.features;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Relation;

public class GenderAgreement extends AbstractFeature<Relation, Boolean> {

	private Boolean value;
	
	@Override
	public void generateFeature(Relation input) {
		Annotation from = input.getAnnotationFrom();
		Annotation to = input.getAnnotationTo();
		value = from.getBegin() == to.getBegin();
	}

	@Override
	public Boolean getValue() {
		return value;
	}

	@Override
	public Class<Relation> getInputTypeClass() {return Relation.class;}

	@Override
	public Class<Boolean> getReturnTypeClass() {return Boolean.class;}

	@Override
	public String getName() {
		return "gender_agreement";
	}

}
