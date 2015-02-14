package g419.crete.api.resolver.factory;

import g419.crete.api.resolver.WekaJ48SequentialResolver;

public class WekaJ48ResolverItem extends CreteResolverFactoryItem{

	@Override
	public WekaJ48SequentialResolver getResolver() {
		return new WekaJ48SequentialResolver();
	}

}
