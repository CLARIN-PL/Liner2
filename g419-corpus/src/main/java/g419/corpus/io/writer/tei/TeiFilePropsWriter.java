package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import g419.corpus.io.Tei;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.util.Map;

public class TeiFilePropsWriter extends TeiFileWriter {

  public TeiFilePropsWriter(final OutputStream stream, final String filename, final TeiPointerManager pointers) throws XMLStreamException {
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
    for (final Sentence sentence : paragraph.getSentences()) {
      writeSentence(sentence);
    }
  }

  private void writeSentence(final Sentence sentence) throws XMLStreamException {
    for (final Token token : sentence.getTokens()) {
      writeToken(token);
    }
  }

  private void writeToken(final Token token) throws XMLStreamException {
    String morphId = "morph_" + token.getId();
    if (token.getProps().size() > 0) {
      writelnStartElement(Tei.TAG_SEGMENT, ImmutableMap.of(Tei.ATTR_CORESP, pointers.getPointer(token)));
      for (Map.Entry<String, String> kv : token.getProps().entrySet()) {
        writelnElement(Tei.TAG_FEATURE, ImmutableMap.of(Tei.ATTR_NAME, kv.getKey()), kv.getValue());
      }
      writelnEndElement();
    }
  }
}
