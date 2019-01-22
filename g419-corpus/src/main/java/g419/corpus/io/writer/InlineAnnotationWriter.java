package g419.corpus.io.writer;

import g419.corpus.structure.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Stack;

/**
 * @author Michał Marcińczuk
 */
public class InlineAnnotationWriter extends AbstractDocumentWriter {

    private final String TAG_DOCUMENT = "document";
    private final String ATTR_DOCUMENT_ID = "id";

    private final String TAG_PHRASE = "phrase";
    private final String ATTR_PHRASE_ID = "id";

    private XMLStreamWriter xmlw;

    public InlineAnnotationWriter(final OutputStream os) {
        try {
            final XMLOutputFactory xmlof = XMLOutputFactory.newFactory();
            xmlw = xmlof.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(os)));
        } catch (final XMLStreamException ex) {
            ex.printStackTrace();
        }
    }

    private void openXml(final String documentId) {
        try {
            xmlw.writeStartDocument("UTF-8", "1.0");
            xmlw.writeCharacters("\n");
            xmlw.writeStartElement(TAG_DOCUMENT);
            xmlw.writeAttribute(ATTR_DOCUMENT_ID, documentId);
            xmlw.writeCharacters("\n");
        } catch (final XMLStreamException ex) {
            getLogger().error("An exception occurred", ex);
        }
    }

    private void closeXml() {
        try {
            xmlw.writeCharacters("\n");
            xmlw.writeEndDocument();
            xmlw.close();
        } catch (final XMLStreamException ex) {
            getLogger().error("An exception occurred", ex);
        }
    }

    @Override
    public void close() {
        try {
            xmlw.flush();
            xmlw.close();
        } catch (final XMLStreamException ex) {
            getLogger().error("An exception occurred", ex);
        }
    }

    @Override
    public void writeDocument(final Document document) {
        openXml(document.getName());
        int i = 0;
        for (final Paragraph paragraph : document.getParagraphs()) {
            if (i++ > 0) {
                try {
                    xmlw.writeCharacters("\n\n");
                } catch (final XMLStreamException ex) {
                    getLogger().error("An exception occurred", ex);
                }
            }
            paragraph.getSentences().forEach(this::writeSentence);
        }
        closeXml();
    }

    private void writeSentence(final Sentence sentence) {
        try {
            final SentenceAnnotationIndexTypePos index = new SentenceAnnotationIndexTypePos(sentence);
            final Stack<Integer> annotationEnds = new Stack<>();
            for (int n = 0; n < sentence.getTokenNumber(); n++) {
                index.getAnnotationStartingFrom(n).forEach(an -> {
                    annotationEnds.push(an.getEnd());
                    writeAnnotationStart(an);
                });
                final Token t = sentence.getTokens().get(n);
                xmlw.writeCharacters(t.getOrth());
                while (!annotationEnds.empty() && annotationEnds.peek() == n) {
                    annotationEnds.pop();
                    writeAnnotationEnd();
                }
                if (!t.getNoSpaceAfter()) {
                    xmlw.writeCharacters(" ");
                }
            }
        } catch (final XMLStreamException ex) {
            getLogger().error("An exception occurred", ex);
        }
    }

    private void writeAnnotationStart(final Annotation an) {
        try {
            getLogger().debug(an.toString());
            xmlw.writeStartElement(TAG_PHRASE);
            xmlw.writeAttribute(ATTR_PHRASE_ID, an.getId());
        } catch (final XMLStreamException ex) {
            getLogger().error("An exception occurred", ex);
        }
    }

    private void writeAnnotationEnd() {
        try {
            xmlw.writeEndElement();
        } catch (final XMLStreamException ex) {
            getLogger().error("An exception occurred", ex);
        }
    }

    @Override
    public void flush() {
        try {
            xmlw.flush();
        } catch (final XMLStreamException ex) {
            getLogger().error("An exception occurred", ex);
        }
    }
}
