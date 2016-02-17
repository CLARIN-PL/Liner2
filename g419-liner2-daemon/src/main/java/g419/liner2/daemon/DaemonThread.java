package g419.liner2.daemon;

import g419.corpus.Logger;
import g419.lib.cli.ParameterException;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by michal on 11/20/14.
 */
public abstract class DaemonThread extends Thread{
    public static final int DEFAULT_MAX_THREADS = 5;

    int maxThreads;
    protected Vector<WorkingThread> workingThreads;
    protected HashMap<String, TokenFeatureGenerator> featureGenerators;
    protected  HashMap<String, Chunker> chunkers;

    public DaemonThread(int maxThreads) throws ParameterException {
        // setup maximum threads number
        this.maxThreads = maxThreads;
        // setup working threads
        this.workingThreads = new Vector<WorkingThread>();

        chunkers = new HashMap<String, Chunker>();
        featureGenerators = new HashMap<String, TokenFeatureGenerator>();
        try {
            for (String modelNam: DaemonOptions.getGlobal().models.keySet()){
                LinerOptions modelConfig = DaemonOptions.getGlobal().models.get(modelNam);
                ChunkerManager cm = new ChunkerManager(modelConfig);
                cm.loadChunkers();
                this.chunkers.put(modelNam, cm.getChunkerByName(modelConfig.getOptionUse()));
                TokenFeatureGenerator gen = null;
                if (!modelConfig.features.isEmpty()) {
                    gen = new TokenFeatureGenerator(modelConfig.features);
                }
                this.featureGenerators.put(modelNam, gen);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public  void run(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { shutdown(); }});
    }

    public void finishWorkingThread(WorkingThread callingThread){
        this.workingThreads.remove(callingThread);
        if (this.workingThreads.isEmpty())
            Logger.log("Sleeping...", false);
    }

    public void startWorkingThread(){
        if (this.workingThreads.isEmpty()) {
            Logger.log("Woke up!", false);
        }
    }

    public  void shutdown(){
        try {
            for (WorkingThread wt : this.workingThreads)
                wt.interrupt();
            this.interrupt();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Logger.log("Shutting down...", false);
    }
}
