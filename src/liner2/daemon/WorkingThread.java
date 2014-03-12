package liner2.daemon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import liner2.LinerOptions;
import liner2.chunker.Chunker;

import liner2.reader.ReaderFactory;
import liner2.reader.AbstractDocumentReader;
import liner2.tools.Template;
import liner2.writer.AbstractDocumentWriter;
import liner2.writer.WriterFactory;

import liner2.structure.Paragraph;
import liner2.structure.Document;
import liner2.structure.Sentence;

import liner2.Main;

public class WorkingThread extends Thread {
	DaemonThread daemon;
	Chunker chunker;
	Database db;
	
	public WorkingThread(DaemonThread daemon, Chunker chunker, String databaseAddress) {
		this.daemon = daemon;
		this.chunker = chunker;
		this.db = new Database(databaseAddress);
	}

	@Override
	public void run() {
		Request request = null;

		try {
			this.db.connect();
			Main.log("Searching for work...", true);
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
				Main.log("Searching for work...", true);
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
		Main.log("Processing request with id: " + request.getId(), false);		

		ByteArrayInputStream ins = new ByteArrayInputStream(request.getText().getBytes());
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader("byte stream", ins, request.getInputFormat());
					
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
        AbstractDocumentWriter writer;
        if (request.getOutputFormat().equals("arff")){
            Template arff_template = LinerOptions.getGlobal().getArffTemplate();
            writer = WriterFactory.get().getArffWriter(ous, arff_template);
        }
        else{
            writer = WriterFactory.get().getStreamWriter(ous, request.getOutputFormat());
        }

		// process text and calculate stats
		Document ps = reader.nextDocument();
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

		Main.log("Request processing completed: " + request.getId(), false);
	}
}

