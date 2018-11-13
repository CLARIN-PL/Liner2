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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.HashMap;


public class SQLWorkingThread extends WorkingThread {
    SQLDaemonThread daemon;
    Database db;
    HashMap<String, Chunker> chunkers;
    HashMap<String, TokenFeatureGenerator> featureGenerators;

    public SQLWorkingThread(final SQLDaemonThread daemon, final String databaseAddress, final HashMap<String, Chunker> chunkers, final HashMap<String, TokenFeatureGenerator> featureGenerators) {
        this.daemon = daemon;
        db = new Database(databaseAddress);
        this.chunkers = chunkers;
        this.featureGenerators = featureGenerators;
    }

    @Override
    public void run() {
        Request request = null;

        try {
            db.connect();
            ConsolePrinter.log("Searching for work...", true);
            request = db.getNextRequest(daemon.getDaemonId());
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        while (request != null) {
            try {
                processRequest(request);
                db.submitResult(request);
            } catch (final Exception ex) {
                try {
                    ex.printStackTrace();
                    //Main.log("Error: " + ex.getMessage(), false);
                    db.submitError(request.getId(), ex.getMessage());
                } catch (final SQLException sqlex) {
                    sqlex.printStackTrace();
                    request = null;
                }
            }

            try {
                ConsolePrinter.log("Searching for work...", true);
                request = db.getNextRequest(daemon.getDaemonId());
            } catch (final SQLException ex) {
                ex.printStackTrace();
                request = null;
            }
        }

        try {
            db.disconnect();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
        daemon.finishWorkingThread(this);
    }

    //TODO porzucic zadanie w przypadku przerwania watku
    //@Override
    //public void interrupt() {
    //}

    private void processRequest(final Request request) throws Exception {
        ConsolePrinter.log("Processing request with id: " + request.getId(), false);

        String model = request.getModelName();
        if (model.equals("default")) {
            model = DaemonOptions.getGlobal().defaultModel;
        }
        if (!chunkers.containsKey(model)) {
            throw new Exception("Unrecognized model in request: " + model);
        }
        final TokenFeatureGenerator gen = featureGenerators.get(model);
        final Chunker chunker = chunkers.get(model);

        final ByteArrayInputStream ins = new ByteArrayInputStream(request.getText().getBytes());
        final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader("byte stream", ins, request.getInputFormat());

        final ByteArrayOutputStream ous = new ByteArrayOutputStream();
        final AbstractDocumentWriter writer;
        writer = WriterFactory.get().getStreamWriter(ous, request.getOutputFormat());

        // process text and calculate stats
        final Document ps = reader.nextDocument();
        if (gen != null) {
            gen.generateFeatures(ps);
        }
        int numTokens = 0, numSentences = 0, numParagraphs = 0, numChunks = 0;
        System.out.println("Before chunking");
        chunker.chunkInPlace(ps);
        System.out.println("After chunking");

        for (final Paragraph p : ps.getParagraphs()) {
            for (final Sentence s : p.getSentences()) {
                numSentences++;
                numTokens += s.getTokenNumber();
                numChunks += s.getChunks().size();
            }
            numParagraphs++;
        }
        writer.writeDocument(ps);
        writer.close();
        // save results
        request.setStats(numTokens, numSentences, numParagraphs, numChunks);
        request.setText(ous.toString());
        ConsolePrinter.log("Request processing completed: " + request.getId(), false);
    }
}

