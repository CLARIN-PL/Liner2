package g419.crete.core.resolver.factory;

import g419.crete.core.resolver.WekaJ48SequentialResolver;

public class WekaJ48ResolverItem extends CreteResolverFactoryItem{

	@Override
	public WekaJ48SequentialResolver getResolver() {
		return new WekaJ48SequentialResolver();
	}

}
