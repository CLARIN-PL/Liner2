package g419.crete.api.instance.converter.factory.item;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.classifier.WekaClassifier;
import g419.crete.api.instance.ClusterClassificationInstance;
import g419.crete.api.instance.converter.AbstractCreteInstanceConverter;
import g419.crete.api.instance.converter.ClusterClassificationWekaInstanceConverter;
import weka.core.Instance;

public class ClusterClassificationWekaInstanceConverterItem implements ICreteInstanceConverterFactoryItem<ClusterClassificationInstance, Instance>{

	@Override
	public AbstractCreteInstanceConverter<ClusterClassificationInstance, Instance> createConverter(AbstractCreteClassifier<?, Instance, ?> classifier) {
		return new ClusterClassificationWekaInstanceConverter((WekaClassifier) classifier);
	}

}
