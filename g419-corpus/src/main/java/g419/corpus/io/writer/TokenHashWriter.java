package g419.corpus.io.writer;

import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Print a list of tokens from a document. Each line has the following format:
 * <code>
 * orth[TAB]#base[TAB]#ctag#
 * </code>
 * and represent a single token.
 * The format does not contain information about spaces between tokens
 * nor sentence segmentation.
 *
 * @author Michał Marcińczuk
 */
public class TokenHashWriter extends AbstractDocumentWriter {
  private BufferedWriter ow;

  /**
   * Line format.
   */
  private static final String LINE = "%s\t#%s\t#%s#";

  public TokenHashWriter(OutputStream os) {
    this.ow = new BufferedWriter(new OutputStreamWriter(os));
  }

  @Override
  public void close() {
    try {
      this.ow.flush();
      this.ow.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void writeDocument(Document document) {
    for (Paragraph paragraph : document.getParagraphs()) {
      for (Sentence s : paragraph.getSentences()) {
        writeSentence(s);
      }
    }
  }

  private void writeSentence(Sentence sentence) {
    try {
      for (Token t : sentence.getTokens()) {
        this.ow.write(String.format(LINE, t.getOrth(), t.getDisambTag().getBase(), t.getDisambTag().getCtag()));
        this.ow.newLine();
      }
    } catch (IOException ex) {
      Logger.getLogger(this.getClass()).error("An error occured while writing the data.", ex);
    }
  }

  @Override
  public void flush() {
    try {
      ow.flush();
    } catch (IOException ex) {
      Logger.getLogger(this.getClass()).error("An error occured while flushing the writer.", ex);
    }
  }
}
