package g419.crete.api.instance.converter.factory.item;

import g419.crete.api.classifier.AbstractCreteClassifier;
import g419.crete.api.instance.AbstractCreteInstance;
import g419.crete.api.instance.converter.AbstractCreteInstanceConverter;

public interface ICreteInstanceConverterFactoryItem<T extends AbstractCreteInstance<?>, I> {
	public AbstractCreteInstanceConverter<T, I> createConverter(AbstractCreteClassifier<?, I, ?> classifier);
}
