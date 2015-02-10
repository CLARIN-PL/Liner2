package g419.crete.api.trainer.factory;

import g419.crete.api.instance.AbstractCreteInstance;
import g419.crete.api.trainer.AbstractCreteTrainer;

public abstract class CreteTrainerFactoryItem<L> {
	
	public abstract AbstractCreteTrainer<?, AbstractCreteInstance<L>, ?, L> getTrainer();
	
}
