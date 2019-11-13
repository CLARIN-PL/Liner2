package g419.corpus.io.reader.parser.tei;

import g419.corpus.io.Tei;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class TeiSegmentationSAXParser extends DefaultHandler {

  final TeiDocumentElements elements;

  Token currentToken;
  Sentence currentSentence;
  Paragraph currentParagraph;
  int currentTokenIdx;
  int currentParagraphIdx;
  int currentSentenceIdx;

  public TeiSegmentationSAXParser(final InputStream is,
                                  final TeiDocumentElements elements)
      throws IOException, SAXException, ParserConfigurationException {
    this.elements = elements;
    currentSentence = elements.getParagraphs().get(0).getSentences().get(0);
    SAXParserFactory.newInstance().newSAXParser().parse(is, this);
  }

  @Override
  public InputSource resolveEntity(final String publicId, final String systemId) {
    return new InputSource(new StringReader(""));
  }

  @Override
  public void startElement(final String s, final String s1, final String elementName, final Attributes attributes) throws SAXException {
    if (elementName.equalsIgnoreCase(Tei.TAG_PARAGRAPH)) {
      currentParagraph = elements.getParagraphs().get(currentParagraphIdx++);
      currentSentenceIdx = 0;
    } else if (elementName.equalsIgnoreCase(Tei.TAG_SENTENCE)) {
      currentSentence = currentParagraph.getSentences().get(currentSentenceIdx++);
      currentTokenIdx = 0;
    } else if (elementName.equalsIgnoreCase(Tei.TAG_SEGMENT) && !Tei.VAL_TRUE.equals(attributes.getValue(Tei.ATTR_REJECTED))) {
      if (attributes.getValue("nkjp:nps") != null) {
        currentToken.setNoSpaceAfter(true);
      }
      currentToken = currentSentence.getTokens().get(currentTokenIdx++);
    }
  }

  @Override
  public void endElement(final String s, final String s1, final String element) throws SAXException {
  }

}
