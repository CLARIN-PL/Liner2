package g419.corpus.io.writer;

import g419.corpus.structure.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;


/**
 * TODO Wykorzystać klasę ArffGenericWriter do obsługi zapisu w formacie ARFF.
 *
 * @author czuk
 */
public class ArffTokenStreamWriter extends AbstractDocumentWriter {

  private final BufferedWriter ow;
  private boolean init = false;

  public ArffTokenStreamWriter(final OutputStream os) {
    ow = new BufferedWriter(new OutputStreamWriter(os));
  }

  @Override
  public void writeDocument(final Document paragraphSet) {
    for (final Paragraph p : paragraphSet.getParagraphs()) {
      writeParagraph(p);
    }
  }


  protected void init(final TokenAttributeIndex attributeIndex) {
    if (init) {
      return;
    }
    try {
      String line = "@relation rel";
      ow.write(line, 0, line.length());
      ow.newLine();
      ow.newLine();
      for (int i = 0; i < attributeIndex.getLength(); i++) {
        line = "@attribute " + attributeIndex.getName(i) + " string";
        ow.write(line, 0, line.length());
        ow.newLine();
      }
      line = "@attribute iobtag string";
      ow.write(line, 0, line.length());
      ow.newLine();
      ow.newLine();
      line = "@data";
      ow.write(line, 0, line.length());
      ow.newLine();
    } catch (final IOException ex) {
      ex.printStackTrace();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
    init = true;
  }

  @Override
  public void flush() {
    try {
      ow.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() {
    try {
      ow.close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  public void writeParagraph(final Paragraph paragraph) {
    try {
      if (!init) {
        init(paragraph.getAttributeIndex());
      }
      for (final Sentence sentence : paragraph.getSentences()) {
        writeSentence(sentence);
      }
    } catch (final IOException ex) {
      ex.printStackTrace();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  private void writeSentence(final Sentence sentence) throws IOException {
    final List<Token> tokens = sentence.getTokens();
    for (int i = 0; i < tokens.size(); i++) {
      writeToken(i, tokens.get(i), sentence);
    }
  }

  private void writeToken(final int idx, final Token token, final Sentence sentence)
      throws IOException {
    String line = "";
    for (int i = 0; i < token.getNumAttributes(); i++) {
      String attrval = token.getAttributeValue(i);
      if (attrval == null) {
        attrval = "?";
      } else {
        attrval = "\'" + attrval.replace("\'", "\\\'") + "\'";
      }
      line += (line.length() > 0 ? ",\t" : "") + attrval;
    }

    line += " " + sentence.getTokenClassLabel(idx, null);
    ow.write(line, 0, line.length());
    ow.newLine();
  }


}
