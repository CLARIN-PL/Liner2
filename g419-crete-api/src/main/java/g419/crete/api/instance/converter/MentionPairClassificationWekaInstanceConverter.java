package g419.crete.api.instance.converter;

import g419.crete.api.classifier.WekaClassifier;
import g419.crete.api.instance.MentionPairClassificationInstance;
import weka.core.Instance;

public class MentionPairClassificationWekaInstanceConverter  extends AbstractCreteInstanceConverter<MentionPairClassificationInstance<?>, Instance>{

	public MentionPairClassificationWekaInstanceConverter(WekaClassifier cls) {
		super(cls);
	}

	@Override
	public Instance convertSingleInstance(MentionPairClassificationInstance instance) {
		Instance converted = csi(instance);
		return converted;
	}

	@Override
	public Class<Instance> getRepresentationClass() {
		return Instance.class;
	}
	
}
