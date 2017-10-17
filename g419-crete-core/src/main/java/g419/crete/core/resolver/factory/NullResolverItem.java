package g419.crete.core.resolver.factory;

import g419.crete.core.resolver.AbstractCreteResolver;
import g419.crete.core.resolver.NullResolver;

public class NullResolverItem extends CreteResolverFactoryItem {

	@Override
	public AbstractCreteResolver getResolver() {
		return new NullResolver();
	}

}
