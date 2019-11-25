package g419.corpus.io.reader.parser.tei;

import g419.corpus.io.DataFormatException;
import g419.corpus.io.Tei;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.InputStream;

/**
 *
 */
public class TeiAnnotationWordsSAXParser extends TeiAnnotationSAXParser {

  public TeiAnnotationWordsSAXParser(final String filename,
                                     final InputStream is,
                                     final String group,
                                     final TeiDocumentElements elements) throws DataFormatException {
    super(filename, is, group, elements);
  }

  @Override
  public void startElement(final String s, final String s1, final String elementName, final Attributes attributes) throws SAXException {
    tmpValue = "";
    if (elementName.equalsIgnoreCase(Tei.TAG_PARAGRAPH)) {
      currentParagraph = elements.getParagraphs().get(currentParagraphIdx++);
      currentSentenceIdx = 0;
    } else if (elementName.equalsIgnoreCase(Tei.TAG_SENTENCE)) {
      currentSentence = currentParagraph.getSentences().get(currentSentenceIdx++);
    } else if (elementName.equalsIgnoreCase(Tei.TAG_SEGMENT)) {
      currentGroup = new SentenceGroup();
      currentGroup.setSentence(currentSentence);
      currentGroup.setGroupId(absPtr(attributes.getValue(Tei.ATTR_XMLID)));
      elements.getElementsIdMap().put(absPtr(currentGroup.getGroupId()), currentGroup.getTokens());
    } else if (elementName.equalsIgnoreCase(Tei.TAG_FEATURE)) {
      currentFeatureName = attributes.getValue("name");
    } else if (elementName.equalsIgnoreCase(Tei.TAG_SYMBOL)) {
      if (currentFeatureName.equals("ctag")) {
        currentGroup.setType(attributes.getValue("value"));
      }
    } else if (elementName.equalsIgnoreCase(Tei.TAG_POINTER)) {
      final String target = attributes.getValue("target");
      final String type = attributes.getValue("type");
      currentGroup.getTokens().add(absPtr(target));
      if ("head".equals(type) || "semh".equals(type)) {
        elements.getHeadIds().put(currentGroup.getGroupId(), absPtr(target));
      }
    }
  }

}
