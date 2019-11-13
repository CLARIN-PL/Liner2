package g419.corpus.io.reader;

import com.google.common.collect.Lists;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.Tei;
import g419.corpus.io.reader.parser.tei.*;
import g419.corpus.structure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SAX reader for a document in TEI (NKJP) format.
 */
public class TeiStreamReader extends AbstractDocumentReader {

  final private TokenAttributeIndex attributeIndex;
  final private List<InputStream> streams = Lists.newArrayList();
  Logger logger = LoggerFactory.getLogger(getClass());
  private Document document;

  public TeiStreamReader(
      final String uri,
      final InputStream annMetadata,
      final InputStream annMorphosyntax,
      final InputStream annProps,
      final InputStream annSegmentation,
      final InputStream annNamed,
      final InputStream annMentions,
      final InputStream annChunks,
      final InputStream annAnnotations,
      final InputStream annCoreference,
      final InputStream annWords,
      final InputStream annGroups,
      final InputStream annRelations,
      final String docName) throws DataFormatException, Exception {

    streams.add(annMetadata);
    streams.add(annMorphosyntax);
    streams.add(annProps);
    streams.add(annSegmentation);
    streams.add(annNamed);
    streams.add(annMentions);
    streams.add(annChunks);
    streams.add(annAnnotations);
    streams.add(annCoreference);
    streams.add(annWords);
    streams.add(annGroups);
    streams.add(annRelations);

    attributeIndex = new TokenAttributeIndex().with("orth").with("base").with("ctag");
    // TODO dodanie tego atrybutu "psuje" kolejność atrybutów
    //this.attributeIndex.addAttribute("tagTool");

    final RelationSet relationSet = new RelationSet();

    final TeiDocumentElements elements = new TeiDocumentElements();

    // ToDo: Sprawdzenie, czy poszczególne inputstream nie są nullem
    new TeiMorphosyntaxSAXParser(docName, annMorphosyntax, attributeIndex, elements);
    new TeiSegmentationSAXParser(annSegmentation, elements);

    final TeiPropsSAXParser propsParser;

    if (annWords != null) {
      new TeiAnnotationWordsSAXParser("ann_words.xml", annWords, "word", elements);
    }

    if (annNamed != null) {
      new TeiAnnotationSAXParser("ann_named.xml", annNamed, Tei.ANNOTATION_GROUP_NAMED, elements);
    }

    if (annGroups != null) {
      new TeiAnnotationSAXParser("ann_groups.xml", annGroups, "group", elements);
    }

    if (annMentions != null) {
      new TeiAnnotationSAXParser("ann_mentions.xml", annMentions, "mentions", elements);
    }

    if (annChunks != null) {
      new TeiAnnotationSAXParser("ann_chunks.xml", annChunks, "chunks", elements);
    }

    if (annAnnotations != null) {
      new TeiAnnotationSAXParser("ann_annotations.xml", annAnnotations, "other", elements);
    }

    if (annCoreference != null) {
      //TeiCoreferenceSAXParser coreferenceParser = new TeiCoreferenceSAXParser(annCoreference, mentionsParser.getParagraphs(), mentionsParser.getAnnotaitonMap());
    }

    if (annRelations != null) {
      new TeiRelationsSAXParser(annRelations, elements);
      relationSet.getRelations().addAll(elements.getRelations());
    }

    document = new Document(docName, elements.getParagraphs(), attributeIndex, relationSet);
    document.setUri(uri);
    document.getSentences().stream().forEach(s -> s.setDocument(document));

    if (annProps != null) {
      try {
        propsParser = new TeiPropsSAXParser(annProps);
        final Map<String, Map<String, String>> props = propsParser.getProps();
        for (final Paragraph p : document.getParagraphs()) {
          for (final Sentence s : p.getSentences()) {
            for (final Token t : s.getTokens()) {
              final Optional<Map<String, String>> tokenProps = Optional.ofNullable(props.get(t.getId()));
              tokenProps.ifPresent(tp -> tp.entrySet().stream().forEach(e -> t.setProp(e.getKey(), e.getValue())));
            }
          }
        }
      } catch (final Exception e) {
        e.printStackTrace();
      }
    }

    if (annMetadata != null) {
      try {
        (new TeiMetadataSAXParser(annMetadata)).getMetadata().entrySet().stream()
            .forEach(e -> document.getDocumentDescriptor().setMetadata(e.getKey(), e.getValue()));
      } catch (final Exception ex) {
        logger.error("Failed to read TEI metadata file", ex);
      }
    }

  }

  @Override
  public TokenAttributeIndex getAttributeIndex() {
    return attributeIndex;
  }

  @Override
  public void close() {
    streams.stream().filter(s -> s != null).forEach(this::close);
  }

  private void close(final InputStream stream) {
    try {
      stream.close();
    } catch (final IOException e) {
      logger.error("Unable to close stream", e);
    }
  }

  @Override
  public Document nextDocument() throws DataFormatException, IOException {
    final Document doc = document;
    document = null;
    return doc;
  }

  @Override
  public boolean hasNext() {
    return document != null;
  }

}
