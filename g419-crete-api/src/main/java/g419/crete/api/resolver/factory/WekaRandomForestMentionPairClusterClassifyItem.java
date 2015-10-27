package g419.crete.api.resolver.factory;

import g419.crete.api.resolver.WekaRandomForestMentionPairClusterClassifyResolver;

public class WekaRandomForestMentionPairClusterClassifyItem extends CreteResolverFactoryItem{

	@Override
	public WekaRandomForestMentionPairClusterClassifyResolver  getResolver() {
		return new WekaRandomForestMentionPairClusterClassifyResolver();
	}

}
