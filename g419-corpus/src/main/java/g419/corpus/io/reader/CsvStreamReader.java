package g419.corpus.io.reader;

import com.google.common.collect.Maps;
import g419.corpus.io.DataFormatException;
import g419.corpus.structure.*;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CsvStreamReader extends AbstractDocumentReader {
  static private Pattern annotationPattern = Pattern.compile("([IB])-([^#]*)");
  private BufferedReader stream;
  private TokenAttributeIndex attributeIndex = null;
  private String nextParagraphId = null;
  private Document documentBuffor;

  public CsvStreamReader(InputStream is) {
    stream = new BufferedReader(new InputStreamReader(is));
    attributeIndex = new TokenAttributeIndex();
    attributeIndex.addAttribute("orth");
    documentBuffor = readNextDocument();
  }

  @Override
  public void close() {
    try {
      stream.close();
    } catch (IOException ex) {
      LoggerFactory.getLogger(getClass()).error("Failed to close the input stream", ex);
    }
  }

  @Override
  protected TokenAttributeIndex getAttributeIndex() {
    return this.attributeIndex;
  }

  @Override
  public Document nextDocument() throws DataFormatException {
    Document document = documentBuffor;
    documentBuffor = readNextDocument();
    return document;
  }

  private Document readNextDocument() {
    String line;
    TokenAttributeIndex index = this.attributeIndex.clone();

    Paragraph paragraph = new Paragraph(nextParagraphId);
    paragraph.setAttributeIndex(index);
    this.nextParagraphId = null;
    Sentence currentSentence = new Sentence();
    HashMap<String, Annotation> annsByType = new HashMap<String, Annotation>();

    try {
      while ((line = this.stream.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0) {
          if (currentSentence.getTokenNumber() > 0) {
            currentSentence.setId("sent" + paragraph.numSentences() + 1);
            paragraph.addSentence(currentSentence);
            currentSentence = new Sentence();
            annsByType = Maps.newHashMap();
          }
        } else {
          String[] columns = line.split(" ");
          currentSentence.addToken(new Token(columns[0], new Tag("base", "ctag", false), index));
          String label = columns[columns.length - 1];
          if (label.equals("O")) {
            annsByType = Maps.newHashMap();
          } else {
            Matcher m = annotationPattern.matcher(label);
            int idx = currentSentence.getTokenNumber() - 1;
            while (m.find()) {
              String annType = m.group(2);
              if (m.group(1).equals("B")) {
                Annotation newAnn = new Annotation(idx, annType, currentSentence);
                currentSentence.addChunk(newAnn);
                annsByType.put(annType, newAnn);
              } else if (m.group(1).equals("I") && annsByType.containsKey(annType)) {
                annsByType.get(annType).addToken(idx);
              }
            }
          }

        }
      }
    } catch (IOException e) {
      // ToDo: Use logger to log the exception
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
