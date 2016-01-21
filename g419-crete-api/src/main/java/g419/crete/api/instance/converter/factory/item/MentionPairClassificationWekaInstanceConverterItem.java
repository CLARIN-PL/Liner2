package g419.crete.api.instance.converter.factory.item;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.classifier.WekaClassifier;
import g419.crete.api.instance.MentionPairClassificationInstance;
import g419.crete.api.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.api.instance.converter.MentionPairClassificationWekaInstanceConverter;
import g419.crete.api.instance.converter.factory.item.ICreteInstanceConverterFactoryItem;
import weka.core.Instance;

public class MentionPairClassificationWekaInstanceConverterItem implements	ICreteInstanceConverterFactoryItem<MentionPairClassificationInstance<?>, Instance> {

	@Override
	public AbstractCreteInstanceConverter<MentionPairClassificationInstance<?>, Instance> createConverter(AbstractCreteClassifier<?, Instance, ?> classifier) {
		return new MentionPairClassificationWekaInstanceConverter((WekaClassifier) classifier);
	}

}