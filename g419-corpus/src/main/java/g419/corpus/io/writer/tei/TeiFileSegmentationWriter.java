package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import g419.corpus.io.Tei;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import java.io.OutputStream;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

public class TeiFileSegmentationWriter extends TeiFileWriter {

  public TeiFileSegmentationWriter(final OutputStream stream,
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
    writelnStartElement(Tei.TAG_PARAGRAPH,
        ImmutableMap.of("corresp", "text.xml#" + paragraph.getId(), "xml:id", paragraph.getId()));
    int paragraphOffset = 0;
    for (final Sentence sentence : paragraph.getSentences()) {
      paragraphOffset = writeSentence(sentence, paragraph.getId(), paragraphOffset);
    }
    writelnEndElement();
  }

  private int writeSentence(final Sentence sentence,
                            final String paragraphId,
                            final int paragraphIndex) throws XMLStreamException {
    writelnStartElement(Tei.TAG_SENTENCE, ImmutableMap.of("xml:id", paragraphId));
    int tokenStart = paragraphIndex;
    boolean noSpaceBefore = false;
    for (final Token token : sentence.getTokens()) {
      writeToken(token, paragraphId, tokenStart, noSpaceBefore);
      tokenStart += token.getOrth().length();
      tokenStart += token.getNoSpaceAfter() ? 0 : 1;
      noSpaceBefore = token.getNoSpaceAfter();
    }
    writelnEndElement();
    return tokenStart;
  }

  private void writeToken(final Token token,
                          final String sentenceid,
                          final int tokenStart,
                          final boolean noSpaceBefore) throws XMLStreamException {
    writelnComment(token.getOrth());
    final Map<String, String> attributes = Maps.newHashMap();
    attributes.put("corresp", String.format("text.xml#string-range(%s,%d,%d)", sentenceid, tokenStart, token.getOrth().length()));
    if (noSpaceBefore) {
      attributes.put("nkjp:nps", "true");
    }
    attributes.put("xml:id", "segm_" + token.getId());
    writelnEmptyElement(Tei.TAG_SEGMENT, attributes);
  }
}
