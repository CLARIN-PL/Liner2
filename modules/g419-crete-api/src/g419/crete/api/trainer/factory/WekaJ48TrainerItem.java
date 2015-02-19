package g419.crete.api.trainer.factory;

import g419.crete.api.trainer.WekaJ48Trainer;

public class WekaJ48TrainerItem extends CreteTrainerFactoryItem{
	
	public WekaJ48Trainer getTrainer(){
		return new WekaJ48Trainer();
	}
	
}