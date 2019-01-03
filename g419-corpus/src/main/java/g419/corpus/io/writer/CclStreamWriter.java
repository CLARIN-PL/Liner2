package g419.corpus.io.writer;

import com.google.common.collect.Maps;
import g419.corpus.structure.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.*;
import java.util.zip.DataFormatException;


public class CclStreamWriter extends AbstractDocumentWriter {

    private final String TAG_ANN = "ann";
    private final String TAG_BASE = "base";
    private final String TAG_CHAN = "chan";
    private final String TAG_CTAG = "ctag";
    private final String TAG_DISAMB = "disamb";
    private final String TAG_ID = "id";
    private final String TAG_NS = "ns";
    private final String TAG_ORTH = "orth";
    private final String TAG_PARAGRAPH = "chunk";
    private final String TAG_PARAGRAPH_SET = "chunkList";
    private final String TAG_SENTENCE = "sentence";
    private final String TAG_TAG = "lex";
    private final String TAG_TOKEN = "tok";
    private final String TAG_HEAD = "head";

    private final String TAG_RELATIONS = "relations";
    private final String TAG_RELATION = "rel";
    private final String TAG_FROM = "from";
    private final String TAG_TO = "to";
    private final String ATTR_SENT = "sent";
    private final String ATTR_CHAN = "chan";
    private final String ATTR_NAME = "name";
    private final String ATTR_SET = "set";

    private final String TAG_PROP = "prop";
    private final String ATTR_KEY = "key";

    private XMLStreamWriter xmlw;
    private XMLStreamWriter xmlRelw;
    private final OutputStream os;
    private OutputStream osRel;
    private final Map<Annotation, Map<String, Integer>> annotationSentChannelIdx;
    private XMLOutputFactory xmlof = null;
    private final boolean indent = true;
    private final String[] requiredAttributes = new String[]{"orth", "base", "ctag"};
    private boolean disambOnly = false;

    public CclStreamWriter(final OutputStream os) {
        this.os = os;
        xmlof = XMLOutputFactory.newFactory();
        annotationSentChannelIdx = new HashMap<>();
    }

    /**
     * @param os
     * @param disambOnly jeżeli true, to zapisywane są tylko te tagi morfologiczne, które zostały oznaczone jako disamb.
     */
    public CclStreamWriter(final OutputStream os, final boolean disambOnly) {
        this.os = os;
        xmlof = XMLOutputFactory.newFactory();
        annotationSentChannelIdx = Maps.newHashMap();
        this.disambOnly = disambOnly;
    }

    public CclStreamWriter(final OutputStream os, final OutputStream rel) {
        this.os = os;
        osRel = rel;
        xmlof = XMLOutputFactory.newFactory();
        annotationSentChannelIdx = new HashMap<>();
    }

    public CclStreamWriter(final OutputStream os, final OutputStream rel, final boolean disambOnly) {
        this.os = os;
        osRel = rel;
        xmlof = XMLOutputFactory.newFactory();
        annotationSentChannelIdx = new HashMap<>();
        this.disambOnly = disambOnly;
    }

    private void openRelXml() {
        try {
            xmlRelw = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(osRel)));
            xmlRelw.writeStartDocument("UTF-8", "1.0");
            xmlRelw.writeCharacters("\n");
            xmlRelw.writeStartElement(TAG_RELATIONS);
            xmlRelw.writeCharacters("\n");
        } catch (final XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    private void closeRelXml() {
        try {
            xmlRelw.writeEndDocument();
            xmlRelw.close();
        } catch (final XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void openXml() {
        try {
            xmlw = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(os)));
            xmlw.writeStartDocument("UTF-8", "1.0");
            xmlw.writeCharacters("\n");
            xmlw.writeDTD("<!DOCTYPE chunkList SYSTEM \"ccl.dtd\">");
            xmlw.writeCharacters("\n");
            xmlw.writeStartElement(TAG_PARAGRAPH_SET);
            xmlw.writeCharacters("\n");
        } catch (final XMLStreamException e) {
            getLogger().error("Problem with opening the xml file occurred", e);
        }
    }

    private void closeXml() {
        try {
            xmlw.writeEndDocument();
            xmlw.close();
        } catch (final XMLStreamException e) {
            getLogger().error("Problem with closing the xml file occurred", e);
        }
    }

    @Override
    public void flush() {
        try {
            if (xmlw != null && !os.equals(System.out)) {
                xmlw.flush();
            }
            if (xmlRelw != null && !os.equals(System.out)) {
                xmlRelw.flush();
            }
        } catch (final XMLStreamException e) {
            getLogger().error("Problem with flushing the xml file occurred", e);
        }
    }

    @Override
    public void close() {
        try {
            if (!(os instanceof PrintStream)) {
                os.close();
            }
            if (osRel != null && !(osRel instanceof PrintStream)) {
                osRel.close();
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeRelations(final Document document) throws XMLStreamException {
        for (final Relation relation : document.getRelations().getRelations()) {
            writeRelation(relation);
        }
    }

    public void writeRelation(final Relation relation) throws XMLStreamException {
        final String fromSentenceId = relation.getAnnotationFrom().getSentence().getId();
        final String fromChannel = relation.getAnnotationFrom().getType().toLowerCase();

        final String toSentenceId = relation.getAnnotationTo().getSentence().getId();
        final String toChannel = relation.getAnnotationTo().getType().toLowerCase();

        int fromAnnIdx = 0;
        int toAnnIdx = 0;
        try {
            fromAnnIdx = annotationSentChannelIdx.get(relation.getAnnotationFrom()).get(fromChannel);
            toAnnIdx = annotationSentChannelIdx.get(relation.getAnnotationTo()).get(toChannel);
        } catch (final Exception ex) {
            System.out.println(relation);
            System.out.println(relation.getDocument().getName());
        }

        indentRel(2);
        xmlRelw.writeStartElement(TAG_RELATION);
        xmlRelw.writeAttribute(ATTR_NAME, relation.getType());
        xmlRelw.writeAttribute(ATTR_SET, relation.getSet());
        xmlRelw.writeCharacters("\n");
        indentRel(4);
        xmlRelw.writeStartElement(TAG_FROM);
        xmlRelw.writeAttribute(ATTR_SENT, fromSentenceId);
        xmlRelw.writeAttribute(ATTR_CHAN, relation.getAnnotationFrom().getType());
        xmlRelw.writeCharacters(fromAnnIdx + "");
        xmlRelw.writeEndElement();
        xmlRelw.writeCharacters("\n");
        indentRel(4);
        xmlRelw.writeStartElement(TAG_TO);
        xmlRelw.writeAttribute(ATTR_SENT, toSentenceId);
        xmlRelw.writeAttribute(ATTR_CHAN, relation.getAnnotationTo().getType());
        xmlRelw.writeCharacters(toAnnIdx + "");
        xmlRelw.writeEndElement();
        xmlRelw.writeCharacters("\n");
        indentRel(2);
        xmlRelw.writeEndElement();
        xmlRelw.writeCharacters("\n");
    }

    @Override
    public void writeDocument(final Document document) {
        if (!hasRequiredAttributes(document.getAttributeIndex())) {
            try {
                throw new DataFormatException("Document attribute index does not contain features required by ccl format: " + Arrays.toString(requiredAttributes));
            } catch (final DataFormatException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        openXml();
        for (final Paragraph paragraph : document.getParagraphs()) {
            writeParagraph(paragraph);
        }
        closeXml();


        if (osRel != null) {
            openRelXml();
            if (document.getRelations().getRelations().size() > 0) {
                try {
                    writeRelations(document);
                } catch (final XMLStreamException e) {
                    e.printStackTrace();
                }
            }
            closeRelXml();
        }
    }

    private boolean hasRequiredAttributes(final TokenAttributeIndex attrs) {
        return attrs.allAtributes().containsAll(Arrays.asList(requiredAttributes));
    }

    private void writeParagraph(final Paragraph paragraph) {
        try {
            indent(1);
            xmlw.writeStartElement(TAG_PARAGRAPH);

            final Set<String> chunkMetaDataKeys = paragraph.getKeysChunkMetaData();
            for (final String key : chunkMetaDataKeys) {
                xmlw.writeAttribute(key, paragraph.getChunkMetaData(key));
            }

            if (paragraph.getId() != null) {
                xmlw.writeAttribute(TAG_ID, paragraph.getId());
            }
            xmlw.writeCharacters("\n");
            for (final Sentence sentence : paragraph.getSentences()) {
                writeSentence(sentence);
            }
            indent(1);
            xmlw.writeEndElement();
            xmlw.writeCharacters("\n");
        } catch (final XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    private void writeSentence(final Sentence sentence) throws XMLStreamException {
        indent(2);
        xmlw.writeStartElement(TAG_SENTENCE);
        if (sentence.getId() != null) {
            xmlw.writeAttribute(TAG_ID, sentence.getId());
        }
        xmlw.writeCharacters("\n");

        // prepare annotation channels
        final LinkedHashSet<Annotation> chunks = sentence.getChunks();
        final ArrayList<String> channels = new ArrayList<>();
        for (final Annotation chunk : chunks) {
            if (!channels.contains(chunk.getType())) {
                channels.add(chunk.getType());
            }
        }

        final List<Token> tokens = sentence.getTokens();
        for (int i = 0; i < tokens.size(); i++) {
            writeToken(i, tokens.get(i), chunks, channels);
        }
        indent(2);
        xmlw.writeEndElement();
        xmlw.writeCharacters("\n");
    }

    /**
     * Zapisuje dane tokenu do strumienia xmlw.
     *
     * @param idx
     * @param token
     * @param chunks
     * @param channels
     * @throws XMLStreamException
     */
    private void writeToken(final int idx, final Token token, final HashSet<Annotation> chunks, final ArrayList<String> channels)
            throws XMLStreamException {

        /* Wcięcie i tag rozpoczynający token */
        indent(3);
        xmlw.writeStartElement(TAG_TOKEN);

        /* Identyfikator tokenu */
        if (token.getId() != null) {
            xmlw.writeAttribute(TAG_ID, token.getId());
        }
        xmlw.writeCharacters("\n");

        /* Forma ortograficzna */
        indent(4);
        xmlw.writeStartElement(TAG_ORTH);
        writeText(token.getOrth());
        xmlw.writeEndElement();
        xmlw.writeCharacters("\n");

        /* Tagi morfologiczne */
        for (final Tag tag : token.getTags()) {
            if (disambOnly == false || tag.getDisamb()) {
                writeTag(tag);
            }
        }

        /* Anotacje */
        final Annotation[] tokenchannels = new Annotation[channels.size()];
        for (final Annotation chunk : chunks) {
            if (chunk.getTokens().contains(idx)) {
                tokenchannels[channels.indexOf(chunk.getType())] = chunk;
            }
        }

        for (int chanIdx = 0; chanIdx < channels.size(); chanIdx++) {
            indent(4);
            xmlw.writeStartElement(TAG_ANN);
            final Annotation ann = tokenchannels[chanIdx];
            if (ann != null) {
                int annIdx = 1;
                for (final Annotation a : chunks) {
                    if (a.getType().equals(ann.getType())) {
                        if (a == ann) {
                            break;
                        }
                        annIdx++;
                    }
                }
                xmlw.writeAttribute(TAG_CHAN, ann.getType().toLowerCase());
                if (ann.hasHead() && ann.getHead() == idx) {
                    xmlw.writeAttribute(TAG_HEAD, "1");
                }
                xmlw.writeCharacters("" + annIdx);
                final Map<String, Integer> sentChannelMap = annotationSentChannelIdx.computeIfAbsent(ann, k -> Maps.newHashMap());
                sentChannelMap.put(ann.getType().toLowerCase(), annIdx);
            } else {
                xmlw.writeAttribute(TAG_CHAN, channels.get(chanIdx).toLowerCase());
                xmlw.writeCharacters("0");
            }
            xmlw.writeEndElement();
            xmlw.writeCharacters("\n");
        }

        for (final Annotation a : chunks) {
            if (a.getSentence().getTokens().get(a.getBegin()) == token) {
                if (a.getLemma() != null) {
                    writeAnnotationAttribute(a, "lemma", a.getLemma());
                }
                for (final String key : a.getMetadata().keySet()) {
                    writeAnnotationAttribute(a, key, a.getMetadata().get(key));
                }
            }
        }

        // Token attributes
        for (final String propKey : token.getProps().keySet()) {
            indent(4);
            xmlw.writeStartElement(TAG_PROP);
            xmlw.writeAttribute(ATTR_KEY, propKey);
            xmlw.writeCharacters(token.getProps().get(propKey));
            xmlw.writeEndElement();
            xmlw.writeCharacters("\n");
        }

        /* Tag zamykający token */
        indent(3);
        xmlw.writeEndElement();
        xmlw.writeCharacters("\n");

        /* Tag no space po zamknięciu tokenu */
        if (token.getNoSpaceAfter()) {
            indent(3);
            xmlw.writeEmptyElement(TAG_NS);
            xmlw.writeCharacters("\n");
        }
    }

    private void writeAnnotationAttribute(final Annotation an, final String name, final String value) throws XMLStreamException {
        indent(4);
        xmlw.writeStartElement(TAG_PROP);
        xmlw.writeAttribute(ATTR_KEY, an.getType() + ":" + name);
        xmlw.writeCharacters(value);
        xmlw.writeEndElement();
        xmlw.writeCharacters("\n");
    }

    /**
     * Zapisuje tag do strumienia xmlw.
     *
     * @param tag
     * @throws XMLStreamException
     */
    private void writeTag(final Tag tag) throws XMLStreamException {
        indent(4);
        xmlw.writeStartElement(TAG_TAG);
        if (tag.getDisamb()) {
            xmlw.writeAttribute(TAG_DISAMB, "1");
        }
        xmlw.writeStartElement(TAG_BASE);
        writeText(tag.getBase());
        xmlw.writeEndElement();
        xmlw.writeStartElement(TAG_CTAG);
        writeText(tag.getCtag());
        xmlw.writeEndElement();
        xmlw.writeEndElement();
        xmlw.writeCharacters("\n");
    }

    private void writeText(final String text) throws XMLStreamException {
        if (text.equals("\"")) {
            xmlw.writeEntityRef("quot");
        } else if (text.equals("\'")) {
            xmlw.writeEntityRef("apos");
        } else if (text.equals("<")) {
            xmlw.writeEntityRef("lt");
        } else if (text.equals(">")) {
            xmlw.writeEntityRef("gt");
        } else {
            xmlw.writeCharacters(text);
        }
    }

    private void indentRel(final int repeat) throws XMLStreamException {
        if (indent) {
            for (int i = 0; i < repeat; i++) {
                xmlRelw.writeCharacters(" ");
            }
        }
    }

    /**
     * Wstawia do strumienia określoną liczbę spacji.
     *
     * @param repeat
     * @throws XMLStreamException
     */
    private void indent(final int repeat) throws XMLStreamException {
        if (indent) {
            for (int i = 0; i < repeat; i++) {
                xmlw.writeCharacters(" ");
            }
        }
    }
}
