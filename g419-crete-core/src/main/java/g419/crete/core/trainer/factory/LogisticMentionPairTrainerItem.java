package g419.crete.core.trainer.factory;

import g419.crete.core.trainer.LogisticMentionPairTrainer;

/**
 * Created by akaczmarek on 30.11.15.
 */
public class LogisticMentionPairTrainerItem extends CreteTrainerFactoryItem {

    public LogisticMentionPairTrainer getTrainer(){
        return new LogisticMentionPairTrainer();
    }

}

