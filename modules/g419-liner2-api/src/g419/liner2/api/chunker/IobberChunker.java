package g419.liner2.api.chunker;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;

public class IobberChunker extends Chunker {

	private final static String CHUNKER_NAME = "iobber";
	public final static String OPTION_TYPE = "type";
	
	private String IOBBER_PATH;
	private String IOBBER_MODEL;
	private String IOBBER_INI_PATH;
	private final String IOBBER_HYPHEN_OF_STDIN_PROCESSING = "-";

	public IobberChunker(String iobberPath, String iobberModel, String iobberIni) {
		IOBBER_PATH = iobberPath;
		IOBBER_MODEL = iobberModel;
		IOBBER_INI_PATH = iobberIni;
	}
	
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document document) {
		HashMap<Sentence, AnnotationSet> chunking = new HashMap<Sentence, AnnotationSet>();
		String cmd = IOBBER_PATH + "iobber " + IOBBER_INI_PATH + " -d " + IOBBER_MODEL + " -i ccl " + IOBBER_HYPHEN_OF_STDIN_PROCESSING;
		Process p = null;
		try {
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
			
			Document documentIobbed = reader.nextDocument();
			reader.close();
			
			for(Annotation an : documentIobbed.getAnnotations()){
				for(Sentence origSentence : document.getSentences()){
					if(origSentence.getId().equals(an.getSentence().getId())){
						AnnotationSet aSet;
						if(chunking.get(origSentence) == null){
							aSet = new AnnotationSet(origSentence);
							chunking.put(origSentence, aSet);
						}
						else{
							aSet = chunking.get(origSentence);
						}
						
						aSet.addChunk(new Annotation(an.getBegin(), an.getEnd(), an.getType(), origSentence)); 
					}
				}
			}
			
			String error = err.readLine();
			if (error != null) {
				throw new Exception(error);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return chunking; 
	}

}
