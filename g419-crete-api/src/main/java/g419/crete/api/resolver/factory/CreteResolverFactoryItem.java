package g419.crete.api.resolver.factory;

import g419.crete.api.instance.AbstractCreteInstance;
import g419.crete.api.resolver.AbstractCreteResolver;

public abstract class CreteResolverFactoryItem<L> {
	
	public abstract AbstractCreteResolver<?, AbstractCreteInstance<L>, ?, L> getResolver();
	
}
