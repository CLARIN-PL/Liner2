package g419.crete.api.resolver.factory;

import g419.crete.api.resolver.AbstractCreteResolver;
import g419.crete.api.resolver.NullResolver;

public class NullResolverItem extends CreteResolverFactoryItem {

	@Override
	public AbstractCreteResolver getResolver() {
		return new NullResolver();
	}

}
