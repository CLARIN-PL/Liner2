package g419.crete.core.instance.converter;

import g419.crete.core.classifier.WekaClassifier;
import g419.crete.core.instance.MentionPairClassificationInstance;
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
