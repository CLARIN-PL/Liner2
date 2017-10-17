package g419.crete.core.trainer.factory;

import g419.crete.core.instance.AbstractCreteInstance;
import g419.crete.core.trainer.AbstractCreteTrainer;

public abstract class CreteTrainerFactoryItem<L> {
	
	public abstract AbstractCreteTrainer<?, AbstractCreteInstance<L>, ?, L> getTrainer();
	
}
