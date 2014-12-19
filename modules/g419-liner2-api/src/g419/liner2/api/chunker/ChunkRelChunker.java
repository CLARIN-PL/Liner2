package g419.liner2.api.chunker;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.liner2.api.LinerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.ini4j.Ini;

public class ChunkRelChunker extends Chunker {

	public final static String CHUNKER_NAME = "chunkrel";
	public final static String OPTION_TYPE = "type";
	
	private String TMP_OUTPUT_FOLDER;
	private String PYTHON_PATH;
	private String CHUNKREL_PATH;
	private String CHUNKREL_CONFIG_PATH;
	private String CHUNKREL_MODEL_PATH ;
	
	public ChunkRelChunker() {
		for(Ini.Section section : LinerOptions.getGlobal().chunkersDescriptions)
			if(CHUNKER_NAME.equals(section.get(OPTION_TYPE))){
				TMP_OUTPUT_FOLDER = section.get("tmp_output_folder");
				PYTHON_PATH = section.get("python_path");
				CHUNKREL_PATH = section.get("chunkrel_path");
				CHUNKREL_CONFIG_PATH = section.get("chunkrel_config_path");
				CHUNKREL_MODEL_PATH = section.get("chunkrel_model_path");
			}
	}
	
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
		finally{
			deleteFile(tmpFileName);
		}

		return new HashMap<Sentence, AnnotationSet>();
	}
	
	private void deleteFile(String filename){
		if(filename == null || "".equals(filename)) return;
		File f = new File(filename);
		f.delete();
		File fRel = new File(filename.replace(".xml", ".rel.xml"));
		fRel.delete();
	}

}
