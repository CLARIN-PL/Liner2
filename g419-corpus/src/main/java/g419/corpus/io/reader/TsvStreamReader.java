package g419.corpus.io.reader;

import com.google.common.collect.Maps;
import g419.corpus.io.DataFormatException;
import g419.corpus.structure.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;


public class TsvStreamReader extends AbstractDocumentReader {
  static private final Pattern annotationPattern = Pattern.compile("([IB])-([^#]*)");
  private final BufferedReader stream;
  private TokenAttributeIndex attributeIndex = null;
  private String nextParagraphId = null;
  private Document documentBuffor;

  public TsvStreamReader(final InputStream is) {
    stream = new BufferedReader(new InputStreamReader(is));
    attributeIndex = new TokenAttributeIndex();
    attributeIndex.addAttribute("orth");
    documentBuffor = readNextDocument();
  }

  @Override
  public void close() {
    try {
      stream.close();
    } catch (final IOException ex) {
      LoggerFactory.getLogger(getClass()).error("Failed to close the input stream", ex);
    }
  }

  @Override
  protected TokenAttributeIndex getAttributeIndex() {
    return attributeIndex;
  }

  @Override
  public Document nextDocument() throws DataFormatException {
    final Document document = documentBuffor;
    documentBuffor = readNextDocument();
    return document;
  }

  private Document readNextDocument() {
    String line;
    final TokenAttributeIndex index = attributeIndex.clone();

    final Paragraph paragraph = new Paragraph(nextParagraphId);
    paragraph.setAttributeIndex(index);
    nextParagraphId = null;
    Sentence currentSentence = new Sentence();
    HashMap<String, Annotation> annsByType = new HashMap<>();

    try {
      while ((line = stream.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0) {
          if (currentSentence.getTokenNumber() > 0) {
            currentSentence.setId("sent" + paragraph.numSentences() + 1);
            paragraph.addSentence(currentSentence);
            currentSentence = new Sentence();
            annsByType = Maps.newHashMap();
          }
        } else {
          final String[] columns = line.split("\t");
          currentSentence.addToken(new Token(columns[0], new Tag("base", "ctag", false), index));
          final String label = columns[columns.length - 1];
          if (label.equals("O")) {
            annsByType = Maps.newHashMap();
          } else {
            final Matcher m = annotationPattern.matcher(label);
            final int idx = currentSentence.getTokenNumber() - 1;
            while (m.find()) {
              final String annType = m.group(2);
              if (m.group(1).equals("B")) {
                final Annotation newAnn = new Annotation(idx, annType, currentSentence);
                currentSentence.addChunk(newAnn);
                annsByType.put(annType, newAnn);
              } else if (m.group(1).equals("I") && annsByType.containsKey(annType)) {
                annsByType.get(annType).addToken(idx);
              }
            }
          }

        }
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }

    if (currentSentence.getTokenNumber() > 0) {
      currentSentence.setId("sent" + paragraph.numSentences() + 1);
      paragraph.addSentence(currentSentence);
    }
    return paragraph.getSentences().size() == 0 ? null : new Document("csv", Arrays.asList(paragraph), index);
  }

  @Override
  public boolean hasNext() {
    return documentBuffor != null;
  }
}
