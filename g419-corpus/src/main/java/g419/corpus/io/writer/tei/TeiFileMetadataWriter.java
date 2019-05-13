package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import g419.corpus.io.Tei;
import g419.corpus.structure.Document;

import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;
import java.util.Map;

public class TeiFileMetadataWriter extends TeiFileWriter {

  public TeiFileMetadataWriter(OutputStream stream, String filename, TeiPointerManager pointers) throws XMLStreamException {
    super(stream, filename, pointers, ImmutableMap.of("xml:lang", "pl"));
    writelnStartElement(Tei.TAG_BODY);
  }

  @Override
  public void writeDocument(Document document) throws XMLStreamException {
    for (Map.Entry<String, String> kv : document.getDocumentDescriptor().getMetadata().entrySet()) {
      writelnEmptyElement(Tei.TAG_METADATA, ImmutableMap.of("name", kv.getKey(), "value", kv.getValue()));
    }
  }
}
