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

import java.io.File;

/**
 * Created by michal on 11/25/14.
 */
public class FileBasedWorkingThread extends WorkingThread {

    File request;
    private TokenFeatureGenerator gen;
    private Chunker chunker;
    FilebasedDaemonThread daemon;

    public FileBasedWorkingThread(FilebasedDaemonThread daemon){
        this.daemon = daemon;
        this.chunker = daemon.chunker;
        this.gen = daemon.gen;
    }

    @Override
    public void run() {
        while(request != null){
            File to_process = request;
            request = null;
            processFile(to_process);
        }
        daemon.finishWorkingThread(this);


    }
    public boolean isBusy(){
        return request != null;
    }

    public void assignJob(File request){
        File next_job = new File(String.format("%s/processing/%s", daemon.db_path.getAbsolutePath(), request.getName()));
        request.renameTo(next_job);
        this.request = next_job;
    }

    public void processFile(File to_process){
        try {
            AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(to_process.getAbsolutePath(), "ccl");

            Logger.log("Processing request with id: " + to_process.getName(), false);
            // process text and calculate stats
            Document ps = reader.nextDocument();
            reader.close();
            if(gen != null){
                gen.generateFeatures(ps);
            }
            int numTokens = 0, numSentences = 0, numParagraphs = 0, numChunks = 0;
            chunker.chunkInPlace(ps);

            for (Paragraph p : ps.getParagraphs()) {
                for (Sentence s : p.getSentences()) {
                    numSentences++;
                    numTokens += s.getTokenNumber();
                    numChunks += s.getChunks().size();
                }
                numParagraphs++;
            }

            AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(to_process.getAbsolutePath().replace("processing", "results"), "ccl");
            writer.writeDocument(ps);
            writer.close();

        } catch (Exception e) {
            to_process.renameTo(new File(to_process.getAbsolutePath().replace("processing", "errors")));
            Logger.log("Error while processing request: " + to_process.getName(), false);
            e.printStackTrace();
        }
        Logger.log("Request processing completed: " + to_process.getName(), false);
        to_process.delete();
    }
}
