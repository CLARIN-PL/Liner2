package g419.corpus.io.reader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.Tei;
import g419.corpus.io.reader.parser.tei.*;
import g419.corpus.structure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    attributeIndex = new TokenAttributeIndex();
    attributeIndex.addAttribute("orth");
    attributeIndex.addAttribute("base");
    attributeIndex.addAttribute("ctag");
    // TODO dodanie tego atrybutu "psuje" kolejność atrybutów
    //this.attributeIndex.addAttribute("tagTool");

    final RelationSet relationSet = new RelationSet();

    // ToDo: Sprawdzenie, czy poszczególne inputstream nie są nullem
    final TeiMorphosyntaxSAXParser morphoParser = new TeiMorphosyntaxSAXParser(docName, annMorphosyntax, attributeIndex);
    final TeiSegmentationSAXParser segmentationParser = new TeiSegmentationSAXParser(annSegmentation, morphoParser.getParagraphs());
    final TeiPropsSAXParser propsParser;
    final Map<String, List<String>> globalElementIndex = Maps.newHashMap();

    /* Read words from the ann_words.xml file */
    if (annWords != null) {
      new TeiAnnotationWordsSAXParser("ann_words.xml", annWords, segmentationParser.getParagraphs(),
          morphoParser.getTokenIdsMap(), globalElementIndex, "word");
    }

    /* Read names from the ann_names.xml file */
    if (annNamed != null) {
      new TeiAnnotationSAXParser("ann_named.xml", annNamed, segmentationParser.getParagraphs(),
          morphoParser.getTokenIdsMap(), globalElementIndex, Tei.ANNOTATION_GROUP_NAMED);
    }

    /* Read groups from the ann_groups.xml file */
    if (annGroups != null) {
      new TeiAnnotationSAXParser("ann_groups.xml", annGroups, segmentationParser.getParagraphs(),
          morphoParser.getTokenIdsMap(), globalElementIndex, "group");
    }

    if (annMentions != null) {
      new TeiAnnotationSAXParser("ann_mentions.xml", annMentions, segmentationParser.getParagraphs(),
          morphoParser.getTokenIdsMap(), globalElementIndex, "mentions");
    }

    if (annChunks != null) {
      new TeiAnnotationSAXParser("ann_chunks.xml", annChunks, segmentationParser.getParagraphs(),
          morphoParser.getTokenIdsMap(), globalElementIndex, "chunks");
    }

    if (annAnnotations != null) {
      new TeiAnnotationSAXParser("ann_annotations.xml", annAnnotations, segmentationParser.getParagraphs(),
          morphoParser.getTokenIdsMap(), globalElementIndex, "other");
    }

    if (annCoreference != null) {
      //TeiCoreferenceSAXParser coreferenceParser = new TeiCoreferenceSAXParser(annCoreference, mentionsParser.getParagraphs(), mentionsParser.getAnnotaitonMap());
    }

    final Map<String, Annotation> idToAnnotation = segmentationParser.getParagraphs().stream()
        .map(Paragraph::getSentences)
        .flatMap(Collection::stream)
        .map(Sentence::getChunks)
        .flatMap(Collection::stream)
        .collect(Collectors.toMap(Annotation::getId, Function.identity()));

    if (annRelations != null) {
      final TeiRelationsSAXParser relationParser = new TeiRelationsSAXParser(annRelations, idToAnnotation);
      relationSet.getRelations().addAll(relationParser.getRelations());
    }

    document = new Document(docName, segmentationParser.getParagraphs(), attributeIndex, relationSet);
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
