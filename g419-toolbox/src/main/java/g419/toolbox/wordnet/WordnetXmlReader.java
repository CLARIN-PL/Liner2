package g419.toolbox.wordnet;

import g419.toolbox.wordnet.struct.LexicalRelation;
import g419.toolbox.wordnet.struct.LexicalUnit;
import g419.toolbox.wordnet.struct.Synset;
import g419.toolbox.wordnet.struct.WordnetPl;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Klasa do wczytania wordnetu z pliku XML.
 *
 * @author czuk
 */
public class WordnetXmlReader extends DefaultHandler {

  private final String TAG_LEXICAL_UNIT = "lexical-unit";
  private final String TAG_LEXICAL_RELATION = "lexicalrelations";
  private final String TAG_SYNSET_RELATION = "synsetrelations";
  private final String TAG_SYNSET = "synset";
  private final String TAG_UNIT_ID = "unit-id";

  private final String ATTR_LEXICAL_UNIT_ID = "id";
  private final String ATTR_LEXICAL_UNIT_NAME = "name";
  private final String ATTR_LEXICAL_UNIT_POS = "pos";
  private final String ATTR_LEXICAL_UNIT_DOMAIN = "domain";
  private final String ATTR_LEXICAL_UNIT_DESC = "desc";
  private final String ATTR_LEXICAL_UNIT_WORKSTATE = "workstate";
  private final String ATTR_LEXICAL_UNIT_SOURCE = "source";
  private final String ATTR_LEXICAL_UNIT_VARIANT = "variant";

  private final String ATTR_SYNSET_ID = "id";
  private final String ATTR_SYNSET_WORKSTATE = "workstate";
  private final String ATTR_SYNSET_SPLIT = "split";
  private final String ATTR_SYNSET_OWNER = "owner";
  private final String ATTR_SYNSET_DEFINITION = "definition";
  private final String ATTR_SYNSET_DESC = "desc";
  private final String ATTR_SYNSET_ABSTRACT = "abstract";

  private final String ATTR_SYNSET_RELATION_PARENT = "parent";
  private final String ATTR_SYNSET_RELATION_CHILD = "child";
  private final String ATTR_SYNSET_RELATION_RELATION = "relation";

  private final String ATTR_LEXICAL_RELATION_PARENT = "parent";
  private final String ATTR_LEXICAL_RELATION_CHILD = "child";
  private final String ATTR_LEXICAL_RELATION_RELATION = "relation";


  StringBuilder tmpValue = new StringBuilder();
  WordnetPl wordnet;
  Synset currentSynset = null;

  public static WordnetPl load(final InputStream is) throws IOException, SAXException,
      ParserConfigurationException {
    final WordnetPl wordnet = new WordnetPl();
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser parser = factory.newSAXParser();
    parser.parse(is, new WordnetXmlReader(wordnet));
    return wordnet;
  }

  public static WordnetPl load(final String filename) throws IOException, SAXException,
      ParserConfigurationException {
    final WordnetPl wordnet;
    try (final InputStream is = new FileInputStream(filename)) {
      wordnet = load(is);
    }
    return wordnet;
  }

  public WordnetXmlReader(final WordnetPl wordnet) {
    this.wordnet = wordnet;
  }

  @Override
  public InputSource resolveEntity(final String publicId, final String systemId) {
    return new InputSource(new StringReader(""));
  }

  @Override
  public void startElement(final String s,
                           final String s1,
                           final String elementName,
                           final Attributes attributes) throws SAXException {
    if (tmpValue.length() > 0) {
      tmpValue = new StringBuilder();
    }

    if (TAG_LEXICAL_UNIT.equalsIgnoreCase(elementName)) {
      wordnet.addLexicalUnit(parseLexicalUnit(attributes));
    } else if (TAG_SYNSET.equalsIgnoreCase(elementName)) {
      wordnet.addSynset(parseSynset(attributes));
    } else if (TAG_LEXICAL_RELATION.equalsIgnoreCase(elementName)) {
      wordnet.addLexicalRelation(parseLexicalRelation(attributes));
    } else if (TAG_SYNSET_RELATION.equalsIgnoreCase(elementName)) {
      parseSynsetRelation(attributes);
    }
  }

  private LexicalUnit parseLexicalUnit(final Attributes attributes) {
    final int id = Integer.parseInt(attributes.getValue(ATTR_LEXICAL_UNIT_ID));
    final String name = attributes.getValue(ATTR_LEXICAL_UNIT_NAME);
    final LexicalUnit unit = new LexicalUnit(id, name);
    unit.setDescription(attributes.getValue(ATTR_LEXICAL_UNIT_DESC));
    unit.setPos(attributes.getValue(ATTR_LEXICAL_UNIT_POS));
    unit.setDomain(attributes.getValue(ATTR_LEXICAL_UNIT_DOMAIN));
    unit.setSource(attributes.getValue(ATTR_LEXICAL_UNIT_SOURCE));
    unit.setVariant(Integer.parseInt(attributes.getValue(ATTR_LEXICAL_UNIT_VARIANT)));
    unit.setWorkstate(attributes.getValue(ATTR_LEXICAL_UNIT_WORKSTATE));
    return unit;
  }

  private Synset parseSynset(final Attributes attributes) {
    final int id = Integer.parseInt(attributes.getValue(ATTR_SYNSET_ID));
    final Synset synset = new Synset(id);
    currentSynset = synset;
    synset.setAbstract(Boolean.parseBoolean(attributes.getValue(ATTR_SYNSET_ABSTRACT)));
    synset.setDefinition(attributes.getValue(ATTR_SYNSET_DEFINITION));
    synset.setDescription(attributes.getValue(ATTR_SYNSET_DESC));
    synset.setOwner(attributes.getValue(ATTR_SYNSET_OWNER));
    synset.setSplit(Integer.parseInt(attributes.getValue(ATTR_SYNSET_SPLIT)));
    synset.setWorkstate(attributes.getValue(ATTR_SYNSET_WORKSTATE));
    return synset;
  }

  private LexicalRelation parseLexicalRelation(final Attributes attributes) {
    final int parent = Integer.parseInt(attributes.getValue(ATTR_LEXICAL_RELATION_PARENT));
    final int child = Integer.parseInt(attributes.getValue(ATTR_LEXICAL_RELATION_CHILD));
    final String relation = attributes.getValue(ATTR_LEXICAL_RELATION_RELATION);

    final LexicalUnit parentUnit = wordnet.getLexicalUnit(parent);
    final LexicalUnit childUnit = wordnet.getLexicalUnit(child);

    return new LexicalRelation(parentUnit, childUnit, relation);
  }

  private void parseSynsetRelation(final Attributes attributes) {
    final int parentId = Integer.parseInt(attributes.getValue(ATTR_LEXICAL_RELATION_PARENT));
    final int childId = Integer.parseInt(attributes.getValue(ATTR_LEXICAL_RELATION_CHILD));
    final int relation = Integer.parseInt(attributes.getValue(ATTR_LEXICAL_RELATION_RELATION));
    final Synset parent = wordnet.getSynset(parentId);
    final Synset child = wordnet.getSynset(childId);
    wordnet.addSynsetRelation(relation, child, parent);
  }

  @Override
  public void endElement(final String s, final String s1, final String elementName) throws SAXException {
    if (TAG_UNIT_ID.equalsIgnoreCase(elementName)) {
      final Integer luId = Integer.parseInt(tmpValue.toString());
      currentSynset.getLexicalUnits().add(wordnet.getLexicalUnit(luId));
    }
  }


  @Override
  public void characters(final char[] ac, final int start, final int length) throws SAXException {
    tmpValue.append(ac, start, length);
  }

}
