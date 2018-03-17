package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import g419.corpus.io.Tei;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.util.StringJoiner;

public class TeiFileTextWriter extends TeiFileWriter {

    public TeiFileTextWriter(final OutputStream stream, final String filename, final TeiPointerManager pointers, final String documentName) throws XMLStreamException {
        super(stream, filename, pointers);
        writelnStartElement(Tei.TAG_FRONT);
        writelnStartElement(Tei.TAG_TITLE);
        writelnElement(Tei.TAG_TITLEPART, ImmutableMap.of("type", "title", "xml:id", "titlePart-1"), documentName);
        writelnEndElement();
        writelnEndElement();
        writelnStartElement(Tei.TAG_BODY);
    }

    @Override
    public void writeDocument(Document document) throws XMLStreamException {
        for (Paragraph paragraph : document.getParagraphs()){
            writeParagraph(paragraph);
        }
    }

    private void writeParagraph(Paragraph paragraph) throws XMLStreamException {
        StringJoiner content = new StringJoiner(" ");
        paragraph.getSentences().stream().map(s -> s.toString()).forEach(content::add);
        writelnElement(Tei.TAG_PARAGRAPH, ImmutableMap.of("xml:id", paragraph.getId()), content.toString());
    }
}
