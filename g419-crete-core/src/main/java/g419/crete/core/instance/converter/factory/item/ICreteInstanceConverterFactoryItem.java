package g419.crete.core.instance.converter.factory.item;

import g419.crete.core.classifier.AbstractCreteClassifier;
import g419.crete.core.instance.AbstractCreteInstance;
import g419.crete.core.instance.converter.AbstractCreteInstanceConverter;

public interface ICreteInstanceConverterFactoryItem<T extends AbstractCreteInstance<?>, I> {
	public AbstractCreteInstanceConverter<T, I> createConverter(AbstractCreteClassifier<?, I, ?> classifier);
}
