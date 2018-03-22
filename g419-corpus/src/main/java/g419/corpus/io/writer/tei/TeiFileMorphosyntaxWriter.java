package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.io.Tei;
import g419.corpus.structure.*;
import org.apache.commons.lang3.tuple.Pair;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TeiFileMorphosyntaxWriter extends TeiFileWriter {

    public TeiFileMorphosyntaxWriter(OutputStream stream, String filename, TeiPointerManager pointers) throws XMLStreamException {
        super(stream, filename, pointers, ImmutableMap.of("xml:lang", "pl"));
        writelnStartElement(Tei.TAG_BODY);
    }

    @Override
    public void writeDocument(final Document document) throws XMLStreamException {
        for (final Paragraph paragraph : document.getParagraphs()) {
            writeParagraph(paragraph);
        }
    }

    private void writeParagraph(final Paragraph paragraph) throws XMLStreamException {
        pointers.addPointer(paragraph, String.format("%s#%s", filename, paragraph.getId()));
        writelnStartElement(Tei.TAG_PARAGRAPH, ImmutableMap.of("xml:id", paragraph.getId()));
        for (final Sentence sentence : paragraph.getSentences()) {
            writeSentence(sentence);
        }
        writelnEndElement();
    }

    private void writeSentence(final Sentence sentence) throws XMLStreamException {
        pointers.addPointer(sentence, String.format("%s#%s", filename, sentence.getId()));
        writelnStartElement(Tei.TAG_SENTENCE,
                ImmutableMap.of("corresp", "ann_segmentation.xml#" + sentence.getId(), "xml:id", sentence.getId()));

        int tokenStart = 0;
        boolean noSpaceBefore = false;
        for (final Token token : sentence.getTokens()) {
            writeToken(token, sentence.getId(), tokenStart, noSpaceBefore, sentence.getAttributeIndex());
            tokenStart += token.getOrth().length();
            tokenStart += token.getNoSpaceAfter() ? 0 : 1;
            noSpaceBefore = token.getNoSpaceAfter();
        }
        writelnEndElement();
    }

    private void writeToken(final Token token, final String sentenceid, final int tokenStart, final boolean noSpaceBefore,
                            TokenAttributeIndex attributeIndex) throws XMLStreamException {
        pointers.addPointer(token, String.format("%s#%s", filename, token.getId()));

        writelnStartElement(Tei.TAG_SEGMENT,
                ImmutableMap.of("corresp", "ann_segmentation.xml#" + token.getId(), "xml:id", token.getId()));
        writelnStartElement(Tei.TAG_FEATURESET, ImmutableMap.of("type", "morph"));
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "orth"));
        writelnElement(Tei.TAG_STRING, Maps.newHashMap(), token.getOrth());
        writelnEndElement();

        if (noSpaceBefore) {
            writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "nps"));
            writelnEmptyElement(Tei.TAG_BINARY, ImmutableMap.of("value", "true"));
            writelnEndElement();
        }
        writelnComment(String.format("%s [%s,%s]", token.getOrth(), tokenStart, token.getOrth().length()));
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "interps"));

        Interps interps = new Interps(token.getTags());
        writeTags(interps, token.getId());
        writelnEndElement();

        String tagTool = Optional.ofNullable(attributeIndex.getAttributeValue(token, "tagTool")).orElse("#unknown");
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "disamb"));
        writelnStartElement(Tei.TAG_FEATURESET, ImmutableMap.of("feats", tagTool, "type", "tool_report"));
        writelnEmptyElement(Tei.TAG_FEATURE,
                ImmutableMap.of("fVal", "#" + token.getId() + "_" + interps.getDisambIdx() + "-msd", "name", "choice"));
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "interpretation"));
        writelnElement(Tei.TAG_STRING, Maps.newHashMap(), interps.getDisamb());
        for (int i = 10; i > 5; i--) {
            writelnEndElement();
        }
    }

    private void writeTags(final Interps interps, final String morphId) throws XMLStreamException {
        if (interps.getLexemes().size() > 1) {
            writelnStartElement(Tei.TAG_VALT);
        }
        int lexCount = 0;
        List<TeiLex> lexs = Lists.newArrayList(interps.getLexemes());
        Collections.sort(lexs, Comparator.comparing(TeiLex::getBase).thenComparing(TeiLex::getCtag));
        for (TeiLex lex : lexs) {
            writeLexeme(lex, morphId, String.format("%s_%d-lex", morphId, lexCount++));
        }
        if (interps.getLexemes().size() > 1) {
            writelnEndElement();
        }
    }

    private void writeLexeme(final TeiLex lex, final String morphId, final String lexId) throws XMLStreamException {
        writelnStartElement(Tei.TAG_FEATURESET, ImmutableMap.of("type", "lex", "xml:id", lexId));
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "base"));
        writelnElement(Tei.TAG_STRING, Maps.newHashMap(), lex.getBase());
        writelnEndElement();
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "ctag"));
        writelnEmptyElement(Tei.TAG_SYMBOL, ImmutableMap.of("value", lex.getCtag()));
        writelnEndElement();
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "msd"));
        if (lex.msdSize() > 1) {
            writelnStartElement(Tei.TAG_VALT);
        }
        for (Pair<String, Integer> entry : lex.getMsds()) {
            String msd = entry.getLeft();
            int msdIdx = entry.getRight();
            writelnEmptyElement(Tei.TAG_SYMBOL,
                    ImmutableMap.of("value", msd, "xml:id", morphId + "_" + msdIdx + "-msd"));
        }
        if (lex.msdSize() > 1) {
            writelnEndElement();
        }
        writelnEndElement();
        writelnEndElement();
    }
}
