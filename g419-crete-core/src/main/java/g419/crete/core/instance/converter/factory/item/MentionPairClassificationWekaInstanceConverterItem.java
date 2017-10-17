package g419.crete.core.instance.converter.factory.item;

import g419.crete.core.classifier.AbstractCreteClassifier;
import g419.crete.core.classifier.WekaClassifier;
import g419.crete.core.instance.MentionPairClassificationInstance;
import g419.crete.core.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.core.instance.converter.MentionPairClassificationWekaInstanceConverter;
import weka.core.Instance;

public class MentionPairClassificationWekaInstanceConverterItem implements	ICreteInstanceConverterFactoryItem<MentionPairClassificationInstance<?>, Instance> {

	@Override
	public AbstractCreteInstanceConverter<MentionPairClassificationInstance<?>, Instance> createConverter(AbstractCreteClassifier<?, Instance, ?> classifier) {
		return new MentionPairClassificationWekaInstanceConverter((WekaClassifier) classifier);
	}

}