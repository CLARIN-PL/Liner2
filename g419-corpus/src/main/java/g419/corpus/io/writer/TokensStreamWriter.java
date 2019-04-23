package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


/*
 * Drukowanie wyników w postaci listy tokenów.
 * @author Dominik Piasecki
 */
public class TokensStreamWriter extends AbstractDocumentWriter {
  private final BufferedWriter ow;

  public TokensStreamWriter(final OutputStream os) {
    ow = new BufferedWriter(new OutputStreamWriter(os));
  }

  @Override
  public void close() {
    try {
      ow.flush();
      ow.close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void writeDocument(final Document document) {
    for (final Paragraph paragraph : document.getParagraphs()) {
      writeParagraph(paragraph);
    }
  }

  public void writeParagraph(final Paragraph paragraph) {
    for (final Sentence s : paragraph.getSentences()) {
      writeSentence(s);
    }
  }

  private void writeSentence(final Sentence sentence) {
    String response = "";
    try {
      for (final Annotation c : sentence.getChunks()) {
        response += String.format("[%d,%d,%s]", c.getBegin() + 1, c.getEnd() + 1,
            c.getType());
      }
      if (response.isEmpty()) {
        response = "NONE";
      }
      ow.write(response);
      ow.newLine();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void flush() {
    try {
      ow.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
