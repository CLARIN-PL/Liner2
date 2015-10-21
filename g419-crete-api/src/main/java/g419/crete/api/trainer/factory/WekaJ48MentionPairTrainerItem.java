package g419.crete.api.trainer.factory;

import g419.crete.api.trainer.WekaJ48MentionPairTrainer;


public class WekaJ48MentionPairTrainerItem extends CreteTrainerFactoryItem {

	public WekaJ48MentionPairTrainer getTrainer(){
		return new WekaJ48MentionPairTrainer();
	}
}
