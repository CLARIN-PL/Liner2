package g419.corpus.io.writer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.io.writer.tei.Interps;
import g419.corpus.io.writer.tei.TeiLex;
import g419.corpus.structure.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 */
public class TeiStreamWriter extends AbstractDocumentWriter {

    private static final Pattern hyphens = Pattern.compile("[-]+");
    private final String TAG_CORPUS = "teiCorpus";
    private final String TAG_TEI = "TEI";
    private final String TAG_TEXT = "text";
    private final String TAG_FRONT = "front";
    private final String TAG_TITLE = "docTitle";
    private final String TAG_TITLEPART = "titlePart";
    private final String TAG_BODY = "body";
    private final String TAG_DIV = "div";
    private final String TAG_PARAGRAPH = "p";
    private final String TAG_SENTENCE = "s";
    private final String TAG_SEGMENT = "seg";
    private final String TAG_FEATURESET = "fs";
    private final String TAG_FEATURE = "f";
    private final String TAG_STRING = "string";
    private final String TAG_SYMBOL = "symbol";
    private final String TAG_VALT = "vAlt";
    private final String TAG_POINTER = "ptr";
    private final String TAG_BINARY = "binary";
    private final String TAG_METADATA = "metadata";
    private final String ATTR_CORESP = "coresp";
    private final String ATTR_NAME = "name";
    final private Pair<OutputStream, XMLStreamWriter> oText;
    final private Pair<OutputStream, XMLStreamWriter> oMeta;
    final private Pair<OutputStream, XMLStreamWriter> oSegm;
    final private Pair<OutputStream, XMLStreamWriter> oMorph;
    final private Pair<OutputStream, XMLStreamWriter> oProps;
    final private Pair<OutputStream, XMLStreamWriter> oNamed;
    final private Pair<OutputStream, XMLStreamWriter> oChunk;
    final private Pair<OutputStream, XMLStreamWriter> oCoref;
    final private Pair<OutputStream, XMLStreamWriter> oRel;
    final private Pair<OutputStream, XMLStreamWriter> oMention;
    TokenAttributeIndex attributeIndex;
    private boolean open = false;
    private boolean indent = true;
    private String documentName;
    private int currentParagraphIdx;
    private List<Pattern> namedPatterns;
    private List<Pattern> mentionPatterns;
    private List<Pattern> chunksPatterns;
    // Identyfikatory anotacji z nazwą pliku, w którym zostały zapisane, np. "ann_mentions.xml#an1"
    private HashMap<Annotation, String> mentionRefs = Maps.newHashMap();
    // Identyfikatory anotacji bez nazwy pliku, np. "an1"
    private HashMap<Annotation, String> mentionIds = Maps.newHashMap();
    private int mentionNr = 0;
    private int chunkNr = 0;
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @param text
     * @param metadata
     * @param annSegmentation
     * @param annMorphosyntax
     * @param annProps
     * @param annNamed
     * @param annMentions
     * @param annChunks
     * @param annCoreference
     * @param annRelations
     * @param documentName
     */
    public TeiStreamWriter(OutputStream text, OutputStream metadata, OutputStream annSegmentation, OutputStream annMorphosyntax,
                           OutputStream annProps, OutputStream annNamed, OutputStream annMentions, OutputStream annChunks,
                           OutputStream annCoreference, OutputStream annRelations, String documentName) throws XMLStreamException {
        this.documentName = documentName;
        this.namedPatterns = Arrays.asList(Pattern.compile(".*nam"));
        this.chunksPatterns = Arrays.asList(Pattern.compile("^chunk_.*"));
        this.mentionPatterns = Lists.newArrayList(Pattern.compile(".*nam"), Pattern.compile("anafora_wyznacznik"));
        this.mentionPatterns.add(Pattern.compile("^(landmark|spatial_indicator|trajector|spatial_object|region|path).*", Pattern.CASE_INSENSITIVE));

        oText = new ImmutablePair<>(text, createXmlWriter(text));
        oMeta = new ImmutablePair<>(metadata, createXmlWriter(metadata));
        oSegm = new ImmutablePair<>(annSegmentation, createXmlWriter(annSegmentation));
        oMorph = new ImmutablePair<>(annMorphosyntax, createXmlWriter(annMorphosyntax));
        oProps = new ImmutablePair<>(annProps, createXmlWriter(annProps));
        oNamed = new ImmutablePair<>(annNamed, createXmlWriter(annNamed));
        oChunk = new ImmutablePair<>(annChunks, createXmlWriter(annChunks));
        oCoref = new ImmutablePair<>(annCoreference, createXmlWriter(annCoreference));
        oRel = new ImmutablePair<>(annRelations, createXmlWriter(annRelations));
        oMention = new ImmutablePair<>(annMentions, createXmlWriter(annMentions));
    }

    private XMLStreamWriter createXmlWriter(OutputStream outputStream) throws XMLStreamException {
        return outputStream == null ? null : XMLOutputFactory.newFactory().createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(outputStream)));
    }

    private void writeAttributes(XMLStreamWriter writer, Map<String, String> attributes) throws XMLStreamException {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            writer.writeAttribute(entry.getKey(), entry.getValue());
        }
    }

    private void indent(int repeat, XMLStreamWriter xmlw) throws XMLStreamException {
        if (this.indent) {
            xmlw.writeCharacters(StringUtils.repeat(' ', repeat));
        }
    }

    private void writelnStartElement(XMLStreamWriter writer, int indent, String text) throws XMLStreamException {
        if (writer == null) {
            return;
        }
        indent(indent, writer);
        writer.writeStartElement(text);
        writer.writeCharacters("\n");
    }

    private void writelnEndElement(XMLStreamWriter writer, int indent) throws XMLStreamException {
        if (writer == null) {
            return;
        }
        indent(indent, writer);
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    private void writelnStartElement(XMLStreamWriter writer, int indent, String text, Map<String, String> attributes) throws XMLStreamException {
        if (writer == null) {
            return;
        }
        indent(indent, writer);
        writer.writeStartElement(text);
        writeAttributes(writer, attributes);
        writer.writeCharacters("\n");
    }

    private void writelnElement(XMLStreamWriter writer, int indent, String text, Map<String, String> attributes, String characters) throws XMLStreamException {
        if (writer == null) {
            return;
        }
        indent(indent, writer);
        writer.writeStartElement(text);
        writeAttributes(writer, attributes);
        writer.writeCharacters(characters);
        writer.writeEndElement();
        writer.writeCharacters("\n");
    }

    private void writelnEmptyElement(XMLStreamWriter writer, int indent, String text, Map<String, String> attributes) throws XMLStreamException {
        if (writer == null) {
            return;
        }
        indent(indent, writer);
        writer.writeEmptyElement(text);
        writeAttributes(writer, attributes);
        writer.writeCharacters("\n");
    }

    public void writeCommonOpening(XMLStreamWriter writer) throws XMLStreamException {
        if (writer == null) {
            return;
        }
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeCharacters("\n");
        writelnStartElement(writer, 0, TAG_CORPUS,
                ImmutableMap.of("xmlns", "http://www.tei-c.org/ns/1.0", "xmlns:xi", "http://www.w3.org/2001/XInclude", "xmlns:nkjp", "http://www.nkjp.pl/ns/1.0"));
        writelnStartElement(writer, 1, TAG_TEI);
        this.indent(2, writer);
        writer.writeStartElement(TAG_TEXT);
    }

    public void writeCommonOpening(XMLStreamWriter writer, Map<String, String> attributes) throws XMLStreamException {
        writeCommonOpening(writer);
        writeAttributes(writer, attributes);
        writer.writeCharacters("\n");
    }

    private void writeTextOpening(XMLStreamWriter writer) throws XMLStreamException {
        writeCommonOpening(writer);
        writer.writeCharacters("\n");
        writelnStartElement(writer, 3, TAG_FRONT);
        writelnStartElement(writer, 4, TAG_TITLE);
        writelnElement(writer, 5, TAG_TITLEPART, ImmutableMap.of("type", "title", "xml:id", "titlePart-1"), documentName);
        writelnEndElement(writer, 4);
        writelnEndElement(writer, 3);
        writelnStartElement(writer, 3, TAG_BODY);
    }

    private void writeSegmentationOpening(XMLStreamWriter writer) throws XMLStreamException {
        writeCommonOpening(writer, ImmutableMap.of("xml:lang", "pl", "xml:id", "segm_text"));
        writelnStartElement(writer, 3, TAG_BODY, ImmutableMap.of("xml:id", "segm_body"));
    }

    private void writeMorphosyntaxOpening(XMLStreamWriter writer) throws XMLStreamException {
        writeCommonOpening(writer);
        writer.writeCharacters("\n");
        writelnStartElement(writer, 3, TAG_BODY);
    }

    private void writeMetadataOpeningNullSafe(XMLStreamWriter writer) throws XMLStreamException {
        if (writer == null) {
            return;
        }
        writeCommonOpening(writer, ImmutableMap.of("xml:lang", "pl"));
        writelnStartElement(oMeta.getRight(), 3, TAG_BODY);
    }

    private void writeGenericOpeningNullSafe(XMLStreamWriter writer) throws XMLStreamException {
        if (writer == null) {
            return;
        }
        writeCommonOpening(writer, ImmutableMap.of("xml:lang", "pl"));
        writelnStartElement(writer, 3, TAG_BODY);
    }

    private void writelnComment(XMLStreamWriter writer, int indent, String comment) throws XMLStreamException {
        indent(indent, writer);
        writer.writeComment(comment);
        writer.writeCharacters("\n");
    }

    @Override
    public void flush() {
    }

    public void open() throws XMLStreamException {
        if (open) {
            return;
        }
        writeTextOpening(oText.getRight());
        writeSegmentationOpening(oSegm.getRight());
        writeMorphosyntaxOpening(oMorph.getRight());
        writeMetadataOpeningNullSafe(oMeta.getRight());
        writeGenericOpeningNullSafe(oNamed.getRight());
        writeGenericOpeningNullSafe(oProps.getRight());
        writeGenericOpeningNullSafe(oMention.getRight());
        writeGenericOpeningNullSafe(oChunk.getRight());
        writeGenericOpeningNullSafe(oCoref.getRight());
        writeGenericOpeningNullSafe(oRel.getRight());
        open = true;
    }

    private void writeMetadata(Map<String, String> metadata) throws XMLStreamException {
        for (Map.Entry<String, String> kv : metadata.entrySet()) {
            writelnEmptyElement(oMeta.getRight(), 0, TAG_METADATA,
                    ImmutableMap.of("name", kv.getKey(), "value", kv.getValue()));
        }
    }

    private void writeParagraphStart(String paragraphId) throws XMLStreamException {
        this.indent(5, oText.getRight());
        oText.getRight().writeStartElement(TAG_PARAGRAPH);
        oText.getRight().writeAttribute("xml:id", paragraphId);

        writelnStartElement(oSegm.getRight(), 4, TAG_PARAGRAPH,
                ImmutableMap.of("corresp", "text.xml#" + paragraphId, "xml:id", "segm_" + paragraphId));
        writelnStartElement(oMorph.getRight(), 4, TAG_PARAGRAPH,
                ImmutableMap.of("xml:id", paragraphId));
        writelnStartElement(oNamed.getRight(), 4, TAG_PARAGRAPH,
                ImmutableMap.of("xml:id", paragraphId, "corresp", "ann_morphosyntax.xml#" + paragraphId));
        writelnStartElement(oMention.getRight(), 4, TAG_PARAGRAPH,
                ImmutableMap.of("xml:id", paragraphId, "corresp", "ann_morphosyntax.xml#" + paragraphId));
        writelnStartElement(oChunk.getRight(), 4, TAG_PARAGRAPH,
                ImmutableMap.of("xml:id", paragraphId, "corresp", "ann_morphosyntax.xml#" + paragraphId));
    }

    @Override
    public void writeDocument(Document document) {
        for (Paragraph paragraph : document.getParagraphs()) {
            this.writeParagraph(paragraph);
        }
        try {
            this.writeMetadata(document.getDocumentDescriptor().getMetadata());
            this.writeCoreferenceRelations(document.getRelations(Relation.COREFERENCE));
            this.writeRelations(document.getRelations().getRelations());
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zapisuje wszystkie relacje do strumienia annRelationsWriter
     *
     * @param relations
     * @throws XMLStreamException
     */
    private void writeRelations(Set<Relation> relations) throws XMLStreamException {
        int relId = 1;
        for (Relation relation : relations) {

            String sourceRef = this.mentionRefs.get(relation.getAnnotationFrom());
            String targetRef = this.mentionRefs.get(relation.getAnnotationTo());

            if (sourceRef == null) {
                sourceRef = "?";
            } else {
                logger.warn("Annotation ref id not found for " + relation.getAnnotationFrom());
            }

            if (targetRef == null) {
                targetRef = "?";
            } else {
                logger.warn("Annotation ref id not found for " + relation.getAnnotationTo());
            }

            writelnStartElement(oRel.getRight(), 4, TAG_SEGMENT,
                    ImmutableMap.of("xml:id", "relation_" + relId++, "type", relation.getType()));
            writelnEmptyElement(oRel.getRight(), 5, TAG_POINTER,
                    ImmutableMap.of("type", "source", "target", sourceRef));
            writelnEmptyElement(oRel.getRight(), 5, TAG_POINTER,
                    ImmutableMap.of("type", "target", "target", targetRef));
            writelnEndElement(oRel.getRight(), 4);
        }
    }

    private void writeParagraph(Paragraph paragraph) {
        attributeIndex = paragraph.getAttributeIndex();
        try {
            HashMap<String, String> currentIds = Maps.newHashMap();
            if (!open) {
                open();
            }
            currentParagraphIdx++;
            currentIds.put("paragraphId", "p-" + currentParagraphIdx);
            writeParagraphStart(currentIds.get("paragraphId"));

            int sentenceNr = 1;
            int tokenNr = 1;
            StringBuilder wholeParagraph = new StringBuilder();
            for (Sentence sent : paragraph.getSentences()) {
                currentIds.put("sentenceId", currentIds.get("paragraphId") + "." + (sentenceNr++) + "-s");
                writeSentence(sent, currentIds, tokenNr, wholeParagraph);
                tokenNr += sent.getTokenNumber();
            }
            writelnEndElement(oSegm.getRight(), 4);
            writelnEndElement(oMorph.getRight(), 4);
            writelnEndElement(oNamed.getRight(), 4);
            writelnEndElement(oMention.getRight(), 4);
            writelnEndElement(oChunk.getRight(), 4);

            oText.getRight().writeCharacters(wholeParagraph.toString().trim());
            writelnEndElement(oText.getRight(), 0);
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    private void writeSentence(Sentence sent, HashMap<String, String> currentIds, int currentTokenNr, StringBuilder wholeParagraph) throws XMLStreamException {
        writelnStartElement(oSegm.getRight(), 5, TAG_SENTENCE,
                ImmutableMap.of("xml:id", "segm_" + currentIds.get("sentenceId")));
        writelnStartElement(oMorph.getRight(), 5, TAG_SENTENCE,
                ImmutableMap.of("corresp", "ann_segmentation.xml#segm_" + currentIds.get("sentenceId"), "xml:id", currentIds.get("sentenceId")));
        writelnStartElement(oNamed.getRight(), 5, TAG_SENTENCE,
                ImmutableMap.of("xml:id", currentIds.get("sentenceId"), "corresp", "ann_morphosyntax.xml#" + currentIds.get("sentenceId")));
        writelnStartElement(oMention.getRight(), 5, TAG_SENTENCE,
                ImmutableMap.of("xml:id", currentIds.get("sentenceId"), "corresp", "ann_morphosyntax.xml#" + currentIds.get("sentenceId")));
        writelnStartElement(oChunk.getRight(), 5, TAG_SENTENCE,
                ImmutableMap.of("xml:id", currentIds.get("sentenceId"), "corresp", "ann_morphosyntax.xml#" + currentIds.get("sentenceId")));

        List<Token> sentenceTokens = sent.getTokens();
        HashMap<Integer, String> tokenTEIIds = new HashMap<Integer, String>();
        boolean noPreviousSpace = false;
        for (int i = 0; i < sent.getTokenNumber(); i++) {
            currentIds.put("tokenId", currentIds.get("paragraphId") + "." + (currentTokenNr++) + "-seg");
            Token currentToken = sentenceTokens.get(i);
            tokenTEIIds.put(i, "morph_" + currentIds.get("tokenId"));
            writeToken(currentToken, currentIds, wholeParagraph, noPreviousSpace);
            noPreviousSpace = currentToken.getNoSpaceAfter();
            if (!noPreviousSpace) {
                wholeParagraph.append(" ");
            }
        }

        // Write named entities
        if (oNamed.getRight() != null) {
            int annotationNr = 1;
            for (Annotation ann : sent.getAnnotations(namedPatterns)) {
                writeAnnotation(oNamed.getRight(), ann, currentIds.get("sentenceId") + "_n" + (annotationNr++), tokenTEIIds);
            }
        }

        // Write mentions
        if (oMention.getRight() != null) {
            for (Annotation ann : sent.getAnnotations(mentionPatterns)) {
                writeChunk(oMention.getRight(), ann, "mention_" + (++this.mentionNr), tokenTEIIds);
            }
        }

        // Write chunks
        if (oChunk.getRight() != null) {
            for (Annotation ann : sent.getAnnotations(this.chunksPatterns)) {
                writeChunk(oChunk.getRight(), ann, "chunk_" + (++this.chunkNr), tokenTEIIds);
            }
        }

        writelnEndElement(oSegm.getRight(), 6);
        writelnEndElement(oMorph.getRight(), 5);
        writelnEndElement(oNamed.getRight(), 5);
        writelnEndElement(oMention.getRight(), 5);
        writelnEndElement(oChunk.getRight(), 5);
    }


    private void writeRelationCluster(AnnotationCluster cluster, int clusterId) throws XMLStreamException {
        int annCoreferenceIndent = 6;
        writelnComment(oCoref.getRight(), annCoreferenceIndent,
                cluster.getAnnotations().stream().map(an -> an.getText()).collect(Collectors.joining(", ")));
        writelnStartElement(oCoref.getRight(), annCoreferenceIndent, TAG_SEGMENT, ImmutableMap.of("xml:id", "coreference_" + clusterId));
        writelnStartElement(oCoref.getRight(), ++annCoreferenceIndent, TAG_FEATURESET, ImmutableMap.of("type", "coreference"));
        writelnEmptyElement(oCoref.getRight(), ++annCoreferenceIndent, TAG_FEATURESET, ImmutableMap.of("name", "type", "fVal", "ident"));
        writelnEndElement(oCoref.getRight(), --annCoreferenceIndent);
        for (Annotation ann : cluster.getAnnotations()) {
            writelnEmptyElement(oCoref.getRight(), annCoreferenceIndent, TAG_POINTER, ImmutableMap.of("target", "ann_mentions.xml#" + mentionIds.get(ann)));
        }
        writelnEndElement(oCoref.getRight(), --annCoreferenceIndent);
    }

    private void writeCoreferenceRelations(RelationSet relations) throws XMLStreamException {
        writelnStartElement(oCoref.getRight(), 4, TAG_PARAGRAPH);
        AnnotationClusterSet documentRelations = AnnotationClusterSet.fromRelationSet(relations);
        int clusterId = 0;
        for (AnnotationCluster cluster : documentRelations.getClusters()) {
            this.writeRelationCluster(cluster, ++clusterId);
        }
        writelnEndElement(oCoref.getRight(), 4);
    }

    /**
     * <!-- płace kontrolerów  -->
     * <seg xml:id="mention_2">
     * <fs type="mention">
     * <f name="semh" fVal="ann_morphosyntax.xml#morph_1.1.5-seg"/>
     * </fs>
     * <ptr target="ann_morphosyntax.xml#morph_1.1.5-seg"/>
     * <ptr target="ann_morphosyntax.xml#morph_1.1.6-seg"/>
     * </seg>
     *
     * @param ann
     * @param annotationId
     * @param tokenTEIIds
     * @throws XMLStreamException
     */
    private void writeChunk(XMLStreamWriter writer, Annotation ann, String annotationId, HashMap<Integer, String> tokenTEIIds) throws XMLStreamException {
        this.mentionIds.put(ann, annotationId);
        this.mentionRefs.put(ann, "ann_mentions.xml#" + annotationId);

        int annMentionsIndent = 6;
        writelnComment(writer, annMentionsIndent, ann.getText());
        writelnStartElement(writer, annMentionsIndent, TAG_SEGMENT, ImmutableMap.of("xml:id", annotationId));
        writelnStartElement(writer, ++annMentionsIndent, TAG_FEATURESET, ImmutableMap.of("type", ann.getType()));
        if (!ann.hasHead()) ann.assignHead();  // ToDo: Writer should not modify the input data
        writelnEmptyElement(writer, ++annMentionsIndent, TAG_FEATURE,
                ImmutableMap.of("name", "semh", "fVal", "ann_morphosyntax.xml#" + tokenTEIIds.get(ann.getHead())));
        writelnEndElement(writer, --annMentionsIndent);
        for (int tokenIdx : ann.getTokens()) {
            writelnEmptyElement(writer, annMentionsIndent, TAG_POINTER, ImmutableMap.of("target", "ann_morphosyntax.xml#" + tokenTEIIds.get(tokenIdx)));
        }
        writelnEndElement(writer, --annMentionsIndent);
    }

    public void writeAnnotation(XMLStreamWriter writer, Annotation ann, String annotationId, HashMap<Integer, String> tokenTEIIds) throws XMLStreamException {
        int annNamedIndent = 6;
        writelnStartElement(writer, annNamedIndent, TAG_SEGMENT, ImmutableMap.of("xml:id", annotationId));
        writelnStartElement(writer, ++annNamedIndent, TAG_FEATURESET, ImmutableMap.of("type", "named"));
        writelnStartElement(writer, ++annNamedIndent, TAG_FEATURE, ImmutableMap.of("name", "type"));
        writelnEmptyElement(writer, ++annNamedIndent, TAG_SYMBOL, ImmutableMap.of("value", ann.getType()));
        writelnEndElement(writer, --annNamedIndent);
        writelnStartElement(writer, annNamedIndent, TAG_FEATURE, ImmutableMap.of("name", "orth"));
        writelnElement(writer, ++annNamedIndent, TAG_STRING, Maps.newHashMap(), ann.getText());
        writelnEndElement(writer, --annNamedIndent);
        writelnEndElement(writer, --annNamedIndent);
        for (int tokenIdx : ann.getTokens()) {
            writelnEmptyElement(writer, annNamedIndent, TAG_POINTER,
                    ImmutableMap.of("target", "ann_morphosyntax.xml#" + tokenTEIIds.get(tokenIdx)));
        }
        writelnEndElement(writer, --annNamedIndent);
    }

    public void writeToken(Token tok, HashMap<String, String> currentIds, StringBuilder wholeParagraph, boolean noPreviousSpace) throws XMLStreamException {
        int tokenStart = wholeParagraph.length();
        String orth = tok.getOrth();

        writelnComment(oSegm.getRight(), 6, " " + orth + " ");

        Map<String, String> attributes = Maps.newHashMap();
        attributes.put("corresp", String.format("text.xml#string-range(%s,%d,%d)", currentIds.get("paragraphId"), tokenStart, orth.length()));
        if (noPreviousSpace) {
            attributes.put("nkjp:nps", "true");
        }
        attributes.put("xml:id", "segm_" + currentIds.get("tokenId"));
        writelnEmptyElement(oSegm.getRight(), 6, TAG_SEGMENT, attributes);

        String morphId = "morph_" + currentIds.get("tokenId");
        currentIds.put("morphId", morphId);

        writelnStartElement(oMorph.getRight(), 6, TAG_SEGMENT,
                ImmutableMap.of("corresp", "ann_segmentation.xml#segm_" + currentIds.get("tokenId"), "xml:id", morphId));
        writelnStartElement(oMorph.getRight(), 7, TAG_FEATURESET, ImmutableMap.of("type", "morph"));
        writelnStartElement(oMorph.getRight(), 8, TAG_FEATURE, ImmutableMap.of("name", "orth"));
        writelnElement(oMorph.getRight(), 9, TAG_STRING, Maps.newHashMap(), orth);
        writelnEndElement(oMorph.getRight(), 8);
        if (noPreviousSpace) {
            writelnStartElement(oMorph.getRight(), 8, TAG_FEATURE, ImmutableMap.of("name", "nps"));
            writelnEmptyElement(oMorph.getRight(), 9, TAG_BINARY, ImmutableMap.of("value", "true"));
            writelnEndElement(oMorph.getRight(), 8);
        }
        writelnComment(oMorph.getRight(), 8, String.format("%s [%s,%s]", orth, tokenStart, orth.length()));
        writelnStartElement(oMorph.getRight(), 8, TAG_FEATURE, ImmutableMap.of("name", "interps"));

        Interps interps = new Interps(tok.getTags());
        int annMorphoSyntaxIndent = 9;

        if (interps.getLexemes().size() > 1) {
            writelnStartElement(oMorph.getRight(), annMorphoSyntaxIndent++, TAG_VALT);
        }
        int lexId = 0;
        for (TeiLex lex : interps.getLexemes()) {
            currentIds.put("lexId", morphId + "_" + (lexId++) + "-lex");
            writeLexeme(oMorph.getRight(), lex, currentIds, annMorphoSyntaxIndent);
        }

        if (interps.getLexemes().size() > 1) {
            writelnEndElement(oMorph.getRight(), --annMorphoSyntaxIndent);
        }

        writelnEndElement(oMorph.getRight(), 8);

        writelnStartElement(oMorph.getRight(), 8, TAG_FEATURE, ImmutableMap.of("name", "disamb"));
        writelnStartElement(oMorph.getRight(), 9, TAG_FEATURESET,
                ImmutableMap.of(
                        "feats", Optional.ofNullable(attributeIndex.getAttributeValue(tok, "tagTool")).orElse("#unknown"),
                        "type", "tool_report"));
        writelnEmptyElement(oMorph.getRight(), 10, TAG_FEATURE,
                ImmutableMap.of("fVal", "#" + morphId + "_" + interps.getDisambIdx() + "-msd", "name", "choice"));
        writelnStartElement(oMorph.getRight(), 10, TAG_FEATURE, ImmutableMap.of("name", "interpretation"));
        writelnElement(oMorph.getRight(), 11, TAG_STRING, Maps.newHashMap(), interps.getDisamb());
        for (int i = 10; i > 5; i--) {
            writelnEndElement(oMorph.getRight(), i);
        }
        wholeParagraph.append(orth);

        /* Zapisz propsy */
        if (tok.getProps().size() > 0) {
            writelnStartElement(oProps.getRight(), 1, TAG_SEGMENT, ImmutableMap.of(ATTR_CORESP, "ann_morphosyntax.xml#" + morphId));
            for (String name : tok.getProps().keySet()) {
                writelnElement(oProps.getRight(), 2, TAG_FEATURE, ImmutableMap.of(ATTR_NAME, name), tok.getProps().get(name));
            }
            writelnEndElement(oProps.getRight(), 1);
        }
    }

    public void writeLexeme(XMLStreamWriter writer, TeiLex lex, HashMap<String, String> currentIds, int currentIndent) throws XMLStreamException {
        writelnStartElement(writer, currentIndent, TAG_FEATURESET, ImmutableMap.of("type", "lex", "xml:id", currentIds.get("lexId")));
        writelnStartElement(writer, ++currentIndent, TAG_FEATURE, ImmutableMap.of("name", "base"));
        writelnElement(writer, ++currentIndent, TAG_STRING, Maps.newHashMap(), lex.getBase());
        writelnEndElement(writer, --currentIndent);
        writelnStartElement(writer, currentIndent, TAG_FEATURE, ImmutableMap.of("name", "ctag"));
        writelnEmptyElement(writer, ++currentIndent, TAG_SYMBOL, ImmutableMap.of("value", lex.getCtag()));
        writelnEndElement(writer, --currentIndent);
        writelnStartElement(writer, currentIndent, TAG_FEATURE, ImmutableMap.of("name", "msd"));
        if (lex.msdSize() > 1) {
            writelnStartElement(writer, ++currentIndent, TAG_VALT);
        }
        currentIndent++;
        for (Pair<String, Integer> entry : lex.getMsds()) {
            String msd = entry.getLeft();
            int msdIdx = entry.getRight();
            writelnEmptyElement(oMorph.getRight(), currentIndent, TAG_SYMBOL,
                    ImmutableMap.of("value", msd, "xml:id", currentIds.get("morphId") + "_" + msdIdx + "-msd"));
        }
        if (lex.msdSize() > 1) {
            writelnEndElement(oMorph.getRight(), --currentIndent);
        }
        writelnEndElement(oMorph.getRight(), --currentIndent);
        writelnEndElement(oMorph.getRight(), --currentIndent);
    }

    @Override
    public void close() {
        try {
            closeStream(oText, 4);
            closeStream(oMeta, 1);
            closeStream(oSegm, 3);
            closeStream(oMorph, 3);
            closeStream(oProps, 1);
            closeStream(oNamed, 3);
            closeStream(oMention, 3);
            closeStream(oChunk, 3);
            closeStream(oCoref, 3);
            closeStream(oRel, 3);
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeClosing(XMLStreamWriter xmlw, int indent) throws XMLStreamException {
        if (xmlw != null) {
            while (indent > 0) {
                this.indent(indent--, xmlw);
                xmlw.writeEndElement();
                xmlw.writeCharacters("\n");
            }
            xmlw.writeEndDocument();
        }
    }

    private void closeStream(Pair<OutputStream, XMLStreamWriter> stream, int indent) throws XMLStreamException, IOException {
        writeClosing(stream.getRight(), indent);
        if (stream.getRight() != null) {
            stream.getRight().close();
        }
        if (stream.getLeft() != null && stream.getLeft() instanceof GZIPOutputStream) {
            ((GZIPOutputStream) stream.getLeft()).finish();
        }
        if (stream.getLeft() != null) {
            stream.getLeft().close();
        }
    }
}