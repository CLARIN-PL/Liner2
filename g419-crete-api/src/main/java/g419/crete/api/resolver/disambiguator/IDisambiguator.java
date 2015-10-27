package g419.crete.api.resolver.disambiguator;

import g419.crete.api.instance.AbstractCreteInstance;

import java.util.List;

public  interface IDisambiguator<T extends AbstractCreteInstance<?>> {
	public T disambiguate(List<T> instances);
}
