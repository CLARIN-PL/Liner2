package g419.liner2.core.chunker;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;

import java.io.File;
import java.util.HashMap;

public class ChunkRelChunker extends Chunker {

  public final static String CHUNKER_NAME = "chunkrel";
  public final static String OPTION_TYPE = "type";

  private String TMP_OUTPUT_FOLDER;
  private String PYTHON_PATH;
  private String CHUNKREL_PATH;
  private String CHUNKREL_CONFIG_PATH;
  private String CHUNKREL_MODEL_PATH;

  public ChunkRelChunker(String pythonPath, String chunkrelPath, String chunkrelConfigPath, String chunkrelModelPath, String tmpOut) {
    PYTHON_PATH = pythonPath;
    CHUNKREL_PATH = chunkrelPath;
    CHUNKREL_CONFIG_PATH = chunkrelConfigPath;
    CHUNKREL_MODEL_PATH = chunkrelModelPath;
    TMP_OUTPUT_FOLDER = tmpOut;
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

    String cmd = PYTHON_PATH + " " + CHUNKREL_PATH + "markchunkrel.py -c " + CHUNKREL_CONFIG_PATH + "chunkrel.ini -w " + CHUNKREL_MODEL_PATH + " -s --in " + tmpFileName + " " + tmpFileName;
    System.out.println(cmd);
    Process p = null;
    try {
//			System.out.println(cmd);
      p = Runtime.getRuntime().exec(cmd);
      System.out.println("Waiting for ChunkRel");
      p.waitFor();
      System.out.println("ChunkRel finished!");
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    try {
      AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(tmpFileName, "cclrel");
      Document relationsDocument = reader.nextDocument();
      document.setRelations(relationsDocument.getRelations());
      reader.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
//			deleteFile(tmpFileName);
    }

    return new HashMap<Sentence, AnnotationSet>();
  }

  private void deleteFile(String filename) {
    if (filename == null || "".equals(filename)) {
      return;
    }
    File f = new File(filename);
    f.delete();
    File fRel = new File(filename.replace(".xml", ".rel.xml"));
    fRel.delete();
  }

}
