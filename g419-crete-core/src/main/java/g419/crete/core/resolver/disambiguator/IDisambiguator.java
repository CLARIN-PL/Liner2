package g419.crete.core.resolver.disambiguator;

import g419.crete.core.instance.AbstractCreteInstance;

import java.util.List;

public  interface IDisambiguator<T extends AbstractCreteInstance<?>> {
	public T disambiguate(List<T> instances);
}
