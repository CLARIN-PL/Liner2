package liner2.daemon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import liner2.chunker.Chunker;

import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.writer.StreamWriter;
import liner2.writer.WriterFactory;

import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;

public class WorkingThread extends Thread {
	LinerDaemon daemon;
	Chunker chunker;
	Database db;
	
	public WorkingThread(LinerDaemon daemon, Chunker chunker, String databaseAddress) {
		this.daemon = daemon;
		this.chunker = chunker;
		this.db = new Database(databaseAddress);
	}

	@Override
	public void run() {
		Request request = null;

		try {
			this.db.connect();
			request = db.getNextRequest();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		while (request != null) {
			try {
				processRequest(request);
				this.db.submitResult(request);
			} catch (Exception ex) {
				try {
					this.db.submitError(request.getId(), ex.getMessage());
				} catch (SQLException sqlex) {
					sqlex.printStackTrace();
					request = null;
				}
			}
			
			try {
				request = db.getNextRequest();
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
		daemon.sleep();
	}

	//TODO porzucić żądanie w przypadku przerwania wątku
	//@Override
	//public void interrupt() {
	//}

	private void processRequest(Request request) throws Exception {
		ByteArrayInputStream ins = new ByteArrayInputStream(request.getText().getBytes());
		StreamReader reader = ReaderFactory.get().getStreamReader(ins, request.getInputFormat());
					
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		StreamWriter writer = WriterFactory.get().getStreamWriter(ous, request.getOutputFormat());

		// process text and calculate stats
		ParagraphSet ps = reader.readParagraphSet();
		int numTokens = 0, numSentences = 0, numParagraphs = 0, numChunks = 0;
		for (Paragraph p : ps.getParagraphs()) {
			for (Sentence s : p.getSentences()) {
				this.chunker.chunkSentenceInPlace(s);

				numSentences++;
				numTokens += s.getTokenNumber();
				numChunks += s.getChunks().size();
			}
			numParagraphs++;
		}
		writer.writeParagraphSet(ps);

		// save results
		request.setStats(numTokens, numSentences, numParagraphs, numChunks);
		request.setText(ous.toString());
	}
}

