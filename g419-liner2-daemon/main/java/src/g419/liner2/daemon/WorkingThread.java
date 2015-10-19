package g419.liner2.daemon;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.features.TokenFeatureGenerator;

import java.util.HashMap;

/**
 * Created by michal on 11/25/14.
 */
public abstract class WorkingThread extends Thread {

    @Override
    public abstract void run();

}
