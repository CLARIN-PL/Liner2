package g419.crete.api.resolver.factory;

import g419.crete.api.resolver.WekaJ48MentionPairResolver;

public class WekaJ48MentionPairResolverItem extends CreteResolverFactoryItem{

	@Override
	public WekaJ48MentionPairResolver getResolver() {
		return new WekaJ48MentionPairResolver();
	}

}
