package g419.liner2.daemon.utils;

import g419.corpus.ConsolePrinter;
import g419.lib.cli.ParameterException;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.factory.ChunkerManager;
import g419.liner2.core.features.TokenFeatureGenerator;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by michal on 11/20/14.
 */
public abstract class DaemonThread extends Thread {
    public static final int DEFAULT_MAX_THREADS = 5;

    int maxThreads;
    protected Vector<WorkingThread> workingThreads;
    protected HashMap<String, TokenFeatureGenerator> featureGenerators;
    protected HashMap<String, Chunker> chunkers;

    public DaemonThread(final int maxThreads) throws ParameterException {
        // setup maximum threads number
        this.maxThreads = maxThreads;
        // setup working threads
        workingThreads = new Vector<>();

        chunkers = new HashMap<>();
        featureGenerators = new HashMap<>();
        try {
            for (final String modelNam : DaemonOptions.getGlobal().models.keySet()) {
                final LinerOptions modelConfig = DaemonOptions.getGlobal().models.get(modelNam);
                final ChunkerManager cm = new ChunkerManager(modelConfig);
                cm.loadChunkers();
                chunkers.put(modelNam, cm.getChunkerByName(modelConfig.getOptionUse()));
                TokenFeatureGenerator gen = null;
                if (!modelConfig.features.isEmpty()) {
                    gen = new TokenFeatureGenerator(modelConfig.features);
                }
                featureGenerators.put(modelNam, gen);
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public void finishWorkingThread(final WorkingThread callingThread) {
        workingThreads.remove(callingThread);
        if (workingThreads.isEmpty()) {
            ConsolePrinter.log("Sleeping...", false);
        }
    }

    public void startWorkingThread() {
        if (workingThreads.isEmpty()) {
            ConsolePrinter.log("Woke up!", false);
        }
    }

    public void shutdown() {
        try {
            for (final WorkingThread wt : workingThreads) {
                wt.interrupt();
            }
            interrupt();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        ConsolePrinter.log("Shutting down...", false);
    }
}
