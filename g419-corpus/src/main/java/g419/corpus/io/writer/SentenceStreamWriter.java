package g419.corpus.io.writer;

import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class SentenceStreamWriter extends AbstractDocumentWriter {
    final private BufferedWriter ow;

    public SentenceStreamWriter(final OutputStream os) {
        ow = new BufferedWriter(new OutputStreamWriter(os));
    }

    int n = 1;

    @Override
    public void close() {
        flush();
        try {
            ow.close();
        } catch (final IOException ex) {
            getLogger().error("Failed to close AnnotationTupleStreamWriter", ex);
        }
    }

    @Override
    public void writeDocument(final Document document) {
        document.getParagraphs().forEach(this::writeParagraph);
        flush();
    }

    public void writeParagraph(final Paragraph paragraph) {
        paragraph.getSentences().forEach(this::writeSentence);
    }

    private void writeSentence(final Sentence sentence) {
        try {
            ow.write(n++ + " ");
            ow.write(sentence.toString() + "\n");
        } catch (final IOException ex) {
            getLogger().error("Failed to writeChunk", ex);
        }
    }

    @Override
    public void flush() {
        try {
            ow.flush();
        } catch (final IOException ex) {
            getLogger().error("Failed to flush AnnotationTupleStreamWriter", ex);
        }
    }
}
