package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.io.Tei;
import g419.corpus.structure.*;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.tuple.Pair;

public class TeiFileMorphosyntaxWriter extends TeiFileWriter {

  public TeiFileMorphosyntaxWriter(final OutputStream stream,
                                   final String filename,
                                   final TeiPointerManager pointers) throws XMLStreamException {
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
    writelnStartElement(Tei.TAG_SENTENCE, ImmutableMap.of(
        "corresp", "ann_segmentation.xml#" + sentence.getId(),
        "xml:id", sentence.getId()));

    int tokenStart = 0;
    boolean noSpaceBefore = false;
    for (final Token token : sentence.getTokens()) {
      writeToken(token, tokenStart, noSpaceBefore, sentence.getAttributeIndex());
      tokenStart += token.getOrth().length();
      tokenStart += token.getNoSpaceAfter() ? 0 : 1;
      noSpaceBefore = token.getNoSpaceAfter();
    }
    writelnEndElement();
  }

  private void writeToken(final Token token, final int tokenStart, final boolean noSpaceBefore,
                          final TokenAttributeIndex attributeIndex) throws XMLStreamException {
    pointers.addPointer(token, String.format("%s#%s", filename, token.getId()));

    writelnStartElement(Tei.TAG_SEGMENT,
        ImmutableMap.of(
            "corresp", "ann_segmentation.xml#" + token.getId(),
            "xml:id", token.getId()));
    writelnStartElement(Tei.TAG_FEATURESET, ImmutableMap.of("type", "morph"));
    writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "orth"));
    writelnElement(Tei.TAG_STRING, Maps.newHashMap(), token.getOrth());
    writelnEndElement();

    if (noSpaceBefore) {
      writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "nps"));
      writelnEmptyElement(Tei.TAG_BINARY, ImmutableMap.of("value", "true"));
      writelnEndElement();
    }
    writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "interps"));

    final Interps interps = new Interps(token.getTags());
    writeTags(interps);
    writelnEndElement();

    writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "disamb"));

    final Map<String, String> fsAttr = ImmutableMap.of("type", "tool_report");
    if (attributeIndex.getAttributeValue(token, "tagTool") != null) {
      fsAttr.put("tagTool", attributeIndex.getAttributeValue(token, "tagTool"));
    }
    writelnStartElement(Tei.TAG_FEATURESET, fsAttr);
    writelnEmptyElement(Tei.TAG_FEATURE, ImmutableMap.of("" +
            "fVal", "#" + getMsdId(interps.getDisambLex().getId(), interps.getDisambIdx() + 1),
        "name", "choice"));
    writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", "interpretation"));
    writelnElement(Tei.TAG_STRING, Maps.newHashMap(), interps.getDisambLex().getFullCtag());
    for (int i = 10; i > 5; i--) {
      writelnEndElement();
    }
  }

  private void writeTags(final Interps interps) throws XMLStreamException {
//    if (interps.getLexemes().size() > 1) {
//      writelnStartElement(Tei.TAG_VALT);
//    }
    final List<TeiLex> lexs = Lists.newArrayList(interps.getLexemes());
    Collections.sort(lexs, Comparator.comparing(TeiLex::getBase).thenComparing(TeiLex::getCtag));
    for (final TeiLex lex : lexs) {
      writeLexeme(lex);
    }
//    if (interps.getLexemes().size() > 1) {
//      writelnEndElement();
//    }
  }

  private void writeLexeme(final TeiLex lex) throws XMLStreamException {
    writelnStartElement(Tei.TAG_FEATURESET,
        ImmutableMap.of("type", "lex", "xml:id", lex.getId()));
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
    for (final Pair<String, Integer> entry : lex.getMsds()) {
      final String msd = entry.getLeft();
      final int msdIdx = entry.getRight();
      writelnEmptyElement(Tei.TAG_SYMBOL,
          ImmutableMap.of("value", msd, "xml:id", getMsdId(lex.getId(), 1)));
    }
    if (lex.msdSize() > 1) {
      writelnEndElement();
    }
    writelnEndElement();
    writelnEndElement();
  }

  private String getMsdId(final String lexId, final int msdIdx) {
    String msdId = lexId;
    if (msdId.endsWith("-lex")) {
      msdId = msdId.substring(0, msdId.length() - 4);
    }
    msdId += "." + msdIdx + "-msd";
    return msdId;
  }
}
