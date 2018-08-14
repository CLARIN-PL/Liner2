package g419.corpus.io.reader.parser.tei;

import com.google.common.collect.Lists;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.Tei;
import g419.corpus.structure.Paragraph;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 */
public class TeiAnnotationWordsSAXParser extends TeiAnnotationSAXParser {

    public TeiAnnotationWordsSAXParser(final String filename, final InputStream is, final List<Paragraph> paragraphs,
                                       final Map<String, Integer> tokensIdMap, final Map<String, List<String>> elementsIdMap, final String group) throws DataFormatException {
        super(filename, is, paragraphs, tokensIdMap, elementsIdMap, group);
    }

    @Override
    public void startElement(final String s, final String s1, final String elementName, final Attributes attributes) throws SAXException {
        tmpValue = "";
        if (elementName.equalsIgnoreCase(Tei.TAG_PARAGRAPH)) {
            currentParagraph = paragraphs.get(currentParagraphIdx++);
            currentSentenceIdx = 0;
        } else if (elementName.equalsIgnoreCase(Tei.TAG_SENTENCE)) {
            currentSentence = currentParagraph.getSentences().get(currentSentenceIdx++);
        } else if (elementName.equalsIgnoreCase(Tei.TAG_SEGMENT)) {
            currentGroupId = attributes.getValue(Tei.ATTR_XMLID);
            currentGroupTokens = Lists.newLinkedList();
            currentAnnotationType = null;
            currentAnnotationBase = null;
            elementsIdMap.put(absPtr(currentGroupId), currentGroupTokens);
        } else if (elementName.equalsIgnoreCase(Tei.TAG_FEATURE)) {
            currentFeatureName = attributes.getValue("name");
        } else if (elementName.equalsIgnoreCase(Tei.TAG_SYMBOL)) {
            if (currentFeatureName.equals("ctag")) {
                currentAnnotationType = attributes.getValue("value");
            }
        } else if (elementName.equalsIgnoreCase(Tei.TAG_POINTER)) {
            final String target = attributes.getValue("target");
            final String type = attributes.getValue("type");
            currentGroupTokens.add(absPtr(target));
            if ("head".equals(type) || "semh".equals(type)) {
                headIds.put(currentGroupId, target);
            }
        }
    }

}
