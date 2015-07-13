package g419.corpus.io.reader;

import g419.corpus.structure.Document;
import org.ini4j.Ini;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CclDescriptorReader {
    public void enhanceDocument(Document document, InputStream desc) throws IOException {
        Ini ini = new Ini(desc);
        Ini.Section section = ini.get("document");
        for (Map.Entry<String, String> entry: section.entrySet())
            document.getDocumentDescriptor().setDescription(entry.getKey(), entry.getValue());
        section = ini.get("metadata");
        for (Map.Entry<String, String> entry: section.entrySet())
            document.getDocumentDescriptor().setMetadata(entry.getKey(), entry.getValue());
    }
}
