package g419.corpus.io.writer;

import com.cedarsoftware.util.DeepEquals;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class CclStreamWriterTest extends TestCase {

    AbstractDocumentReader reader;
    InputStream inputStream;
    CclStreamWriter writer;
    ByteArrayOutputStream outputStream;

    public void setUp() throws Exception {
        inputStream = this.getClass().getClassLoader().getResourceAsStream("with_props.xml");
        reader = ReaderFactory.get().getStreamReader("with_props.xml", inputStream, "ccl");
        outputStream = new ByteArrayOutputStream();
        writer = new CclStreamWriter(outputStream);
    }

    public void testReloading() throws Exception {
        Document original = reader.nextDocument();
        writer.writeDocument(original);
        ByteArrayInputStream buffer = new ByteArrayInputStream(outputStream.toByteArray());
        reader.close();
        reader = ReaderFactory.get().getStreamReader("with_props.xml", buffer, "ccl");
        Document reloaded = reader.nextDocument();
//        assertEquals(original, reloaded);
        assertTrue(DeepEquals.deepEquals(original, reloaded));
    }

    public void tearDown() throws Exception {
        inputStream.close();
        reader.close();
    }
}