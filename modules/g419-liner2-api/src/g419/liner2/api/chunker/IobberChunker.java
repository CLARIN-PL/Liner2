package g419.liner2.api.chunker;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

public class IobberChunker extends Chunker {

	private String filePath;

	private final String IOBBER_PATH;
	private final String IOBBER_MODEL;
	private final String IOBBER_INI_PATH;
	private final String IOBBER_HYPHEN_OF_STDIN_PROCESSING = "-";

	public IobberChunker() {
		IOBBER_PATH = "/home/adam/anaconda/envs/nlppwr/bin/";
		IOBBER_MODEL = "model-kpwr11-H";
		IOBBER_INI_PATH = "/home/adam/workspace/nlp/ikar/settings/kpwr.ini";
	}

	public void setPath(String path) {
		filePath = path;
	}

	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document document) {
		String cmd = IOBBER_PATH + "iobber " + IOBBER_INI_PATH + " -d " + IOBBER_MODEL + " -i ccl " + IOBBER_HYPHEN_OF_STDIN_PROCESSING;
		Process p = null;
		String[] activateEnvCmd = new String[]{IOBBER_PATH + "iobber", IOBBER_INI_PATH + " -d " + IOBBER_MODEL + " -i ccl " + IOBBER_HYPHEN_OF_STDIN_PROCESSING};
		ProcessBuilder pb = new ProcessBuilder(activateEnvCmd);
		try {
//			p = pb.start();
			System.out.println(cmd);
			p = Runtime.getRuntime().exec(cmd);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		InputStream in = p.getInputStream();
		OutputStream out = p.getOutputStream();
		BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		
		try {
			AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(out, "ccl");
            writer.writeDocument(document);
            writer.close();
            
			AbstractDocumentReader reader = ReaderFactory.get().getStreamReader("ccl", in, "ccl");
			document = reader.nextDocument();
			reader.close();
			String error = err.readLine();
			if (error != null) {
				throw new Exception(error);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
//		
		return new HashMap<Sentence, AnnotationSet>();
	}

}
