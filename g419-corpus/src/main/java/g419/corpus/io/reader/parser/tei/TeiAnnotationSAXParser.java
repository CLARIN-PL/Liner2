package g419.corpus.io.reader.parser.tei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.HasLogger;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.Tei;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import io.vavr.control.Option;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TeiAnnotationSAXParser extends DefaultHandler implements HasLogger {

  final String filename;
  final String group;

  protected int currentParagraphIdx = 0;
  protected int currentSentenceIdx = 0;
  protected Paragraph currentParagraph = null;
  protected Sentence currentSentence = null;
  protected String currentGroupId = null;
  protected String currentAnnotationType = null;
  protected String currentAnnotationBase = null;
  protected String currentFeatureName = null;
  protected List<String> currentGroupTokens = null;
  String tmpValue = "";

  final Map<String, Integer> tokensIdMap;
  final Map<String, List<String>> elementsIdMap;
  final Map<String, String> headIds = Maps.newHashMap();
  final List<Paragraph> paragraphs;
  final List<SentenceGroup> groups = Lists.newLinkedList();

  public TeiAnnotationSAXParser(final String filename, final InputStream is, final List<Paragraph> paragraphs,
                                final Map<String, Integer> tokensIdMap, final Map<String, List<String>> elementsIdMap, final String group) throws DataFormatException {
    this.filename = filename;
    this.paragraphs = paragraphs;
    this.tokensIdMap = tokensIdMap;
    this.elementsIdMap = elementsIdMap;
    this.group = group;

    try {
      SAXParserFactory.newInstance().newSAXParser().parse(is, this);
      makeAnnotations();
    } catch (final ParserConfigurationException e) {
      throw new DataFormatException("Parse error (ParserConfigurationException)");
    } catch (final SAXException e) {
      throw new DataFormatException("Parse error (SAXException): " + e.getMessage());
    } catch (final IOException e) {
      throw new DataFormatException("Parse error (IOException)");
    }
  }

  @Override
  public InputSource resolveEntity(final String publicId, final String systemId) {
    return new InputSource(new StringReader(""));
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
      currentGroupId = attributes.getValue("xml:id");
      currentGroupTokens = Lists.newLinkedList();
      currentAnnotationType = null;
      currentAnnotationBase = null;
      elementsIdMap.put(filename + "#" + currentGroupId, currentGroupTokens);
    } else if (elementName.equalsIgnoreCase(Tei.TAG_FEATURE)) {
      currentFeatureName = attributes.getValue("name");
    } else if (elementName.equalsIgnoreCase(Tei.TAG_SYMBOL)) {
      if (currentFeatureName.equals("type")) {
        currentAnnotationType = attributes.getValue("value");
      } else if (currentFeatureName.equals("subtype")) {
        currentAnnotationType += "-" + attributes.getValue("value");
      }
    } else if (elementName.equalsIgnoreCase(Tei.TAG_POINTER)) {
      final String target = attributes.getValue(Tei.ATTR_TARGET);
      final String type = attributes.getValue(Tei.ATTR_TYPE);
      currentGroupTokens.add(target);
      if ("head".equals(type) || "semh".equals(type)) {
        headIds.put(currentGroupId, target);
      }
    }
  }

  @Override
  public void endElement(final String s, final String s1, final String element) throws SAXException {
    if (Tei.TAG_SEGMENT.equals(element)) {
      groups.add(new SentenceGroup(currentGroupId, currentSentence,
          currentAnnotationType, currentAnnotationBase, currentGroupTokens));
    } else if (Tei.TAG_STRING.equals(element) && "base".equals(currentFeatureName)) {
      currentAnnotationBase = tmpValue;
    } else if (element.equalsIgnoreCase(Tei.TAG_FEATURE)) {
      currentFeatureName = null;
    }
  }

  public List<Paragraph> getParagraphs() {
    return paragraphs;
  }

  protected String absPtr(final String pointer) {
    return pointer.contains("#") ? pointer : filename + "#" + pointer;
  }

  /**
   * Assign token index to annotations after loading all groups.
   */
  private void makeAnnotations() {
    for (final SentenceGroup group : groups) {
      final TreeSet<Integer> tokens = new TreeSet<>();
      for (final String elementKey : group.getKeys()) {
        final List<Integer> ids = getTokens(elementKey);
        if (ids.size() == 0) {
          LoggerFactory.getLogger(getClass()).warn("No tokens found for " + elementKey);
        } else {
          tokens.addAll(ids);
        }
      }
      if (tokens.size() == 0) {
        getLogger().error("Annotation with id={} from file={} does not have defined tokens (no ptr elements)", group.getGroupId(), filename);
      } else {
        final Annotation an = new Annotation(tokens, group.getType(), group.getSentence())
            .withGroup(this.group)
            .withHead(getHead(group.getGroupId()))
            .withLemma(group.getBase())
            .withId(group.getGroupId());
        group.getSentence().addChunk(an);
      }
    }
  }

  private List<Integer> getTokens(final String elementKey) {
    final String key = absPtr(elementKey);
    if (tokensIdMap.containsKey(key)) {
      return Lists.newArrayList(tokensIdMap.get(key));
    } else if (elementsIdMap.containsKey(key)) {
      return elementsIdMap.get(key).stream()
          .map(this::getTokens)
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    } else {
      LoggerFactory.getLogger(getClass()).warn("No element nor tokens for {}", key);
      return Lists.newArrayList();
    }
  }

  private Integer getHead(final String elementKey) {
    return Option.of(elementKey).map(k -> getTokens(k).get(0)).getOrElse(0);
  }

  @Override
  public void characters(final char[] ac, final int start, final int length) throws SAXException {
    for (int i = start; i < start + length; i++) {
      tmpValue += ac[i];
    }
  }

  class SentenceGroup {

    private final String groupId;
    private final Sentence sentence;
    private final String type;
    private final String base;
    private final List<String> keys;

    public SentenceGroup(final String groupId, final Sentence sentence, final String type, final String base, final List<String> keys) {
      this.groupId = groupId;
      this.sentence = sentence;
      this.base = base;
      this.type = type;
      this.keys = keys;
    }

    public String getGroupId() {
      return groupId;
    }

    public Sentence getSentence() {
      return sentence;
    }

    public String getType() {
      return type;
    }

    public String getBase() {
      return base;
    }

    public List<String> getKeys() {
      return keys;
    }

  }
}
