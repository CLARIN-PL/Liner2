package g419.liner2.api.chunker;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.util.HashMap;

public class ChunkRelChunker extends Chunker {

	private final String TMP_OUTPUT_FOLDER = "/home/adam/Desktop/crtmp/";
	private final String PYTHON_PATH = "/home/adam/anaconda/envs/nlppwr/bin/python";
	private final String CHUNKREL_PATH = "/home/adam/workspace/nlp/chunkrel/trainer/";
	private final String CHUNKREL_CONFIG_PATH = "/home/adam/workspace/nlp/ikar/chunkrel_config/";
	private final String CHUNKREL_MODEL_PATH = "/home/adam/workspace/nlp/chunkrel/trainer/model/";
	
	@Override
	public HashMap<Sentence, AnnotationSet> chunk(Document document) {
		String tmpFileName = TMP_OUTPUT_FOLDER + document.hashCode() + ".xml";
		
		try {
			AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(tmpFileName, "ccl");
	        writer.writeDocument(document);
	        writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}   
		
		String cmd = PYTHON_PATH + " " + CHUNKREL_PATH + "markchunkrel.py -c " + CHUNKREL_CONFIG_PATH + "chunkrel.ini -w " + CHUNKREL_MODEL_PATH+ " -s --in " + tmpFileName + " " + tmpFileName; 

		Process p = null;
		try {
			System.out.println(cmd);
			p = Runtime.getRuntime().exec(cmd);
			System.out.println("Waiting for ChunkRel");
			p.waitFor();
			System.out.println("ChunkRel finished!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		try {
			AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(tmpFileName, "ccl");
			Document relationsDocument = reader.nextDocument();
			document.setRelations(relationsDocument.getRelations());
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new HashMap<Sentence, AnnotationSet>();
	}

}
