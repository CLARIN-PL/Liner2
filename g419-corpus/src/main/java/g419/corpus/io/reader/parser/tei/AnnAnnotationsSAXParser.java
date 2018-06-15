package g419.corpus.io.reader.parser.tei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.HasLogger;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.Tei;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import io.vavr.control.Option;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

public class AnnAnnotationsSAXParser extends DefaultHandler implements HasLogger {

    final List<Paragraph> paragraphs;
    final Map<String, Integer> tokenIdsMap;
    final Map<String, Annotation> annotationsMap = Maps.newHashMap();
    final String filename;
    final String annotationGroup;

    Paragraph currentParagraph;
    int currentParagraphIdx;
    Sentence currentSentence;
    int currentSentenceIdx;
    List<Integer> annotatedTokens;
    String annotationType;
    String currentFeatureName;
    String tagId;

    public AnnAnnotationsSAXParser(final InputStream is, final List<Paragraph> paragraphs, final Map<String, Integer> tokenIdsMap,
                                   final String filename, final String annotationGroup) throws DataFormatException {
        this.paragraphs = paragraphs;
        this.tokenIdsMap = tokenIdsMap;
        this.annotationGroup = annotationGroup;
        this.filename = filename;
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(is, this);
        } catch (ParserConfigurationException e) {
            throw new DataFormatException("Parse error (ParserConfigurationException)");
        } catch (SAXException e) {
            throw new DataFormatException("Parse error (SAXException): " + e.getMessage());
        } catch (IOException e) {
            throw new DataFormatException("Parse error (IOException)");
        }
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new StringReader(""));
    }

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
        if (elementName.equalsIgnoreCase(Tei.TAG_PARAGRAPH)) {
            currentParagraph = paragraphs.get(currentParagraphIdx++);
            currentSentenceIdx = 0;
        } else if (elementName.equalsIgnoreCase(Tei.TAG_SENTENCE)) {
            currentSentence = currentParagraph.getSentences().get(currentSentenceIdx++);
        } else if (elementName.equalsIgnoreCase(Tei.TAG_SEGMENT)) {
            annotatedTokens = Lists.newArrayList();
            annotationType = null;
            tagId = attributes.getValue(Tei.TAG_ID);
        } else if (elementName.equalsIgnoreCase(Tei.TAG_FEATURE)) {
            currentFeatureName = attributes.getValue("name");
        } else if (elementName.equalsIgnoreCase(Tei.TAG_SYMBOL)) {
            if (currentFeatureName.equals("type")) {
                annotationType = attributes.getValue("value");
            } else if (currentFeatureName.equals("subtype")) {
                annotationType += "-" + attributes.getValue("value");
            }
        } else if (elementName.equalsIgnoreCase(Tei.TAG_POINTER)) {
            final String target = attributes.getValue("target");
            final String[] parts = target.split("#");
            if ( parts.length != 2 ){
                getLogger().warn("Invalid target value: {}", target);
            } else {
                final Integer tokenIndex = tokenIdsMap.get(parts[1]);
            	Option.of(tokenIndex)
                    .peek(annotatedTokens::add)
                    .onEmpty(()->new SAXException("Token with id '" + target + "' not found"));
            }
        }
    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {

        if (element.equals(Tei.TAG_SEGMENT) && annotatedTokens.size() > 0) {
            Annotation an = new Annotation(Sets.newTreeSet(annotatedTokens), annotationType, currentSentence);
            an.setGroup(annotationGroup);
            an.setId(tagId);
            currentSentence.addChunk(an);
            annotationsMap.put(String.format("%s#%s", filename, tagId), an);
            Option.of(annotationType).onEmpty(()->getLogger().warn("[{}] Missing annotation type for {}", filename, an.toString()));
        } else if (element.equalsIgnoreCase(Tei.TAG_FEATURE)) {
            currentFeatureName = null;
        }
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public Map<String, Annotation> getAnnotaitonMap() {
        return annotationsMap;
    }

}
