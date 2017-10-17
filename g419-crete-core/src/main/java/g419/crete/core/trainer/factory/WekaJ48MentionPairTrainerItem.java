package g419.crete.core.trainer.factory;

import g419.crete.core.trainer.WekaJ48MentionPairTrainer;


public class WekaJ48MentionPairTrainerItem extends CreteTrainerFactoryItem {

	public WekaJ48MentionPairTrainer getTrainer(){
		return new WekaJ48MentionPairTrainer();
	}
}
