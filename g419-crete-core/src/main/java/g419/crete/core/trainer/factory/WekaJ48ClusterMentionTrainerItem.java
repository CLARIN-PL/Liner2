package g419.crete.core.trainer.factory;

import g419.crete.core.trainer.WekaJ48ClusterMentionTrainer;

public class WekaJ48ClusterMentionTrainerItem extends CreteTrainerFactoryItem{
	
	public WekaJ48ClusterMentionTrainer getTrainer(){
		return new WekaJ48ClusterMentionTrainer();
	}
	
}