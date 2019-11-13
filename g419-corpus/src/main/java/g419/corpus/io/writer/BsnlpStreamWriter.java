package g419.corpus.io.writer;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writes a set of annotations in a BSNLP2017 format.
 * The output consists of the following fields:
 * Number of annotations
 * <ul>
 * <li>text form,</li>
 * <li>lemma,</li>
 * <li>type,</li>
 * <li>lemma ID.</li>
 * </ul>
 *
 * @author Jan Kocoń
 */
public class BsnlpStreamWriter extends AbstractDocumentWriter {
  private BufferedWriter ow = null;
  /**
   * Current token index inside document
   */
  private Set<String> linesWritten = null;

  public BsnlpStreamWriter(final OutputStream os) {
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
    linesWritten = new HashSet<>();
    // look for document ID
    String documentID = "0";
    final Matcher matcher = Pattern.compile("\\d+").matcher((new File(document.getName())).getName());
    if (matcher.find()) {
      documentID = matcher.group();
    }
    try {
      ow.write(documentID + "\n");
    } catch (final IOException ex) {
      Logger.getLogger(getClass()).error("There was an error while writing document ID", ex);
    }
    for (final Paragraph paragraph : document.getParagraphs()) {
      for (final Sentence sentence : paragraph.getSentences()) {
        final Annotation[] chunks = Annotation.sortChunks(sentence.getChunks());
        for (final Annotation an : chunks) {
          try {
            writeChunk(an);
          } catch (final IOException ex) {
            Logger.getLogger(getClass()).error("There was an error while writing an annotation", ex);
          }
        }
      }
    }
    try {
      ow.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Writes a single annotation.
   *
   * @param an
   * @throws IOException
   */
  private void writeChunk(final Annotation an) throws IOException {
    final StringJoiner joiner = new StringJoiner("\t");
    final String type = an.getType().toUpperCase();
    joiner.add(an.getText());
    joiner.add(an.getLemmaOrText());
    joiner.add(type);
    joiner.add("#" + an.getLemmaOrText() + "#" + type + "#");
    final String newLine = joiner.toString() + "\n";

    if (!linesWritten.contains(newLine.toLowerCase())) {
      linesWritten.add(newLine.toLowerCase());
      ow.write(newLine);
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
