package g419.liner2.daemon.utils;

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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by michal on 11/25/14.
 */
public class FileBasedWorkingThread extends WorkingThread {

    File request;
    JSONObject options;
    FilebasedDaemonThread daemon;

    public FileBasedWorkingThread(final FilebasedDaemonThread daemon) {
        this.daemon = daemon;
    }

    @Override
    public void run() {
        while (true) {
            if (request != null) {
                processFile(request, options);
                options = null;
                request = null;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public boolean isBusy() {
        return request != null;
    }

    public void assignJob(final File request, final JSONObject options) {
        this.request = request;
        this.options = options;
    }


    public void processFile(final File to_process, final JSONObject options) {
        try {
            final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(to_process.getAbsolutePath(), "ccl");
            String model = options.getString("model");
            if (model.equals("default")) {
                model = DaemonOptions.getGlobal().defaultModel;
            }
            if (!daemon.chunkers.containsKey(model)) {
                throw new Exception("Unknown model name: " + model);
            }
            final TokenFeatureGenerator gen = daemon.featureGenerators.get(model);
            final Chunker chunker = daemon.chunkers.get(model);

            ConsolePrinter.log("Processing request with id: " + to_process.getName() + "with model: " + model, false);
            // process text and calculate stats
            final Document ps = reader.nextDocument();
            reader.close();
            if (gen != null) {
                gen.generateFeatures(ps);
            }
            int numTokens = 0, numSentences = 0, numParagraphs = 0, numChunks = 0;
            chunker.chunkInPlace(ps);

            for (final Paragraph p : ps.getParagraphs()) {
                for (final Sentence s : p.getSentences()) {
                    numSentences++;
                    numTokens += s.getTokenNumber();
                    numChunks += s.getChunks().size();
                }
                numParagraphs++;
            }

            final AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(to_process.getAbsolutePath().replace("progress", "done"), "ccl");
            writer.writeDocument(ps);
            writer.close();

        } catch (final Exception e) {
            to_process.renameTo(new File(to_process.getAbsolutePath().replace("progress", "error")));
            try {
                final PrintStream writer = new PrintStream(to_process);
                e.printStackTrace(writer);
                writer.close();
            } catch (final IOException e1) {
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
