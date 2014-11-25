package g419.liner2.daemon;

import g419.corpus.Logger;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.ParameterException;

import java.io.*;
import java.net.Socket;

/**
 * Created by michal on 11/20/14.
 */
public class FilebasedDaemonThread extends DaemonThread {

    protected File db_path;
    protected TokenFeatureGenerator gen;
    protected Chunker chunker;

    public FilebasedDaemonThread() throws ParameterException {
        super();
        db_path = new File(DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_PATH));
        String model = DaemonOptions.getGlobal().defaultModel;
        gen = featureGenerators.get(model);
        chunker = chunkers.get(model);
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            assigning_requests:
            for(File request: new File(db_path, "requests").listFiles()){
                FileBasedWorkingThread freeThread = null;
                for(WorkingThread thread: workingThreads){
                    if(!((FileBasedWorkingThread)thread).isBusy()){
                        freeThread = (FileBasedWorkingThread)thread;
                        break;
                    }
                }
                if(freeThread != null){
                    freeThread.assignJob(request);
                    continue assigning_requests;
                }
                if (this.workingThreads.size() < this.maxThreads){
                    startWorkingThread(request);
                    break;
                }

            }
        }
    }

    public void startWorkingThread(File request) {
        super.startWorkingThread();
        FileBasedWorkingThread newThread = new FileBasedWorkingThread(this);
        newThread.assignJob(request);
        newThread.start();
        this.workingThreads.add(newThread);
    }

    @Override
    public void shutdown() {

    }


}
