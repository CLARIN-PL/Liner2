package g419.corpus.io.writer;

import g419.corpus.structure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public class AnnotationTupleStreamWriter extends AbstractDocumentWriter {
    final private BufferedWriter ow;
    private int sentenceOffset = 0;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AnnotationTupleStreamWriter(final OutputStream os) {
        ow = new BufferedWriter(new OutputStreamWriter(os));
    }

    @Override
    public void close() {
        flush();
        try {
            ow.close();
        } catch (final IOException ex) {
            logger.error("Failed to close AnnotationTupleStreamWriter", ex);
        }
    }

    @Override
    public void writeDocument(final Document document) {
        sentenceOffset = 0;
        document.getParagraphs().forEach(this::writeParagraph);
        flush();
    }

    public void writeParagraph(final Paragraph paragraph) {
        paragraph.getSentences().forEach(this::writeSentence);
    }

    private void writeSentence(final Sentence sentence) {
        Arrays.stream(Annotation.sortChunks(sentence.getChunks())).forEach(this::writeChunk);
        sentenceOffset += sentence.getTokens().stream().mapToInt(t -> t.getOrth().length()).sum();
    }

    private void writeChunk(final Annotation c) {
        try {
            ow.write(formatAnnotation(c) + "\n");
        } catch (final IOException ex) {
            logger.error("Failed to writeChunk", ex);
        }
    }

    private String formatAnnotation(final Annotation c) {
        final List<Token> tokens = c.getSentence().getTokens();
        final int begin = sentenceOffset + IntStream.range(0, c.getBegin()).map(i -> tokens.get(i).getOrth().length()).sum();
        final int end = begin + IntStream.rangeClosed(c.getBegin(), c.getEnd()).map(i -> tokens.get(i).getOrth().length()).sum() - 1;
        final StringJoiner joiner = new StringJoiner(",", "(", ")");
        joiner.add("" + begin);
        joiner.add("" + end);
        joiner.add(c.getGroup());
        joiner.add(c.getType());
        joiner.add(quote(c.getText()));
        joiner.add(quote(c.getLemmaOrText()));
        return joiner.toString();
    }

    private String quote(final String text) {
        return String.format("\"%s\"", text);
    }

    @Override
    public void flush() {
        try {
            ow.flush();
        } catch (final IOException ex) {
            logger.error("Failed to flush AnnotationTupleStreamWriter", ex);
        }
    }
}
