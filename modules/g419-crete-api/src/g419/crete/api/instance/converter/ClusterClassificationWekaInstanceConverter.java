package g419.crete.api.instance.converter;

import g419.crete.api.classifier.WekaClassifier;
import g419.crete.api.instance.ClusterClassificationInstance;
import weka.core.Attribute;
import weka.core.Instance;

public class ClusterClassificationWekaInstanceConverter extends AbstractCreteInstanceConverter<ClusterClassificationInstance, Instance>{

	public ClusterClassificationWekaInstanceConverter(WekaClassifier<?,?> cls) {
		super(cls);
	}

	@Override
	public Instance convertSingleInstance(ClusterClassificationInstance instance) {
		Instance converted = csi(instance);
//		converted.setValue(((WekaClassifier<?, ?>)this.classifier).getAttributeIndex("CLASS"), instance.getLabel() + 1);
//		converted.setWeight(instance.getWeigth());
		return converted;
	}

	@Override
	public Class<Instance> getRepresentationClass() {
		return Instance.class;
	}
	
}