package g419.crete.api.features.joint;

import g419.crete.api.features.AbstractFeature;

import java.util.List;

public abstract class AbstractJointFeature<I, O>  extends AbstractFeature<I, O>{

	private List<AbstractFeature<I, O>> features;
		
	
}
