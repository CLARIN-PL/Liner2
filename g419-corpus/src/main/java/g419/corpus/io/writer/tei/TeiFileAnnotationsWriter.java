package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import g419.corpus.io.Tei;
import g419.corpus.structure.*;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

public class TeiFileAnnotationsWriter extends TeiFileWriter {

    final List<Pattern> typePatterns;

    public TeiFileAnnotationsWriter(final OutputStream stream, final String filename, final TeiPointerManager pointers,
                                    final List<Pattern> typePatterns) throws XMLStreamException {
        super(stream, filename, pointers, ImmutableMap.of("xml:lang", "pl"));
        writelnStartElement(Tei.TAG_BODY);
        this.typePatterns = typePatterns;
    }

    public TeiFileAnnotationsWriter(final OutputStream stream, final String filename, final TeiPointerManager pointers) throws XMLStreamException {
        this(stream,filename,pointers, null);
    }

    @Override
    public void writeDocument(final Document document) throws XMLStreamException {
        for (final Paragraph paragraph : document.getParagraphs()) {
            writeParagraph(paragraph);
        }
    }

    private void writeParagraph(final Paragraph paragraph) throws XMLStreamException {
        writelnStartElement(Tei.TAG_PARAGRAPH,
                ImmutableMap.of("xml:id", paragraph.getId(), "corresp", pointers.getPointer(paragraph)));
        for (final Sentence sentence : paragraph.getSentences()) {
            writeSentence(sentence);
        }
        writelnEndElement();
    }

    private void writeSentence(final Sentence sentence) throws XMLStreamException {
        writelnStartElement(Tei.TAG_SENTENCE,
                ImmutableMap.of("xml:id", sentence.getId(), "corresp", pointers.getPointer(sentence)));

        for (Annotation ann : (typePatterns==null ? sentence.getChunks() : sentence.getAnnotations(typePatterns))){
            if ( !pointers.hasPointer(ann) ) {
                writeAnnotation(ann);
            }
        }
        writelnEndElement();
    }

    private void writeAnnotation(final Annotation ann) throws XMLStreamException {
        String pointer = String.format("%s#%s", filename, ann.getId());
        this.pointers.addPointer(ann, pointer);
        writelnStartElement(Tei.TAG_SEGMENT, ImmutableMap.of("xml:id", "" + ann.getId()));
        writelnStartElement(Tei.TAG_FEATURESET, ImmutableMap.of("type", "named"));
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "type"));
        writelnEmptyElement(Tei.TAG_SYMBOL, ImmutableMap.of("value", ann.getType()));
        writelnEndElement();
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "orth"));
        writelnElement(Tei.TAG_STRING, Maps.newHashMap(), ann.getText());
        writelnEndElement();
        writelnEndElement();
        for (Token token : ann.getTokenTokens()) {
            writelnEmptyElement(Tei.TAG_POINTER, ImmutableMap.of("target", pointers.getPointer(token)));
        }
        writelnEndElement();
    }

}
