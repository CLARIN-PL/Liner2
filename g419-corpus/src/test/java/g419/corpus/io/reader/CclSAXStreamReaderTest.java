package g419.corpus.io.reader;

import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.DocumentDescriptor;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class CclSAXStreamReaderTest extends TestCase{
    AbstractDocumentReader reader;
    InputStream stream;
    DocumentDescriptor expectedDescriptor;

    public void setUp() throws Exception {
        stream = this.getClass().getClassLoader().getResourceAsStream("with_props.xml");
        URL url =  this.getClass().getClassLoader().getResource("with_props.xml");
        reader = ReaderFactory.get().getStreamReader(new File(url.toURI()).getPath(), stream, "ccl");
        expectedDescriptor = new DocumentDescriptor();
        expectedDescriptor.setDescription("id", "with_props");
        expectedDescriptor.setDescription("date", "some_date");
        expectedDescriptor.setDescription("title", "some_title");
        expectedDescriptor.setMetadata("subject", "some_subject");
        expectedDescriptor.setMetadata("keywords", "kw1, kw2, kw3");
        expectedDescriptor.setMetadata("licence", "any license");
    }

    public void testReading() throws Exception {
        Document document = reader.nextDocument();
        List<Annotation> annotations = document.getAnnotations();
        assertEquals(expectedDescriptor, document.getDocumentDescriptor());
        assertEquals(annotations.size(), 1);
        assertEquals(annotations.get(0).getMetadata().size(), 1);
        assertTrue(annotations.get(0).getMetadata().keySet().contains("lemma"));
        assertTrue(annotations.get(0).getMetadata("lemma").equals("2005-03-21"));

    }

    public void tearDown() throws IOException, DataFormatException {
        stream.close();
        reader.close();
    }
}