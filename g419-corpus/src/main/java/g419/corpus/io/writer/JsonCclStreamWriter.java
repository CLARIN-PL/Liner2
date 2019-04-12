package g419.corpus.io.writer;

import g419.corpus.structure.Document;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Json like ccl
 *
 * @author
 */
public class JsonCclStreamWriter extends AbstractDocumentWriter {
    private BufferedWriter ow = null;
    private OutputStream os = null;

    public JsonCclStreamWriter(OutputStream os) {
        this.os = os;
        this.ow = new BufferedWriter(new OutputStreamWriter(os));
    }

    @Override
    public void writeDocument(Document document) {
        try {
            this.ow.write(document.toJson().toString());
            this.ow.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void flush() {
        try {
            ow.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            this.ow.flush();
            this.ow.close();
            this.os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
