package g419.corpus.io.reader.parser.tei;

import com.google.common.collect.Lists;
import g419.corpus.HasLogger;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.Tei;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

public class TeiAnnotationSAXParser extends DefaultHandler implements HasLogger {

    @Data
    @NoArgsConstructor
    class SentenceGroup {
        private String groupId = null;
        private Sentence sentence = null;
        private String type = null;
        private String base = null;
        private List<String> tokens = Lists.newArrayList();
        private String head = null;
    }

    final String filename;
    final String group;

    protected int currentParagraphIdx = 0;
    protected int currentSentenceIdx = 0;
    protected Paragraph currentParagraph = null;
    protected Sentence currentSentence = null;
    protected String currentFeatureName = null;
    String tmpValue = "";

    protected SentenceGroup currentGroup;

    final List<SentenceGroup> groups = Lists.newLinkedList();
    final TeiDocumentElements elements;

    public TeiAnnotationSAXParser(final String filename,
                                  final InputStream is,
                                  final String group,
                                  final TeiDocumentElements elements
    )
            throws DataFormatException {
        this.filename = filename;
        this.group = group;
        this.elements = elements;

        try {
            SAXParserFactory.newInstance().newSAXParser().parse(is, this);
            final List<Annotation> annotations = createAnnotations();
            annotations.forEach(an -> an.getSentence().addChunk(an));
            annotations.forEach(an -> elements.addAnnotation(an.getId(), an));
        } catch (final Exception e) {
            throw new DataFormatException("TEI Parse error", e);
        }
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId) {
        return new InputSource(new StringReader(""));
    }

    @Override
    public void startElement(final String s, final String s1, final String elementName, final Attributes attributes) throws SAXException {
        tmpValue = "";
        switch (elementName.toLowerCase()) {
            case Tei.TAG_PARAGRAPH:
                currentParagraph = elements.getParagraphs().get(currentParagraphIdx++);
                currentSentenceIdx = 0;
                break;

            case Tei.TAG_SENTENCE:
                currentSentence = currentParagraph.getSentences().get(currentSentenceIdx++);
                break;

            case Tei.TAG_SEGMENT:
                currentGroup = new SentenceGroup();
                currentGroup.setSentence(currentSentence);
                currentGroup.setGroupId(absPtr(attributes.getValue("xml:id")));
                elements.getElementsIdMap().put(currentGroup.getGroupId(), currentGroup.getTokens());
                break;

            case Tei.TAG_FEATURE:
                currentFeatureName = attributes.getValue("name");
                break;

            case Tei.TAG_SYMBOL:
                if (currentFeatureName.equals("type")) {
                    currentGroup.setType(attributes.getValue("value"));
                } else if (currentFeatureName.equals("subtype")) {
                    currentGroup.setType(currentGroup.getType() + "-" + attributes.getValue("value"));
                }
                break;

            case Tei.TAG_POINTER:
                final String target = absPtr(attributes.getValue(Tei.ATTR_TARGET));
                final String type = attributes.getValue(Tei.ATTR_TYPE);
                currentGroup.getTokens().add(target);
                if ("head".equals(type) || "semh".equals(type)) {
                    currentGroup.setHead(target);
                    elements.getHeadIds().put(currentGroup.getGroupId(), target);
                }
                break;
        }
    }

    @Override
    public void endElement(final String s, final String s1, final String element) throws SAXException {
        switch (element) {
            case Tei.TAG_SEGMENT:
                groups.add(currentGroup);
                currentGroup = new SentenceGroup();
                break;

            case Tei.TAG_FEATURE:
                currentFeatureName = null;
                break;

            case Tei.TAG_STRING:
                if ("base".equals(currentFeatureName)) {
                    currentGroup.setBase(tmpValue);
                }
                break;
        }
    }

    protected String absPtr(final String pointer) {
        final String localPointer = pointer.startsWith("#") ? pointer.substring(1) : pointer;
        return localPointer.contains("#") ? localPointer : filename + "#" + localPointer;
    }

    /**
     * Assign token index to annotations after loading all groups.
     */
    private List<Annotation> createAnnotations() {
        final List<Annotation> annotations = Lists.newArrayList();
        for (final SentenceGroup group : groups) {
            final TreeSet<Integer> tokens = new TreeSet<>();
            for (final String elementKey : group.getTokens()) {
                final List<Integer> ids = elements.getTokens(elementKey);
                if (ids.size() == 0) {
                    LoggerFactory.getLogger(getClass()).warn("No tokens found for " + elementKey);
                } else {
                    tokens.addAll(ids);
                }
            }
            if (tokens.size() == 0) {
                getLogger().error("Annotation with id={} from file={} does not have defined tokens (no ptr elements)", group.getGroupId(), filename);
            } else if (group.getType() == null) {
                getLogger().error("Annotation with id={} from file={} has unknown type", group.getGroupId(), filename);
            } else {
                Optional<Integer> head = Optional.empty();
                if (group.getHead() != null) {
                    head = elements.getHeadToken(group.getHead());
                }
                final Annotation an = new Annotation(tokens, group.getType(), group.getSentence(), head)
                        .withGroup(this.group)
                        .withLemma(group.getBase())
                        .withId(group.getGroupId());
                annotations.add(an);
            }
        }
        return annotations;
    }

    @Override
    public void characters(final char[] ac, final int start, final int length) throws SAXException {
        for (int i = start; i < start + length; i++) {
            tmpValue += ac[i];
        }
    }

}
