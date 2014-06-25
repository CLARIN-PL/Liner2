package g419.liner2.daemon;


import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.CrfTemplate;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.HashMap;


public class WorkingThread extends Thread {
	DaemonThread daemon;
	Database db;
    HashMap<String, Chunker> chunkers;
    HashMap<String, TokenFeatureGenerator> featureGenerators;

	public WorkingThread(DaemonThread daemon, String databaseAddress, HashMap<String, Chunker> chunkers, HashMap<String, TokenFeatureGenerator> featureGenerators) {
		this.daemon = daemon;
		this.db = new Database(databaseAddress);
        this.chunkers = chunkers;
        this.featureGenerators = featureGenerators;
	}

	@Override
	public void run() {
		Request request = null;

		try {
			this.db.connect();
			Logger.log("Searching for work...", true);
			request = db.getNextRequest(this.daemon.getDaemonId());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		while (request != null) {
			try {
				processRequest(request);
				this.db.submitResult(request);
			} catch (Exception ex) {
				try {
					ex.printStackTrace();
					//Main.log("Error: " + ex.getMessage(), false);
					this.db.submitError(request.getId(), ex.getMessage());
				} catch (SQLException sqlex) {
					sqlex.printStackTrace();
					request = null;
				}
			}
			
			try {
				Logger.log("Searching for work...", true);
				request = db.getNextRequest(this.daemon.getDaemonId());
			} catch (SQLException ex) {
				ex.printStackTrace();
				request = null;
			}
		}
		
		try {
			this.db.disconnect();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		daemon.finishWorkingThread(this);
	}

	//TODO porzucić żądanie w przypadku przerwania wątku
	//@Override
	//public void interrupt() {
	//}

	private void processRequest(Request request) throws Exception {
		Logger.log("Processing request with id: " + request.getId(), false);

        String model = request.getModelName();
        if(!chunkers.containsKey(model)){
            throw new Exception("Unrecognized model in request: "+model);
        }
        if(model.equals("default")){
            model = DaemonOptions.getGlobal().getOption(DaemonOptions.getGlobal().defaultModel);
        }
        TokenFeatureGenerator gen = featureGenerators.get(model);
        Chunker chunker = chunkers.get(model);

        ByteArrayInputStream ins = new ByteArrayInputStream(request.getText().getBytes());
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader("byte stream", ins, request.getInputFormat());
					
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
        AbstractDocumentWriter writer;
        if (request.getOutputFormat().equals("arff")){
            CrfTemplate arff_template = DaemonOptions.getGlobal().getArffTemplate();
            writer = WriterFactory.get().getArffWriter(ous, arff_template);
        }
        else{
            writer = WriterFactory.get().getStreamWriter(ous, request.getOutputFormat());
        }

		// process text and calculate stats
		Document ps = reader.nextDocument();
        if(gen != null){
            gen.generateFeatures(ps);
        }
        int numTokens = 0, numSentences = 0, numParagraphs = 0, numChunks = 0;
        System.out.println("Before chunking");
		chunker.chunkInPlace(ps);
		System.out.println("After chunking");
		
		for (Paragraph p : ps.getParagraphs()) {
			for (Sentence s : p.getSentences()) {
				numSentences++;
				numTokens += s.getTokenNumber();
				numChunks += s.getChunks().size();
			}
			numParagraphs++;
		}
		writer.writeDocument(ps);

		// save results
		request.setStats(numTokens, numSentences, numParagraphs, numChunks);
		request.setText(ous.toString());

		Logger.log("Request processing completed: " + request.getId(), false);
	}
}

