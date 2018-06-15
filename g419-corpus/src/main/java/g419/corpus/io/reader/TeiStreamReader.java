package g419.corpus.io.reader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.io.DataFormatException;
import g419.corpus.io.reader.parser.tei.*;
import g419.corpus.structure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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
            final String docName) throws DataFormatException {

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

        RelationSet relationSet = new RelationSet();

        // ToDo: Sprawdzenie, czy poszczególne inputstream nie są nullem
        final AnnMorphosyntaxSAXParser morphoParser = new AnnMorphosyntaxSAXParser(docName, annMorphosyntax, this.attributeIndex);
        final AnnSegmentationSAXParser segmentationParser = new AnnSegmentationSAXParser(annSegmentation, morphoParser.getParagraphs());
        final Map<String, Annotation> idToAnnotation = Maps.newHashMap();
        AnnWordsSAXParser wordsParser = null;

        /* Read words from the ann_words.xml file */
        if (annWords != null) {
            wordsParser = new AnnWordsSAXParser(docName, annWords, segmentationParser.getParagraphs(), morphoParser.getTokenIdsMap());
        }

        /* Read names from the ann_names.xml file */
        if (annNamed != null) {
            final AnnAnnotationsSAXParser namedParser = new AnnAnnotationsSAXParser(annNamed, segmentationParser.getParagraphs(),
                    morphoParser.getTokenIdsMap(), "ann_named.xml", "named");
            idToAnnotation.putAll(namedParser.getAnnotaitonMap());
        }

        /* Read groups from the ann_groups.xml file */
        if (annGroups != null && wordsParser != null) {
            new AnnGroupsSAXParser(annGroups, segmentationParser.getParagraphs(),
                    morphoParser.getTokenIdsMap(), wordsParser.getWordsIdsMap());
        }

        if (annMentions != null) {
            final AnnAnnotationsSAXParser mentionParser = new AnnAnnotationsSAXParser(annMentions, segmentationParser.getParagraphs(),
                    morphoParser.getTokenIdsMap(), "ann_mentions.xml", "mentions");
            idToAnnotation.putAll(mentionParser.getAnnotaitonMap());
        }

        if (annChunks != null) {
            final AnnAnnotationsSAXParser chunksParser = new AnnAnnotationsSAXParser(annChunks, segmentationParser.getParagraphs(),
                    morphoParser.getTokenIdsMap(),"ann_chunks.xml", "chunks");
            idToAnnotation.putAll(chunksParser.getAnnotaitonMap());
        }

        if (annAnnotations != null) {
            final AnnAnnotationsSAXParser annotationsParser = new AnnAnnotationsSAXParser(annAnnotations, segmentationParser.getParagraphs(),
                    morphoParser.getTokenIdsMap(), "ann_annotations.xml", "other");
            idToAnnotation.putAll(annotationsParser.getAnnotaitonMap());
        }

        if (annCoreference != null) {
            //AnnCoreferenceSAXParser coreferenceParser = new AnnCoreferenceSAXParser(annCoreference, mentionsParser.getParagraphs(), mentionsParser.getAnnotaitonMap());
        }

        if (annRelations != null) {
            final AnnRelationsSAXParser relationParser = new AnnRelationsSAXParser(annRelations, idToAnnotation);
            relationSet.getRelations().addAll(relationParser.getRelations());
        }

        document = new Document(docName, segmentationParser.getParagraphs(), this.attributeIndex, relationSet);
        document.setUri(uri);
        document.getSentences().stream().forEach(s->s.setDocument(document));

        if (annProps != null) {
            try {
                AnnPropsSAXParser propsParser = new AnnPropsSAXParser(annProps);
                final Map<String, Map<String, String>> props = propsParser.getProps();
                for (Paragraph p : document.getParagraphs()) {
                    for (Sentence s : p.getSentences()) {
                        for (Token t : s.getTokens()) {
                            Optional<Map<String, String>> tokenProps = Optional.ofNullable(props.get(t.getId()));
                            tokenProps.ifPresent(tp -> tp.entrySet().stream().forEach(e -> t.setProp(e.getKey(), e.getValue())));
                        }
                    }
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (annMetadata != null) {
            try {
                (new AnnMetadataSAXParser(annMetadata)).getMetadata().entrySet().stream()
                        .forEach(e -> document.getDocumentDescriptor().setMetadata(e.getKey(), e.getValue()));
            } catch (Exception ex) {
                logger.error("Failed to read TEI metadata file", ex);
            }
        }

    }

    @Override
    public TokenAttributeIndex getAttributeIndex() {
        return this.attributeIndex;
    }

    @Override
    public void close() {
        streams.stream().filter(s -> s != null).forEach(this::close);
    }

    private void close(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            logger.error("Unable to close stream", e);
        }
    }

    @Override
    public Document nextDocument() throws DataFormatException, IOException {
        Document doc = this.document;
        this.document = null;
        return doc;
    }

    @Override
    public boolean hasNext() {
        return document != null;
    }

}
