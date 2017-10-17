package g419.crete.core.resolver.factory;

import g419.crete.core.resolver.WekaRandomForestMentionPairClusterClassifyResolver;

public class WekaRandomForestMentionPairClusterClassifyItem extends CreteResolverFactoryItem{

	@Override
	public WekaRandomForestMentionPairClusterClassifyResolver  getResolver() {
		return new WekaRandomForestMentionPairClusterClassifyResolver();
	}

}
