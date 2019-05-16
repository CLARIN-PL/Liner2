package g419.corpus.io.writer;

import g419.corpus.structure.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class IobTabStreamWriter extends AbstractDocumentWriter {

  private final BufferedWriter ow;
  private boolean init = false;
  private final ArrayList<Integer> widths = new ArrayList<>();

  public IobTabStreamWriter(final OutputStream os) {
    ow = new BufferedWriter(new OutputStreamWriter(os));
  }

  protected void init(final TokenAttributeIndex attributeIndex) {
    if (init) {
      return;
    }
    try {
      String line = "-DOCSTART CONFIG FEATURES";
      for (int i = 0; i < attributeIndex.getLength(); i++) {
        line += " " + attributeIndex.getName(i);
      }
      ow.write(line, 0, line.length());
      ow.newLine();
    } catch (final IOException ex) {
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

  @Override
  public void writeDocument(final Document document) {
    calcuateWidths(document);
    for (final Paragraph paragraph : document.getParagraphs()) {
      writeParagraph(paragraph);
    }
  }

  public void writeParagraph(final Paragraph paragraph) {
    try {
      if (!init) {
        init(paragraph.getAttributeIndex());
      }
      String paragraphId = paragraph.getId();
      if (paragraphId == null) {
        paragraphId = "";
      }
      final String header = "-DOCSTART FILE " + paragraphId;
      ow.write(header, 0, header.length());
      ow.newLine();
      for (final Sentence sentence : paragraph.getSentences()) {
        writeSentence(sentence);
      }
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  private void writeSentence(final Sentence sentence) throws IOException {
    final List<Token> tokens = sentence.getTokens();
    for (int i = 0; i < tokens.size(); i++) {
      writeToken(i, tokens.get(i), sentence);
    }
    ow.newLine();
  }

  private void writeToken(final int idx, final Token token, final Sentence sentence)
      throws IOException {
    String line = "";
    for (int i = 0; i < sentence.getAttributeIndex().getLength(); i++) {
      line += (line.length() > 0 ? " " : "");
      if (widths.size() > i) {
        line += String.format("%-" + widths.get(i) + "s", token.getAttributeValue(i));
      } else {
        line += token.getAttributeValue(i);
      }
    }

    line += " " + sentence.getTokenClassLabel(idx, null);
    ow.write(line, 0, line.length());
    ow.newLine();
  }

  private void calcuateWidths(final Document document) {
    widths.clear();
    for (int i = 0; i < document.getAttributeIndex().getLength(); i++) {
      widths.add(0);
    }
    for (final Paragraph paragraph : document.getParagraphs()) {
      for (final Sentence sentence : paragraph.getSentences()) {
        for (final Token token : sentence.getTokens()) {
          for (int i = 0; i < widths.size(); i++) {
            widths.set(i, Math.max(widths.get(i), ("" + token.getAttributeValue(i)).length()));
          }
        }
      }
    }
  }

}
