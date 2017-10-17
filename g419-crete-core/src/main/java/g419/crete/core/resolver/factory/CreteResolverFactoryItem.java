package g419.crete.core.resolver.factory;

import g419.crete.core.instance.AbstractCreteInstance;
import g419.crete.core.resolver.AbstractCreteResolver;

public abstract class CreteResolverFactoryItem<L> {
	
	public abstract AbstractCreteResolver<?, AbstractCreteInstance<L>, ?, L> getResolver();
	
}
