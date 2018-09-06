package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import g419.corpus.io.Tei;
import g419.corpus.structure.*;
import io.vavr.control.Option;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TeiFileAnnotationsWriter extends TeiFileWriter {

    final String group;

    public TeiFileAnnotationsWriter(final OutputStream stream, final String filename,
                                    final TeiPointerManager pointers, final String group) throws XMLStreamException {
        super(stream, filename, pointers, ImmutableMap.of("xml:lang", "pl"));
        this.group = group;
        writelnStartElement(Tei.TAG_BODY);
    }

    public TeiFileAnnotationsWriter(final OutputStream stream, final String filename, final TeiPointerManager pointers) throws XMLStreamException {
        this(stream, filename, pointers, null);
    }

    @Override
    public void writeDocument(final Document document) throws XMLStreamException {
        for (final Paragraph paragraph : document.getParagraphs()) {
            writeParagraph(paragraph);
        }
    }

    private void writeParagraph(final Paragraph paragraph) throws XMLStreamException {
        writelnStartElement(Tei.TAG_PARAGRAPH,
                ImmutableMap.of(
                        "xml:id", Option.of(paragraph.getId()).getOrElse("").replace("morph", Option.of(group).getOrElse("")),
                        "corresp", pointers.getPointer(paragraph)));
        for (final Sentence sentence : paragraph.getSentences()) {
            writeSentence(sentence);
        }
        writelnEndElement();
    }

    private void writeSentence(final Sentence sentence) throws XMLStreamException {
        final List<Annotation> annotations = sentence.getChunks().stream()
                .filter(a -> !pointers.hasPointer(a))
                .filter(a -> Objects.equals(group, a.getGroup()))
                .collect(Collectors.toList());

        final ImmutableMap attrs = ImmutableMap.of(
                "xml:id", Option.of(sentence.getId()).getOrElse("").replace("morph", Option.of(group).getOrElse("")),
                "corresp", pointers.getPointer(sentence));
        if (annotations.size() == 0) {
            writelnEmptyElement(Tei.TAG_SENTENCE, attrs);
        } else {
            writelnStartElement(Tei.TAG_SENTENCE, attrs);
            for (final Annotation ann : sentence.getChunks()) {
                if (!pointers.hasPointer(ann) && Objects.equals(group, ann.getGroup())) {
                    writeAnnotation(ann);
                }
            }
            writelnEndElement();
        }
    }

    private void writeAnnotation(final Annotation ann) throws XMLStreamException {
        final String pointer = String.format("%s#%s", filename, ann.getId());
        pointers.addPointer(ann, pointer);
        writelnStartElement(Tei.TAG_SEGMENT, ImmutableMap.of("xml:id", "" + ann.getId()));
        writelnStartElement(Tei.TAG_FEATURESET, ImmutableMap.of("type", "named"));
        writeAnnotationType(ann);
        writeElementFeatureString("orth", ann.getText());
        writeElementFeatureString("base", ann.getBaseText());
        writelnEndElement();
        for (final Token token : ann.getTokenTokens()) {
            writelnEmptyElement(Tei.TAG_POINTER, ImmutableMap.of("target", pointers.getPointer(token)));
        }
        writelnEndElement();
    }

    private void writeAnnotationType(final Annotation ann) throws XMLStreamException {
        final String[] type = ann.getType().split("-");
        writeElementFeatureSymbol("type", type[0]);
        if (type.length == 2) {
            writeElementFeatureSymbol("subtype", type[1]);
        }
    }

}
