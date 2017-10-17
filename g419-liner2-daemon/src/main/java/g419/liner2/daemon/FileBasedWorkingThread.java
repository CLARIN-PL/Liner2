package g419.liner2.daemon;

import g419.corpus.ConsolePrinter;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.features.TokenFeatureGenerator;
import org.json.JSONObject;

import java.io.*;

/**
 * Created by michal on 11/25/14.
 */
public class FileBasedWorkingThread extends WorkingThread {

    File request;
    JSONObject options;
    FilebasedDaemonThread daemon;

    public FileBasedWorkingThread(FilebasedDaemonThread daemon){
        this.daemon = daemon;
    }

    @Override
    public void run() {
        while(true){
            if(request != null){
                processFile(request, options);
                options = null;
                request = null;
            }
            else{
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e){ e.printStackTrace();}
            }

        }
    }
    public boolean isBusy(){
        return request != null;
    }

    public void assignJob(File request, JSONObject options){
        this.request = request;
        this.options = options;
    }
    

    public void processFile(File to_process, JSONObject options){
        try {
            AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(to_process.getAbsolutePath(), "ccl");
            String model = options.getString("model");
            if(model.equals("default")){
                model = DaemonOptions.getGlobal().defaultModel;
            }
            if(!daemon.chunkers.containsKey(model)){
                throw new Exception("Unknown model name: " + model);
            }
            TokenFeatureGenerator gen = daemon.featureGenerators.get(model);
            Chunker chunker = daemon.chunkers.get(model);

            ConsolePrinter.log("Processing request with id: " + to_process.getName() + "with model: " + model, false);
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

            AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(to_process.getAbsolutePath().replace("progress", "done"), "ccl");
            writer.writeDocument(ps);
            writer.close();

        } catch (Exception e) {
            to_process.renameTo(new File(to_process.getAbsolutePath().replace("progress", "error")));
            try {
                PrintStream writer = new PrintStream(to_process);
                e.printStackTrace(writer);
                writer.close();
            } catch (IOException e1) {
                ConsolePrinter.log("Error while creating error log for: " + to_process.getName(), false);
                e1.printStackTrace();
            }
            ConsolePrinter.log("Error while processing request: " + to_process.getName(), false);
            e.printStackTrace();
        }
        ConsolePrinter.log("Request processing completed: " + to_process.getName(), false);
        to_process.delete();
    }
}
