package g419.corpus.io.reader;

import g419.corpus.structure.Document;
import org.ini4j.Ini;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CclDescriptorReader {

  public void enhanceDocument(final Document document, final InputStream desc) throws IOException {
    final Ini ini = new Ini(desc);
    {
      final Ini.Section section = ini.get("document");
      if (section != null) {
        for (final Map.Entry<String, String> entry : section.entrySet()) {
          document.getDocumentDescriptor().setDescription(entry.getKey(), entry.getValue());
        }
      }
    }
    {
      final Ini.Section section = ini.get("metadata");
      if (section != null) {
        for (final Map.Entry<String, String> entry : section.entrySet()) {
          document.getDocumentDescriptor().setMetadata(entry.getKey(), entry.getValue());
        }
      }
    }
  }

}
