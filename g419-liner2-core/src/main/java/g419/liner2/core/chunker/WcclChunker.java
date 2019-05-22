package g419.liner2.core.chunker;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;


/**
 * Recognize annotations using a set of WCCL files stored in a given file.
 */
public class WcclChunker extends Chunker {

  private String wcclFile = null;

  public WcclChunker() {
  }

  public void setWcclFile(String filename) {
    this.wcclFile = filename;
  }

  private AnnotationSet chunkSentence(Sentence sentence) {
    AnnotationSet chunking = new AnnotationSet(sentence);
    String cmd = "wccl-rules -q -t nkjp -i ccl -I - -C " + this.wcclFile;
    Process p = null;
    try {
      p = Runtime.getRuntime().exec(cmd);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    InputStream in = p.getInputStream();
    OutputStream out = p.getOutputStream();
    BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));

    // zapamiętaj AttributeIndex, żeby nie stracić go przy addSentence()
    TokenAttributeIndex ai = sentence.getAttributeIndex();
    Document document = new Document("wccl chunker", ai);
    Paragraph paragraph = new Paragraph(null);
    LinkedHashSet<Annotation> sentenceAnns = sentence.getChunks();
    sentence.setAnnotations(new AnnotationSet(sentence));
    paragraph.addSentence(sentence);
    document.addParagraph(paragraph);
    document.setAttributeIndex(ai);

    try {
      AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(out, "ccl");
      writer.writeDocument(document);
      writer.close();

      AbstractDocumentReader reader = ReaderFactory.get().getStreamReader("wccl chunker", in, "ccl");
      document = reader.nextDocument();
      reader.close();
      String error = err.readLine();
      if (error != null) {
        throw new Exception(error);
      }
    } catch (Exception ex) {
      try {
        String error = err.readLine();
        if (error != null) {
          throw new Exception(error);
        } else {
          if (ex.getMessage().startsWith("Wcclerror:")) {
            System.err.println(ex.getMessage());
            System.err.println("WCCL rules failed on the sentence: " + sentence.toString());
          } else {
            ex.printStackTrace();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
    sentence.setAnnotations(new AnnotationSet(sentence, sentenceAnns));
    if (document.getSentences().size() > 0) {
      Sentence resultSentence = document.getSentences().get(0);
      for (Annotation chunk : resultSentence.getChunks()) {
        if (!chunking.contains(chunk)) {
          /* Create a new annotation with reference to the input sentence. */
          Annotation an = new Annotation(chunk.getBegin(), chunk.getEnd(), chunk.getType(), sentence);
          chunking.addChunk(an);
        }
      }
    }
    return chunking;
  }

  @Override
  public HashMap<Sentence, AnnotationSet> chunk(Document ps) {
    HashMap<Sentence, AnnotationSet> chunkings = new HashMap<Sentence, AnnotationSet>();
    for (Paragraph paragraph : ps.getParagraphs()) {
      for (Sentence sentence : paragraph.getSentences()) {
        chunkings.put(sentence, this.chunkSentence(sentence));
      }
    }
    return chunkings;
  }

}
