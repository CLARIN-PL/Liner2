package g419.corpus.io.writer;

import g419.corpus.TerminateException;
import g419.corpus.structure.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.StringJoiner;


public class IobStreamWriter extends AbstractDocumentWriter {

    private final BufferedWriter ow;
    private boolean init = false;

    public IobStreamWriter(final OutputStream os) {
        ow = new BufferedWriter(new OutputStreamWriter(os));
    }

    protected void init(final TokenAttributeIndex attributeIndex) {
        if (init) {
            return;
        }
        try {
            String line = "-DOCSTART CONFIG FEATURES";
            for (int i = 0; i < attributeIndex.getLength(); i++) {
                line += " " + attributeIndex.getName(i);
            }
            ow.write(line, 0, line.length());
            ow.newLine();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        init = true;
    }

    @Override
    public void flush() {
        try {
            ow.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            ow.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void writeDocument(final Document document) {
        for (final Paragraph paragraph : document.getParagraphs()) {
            writeParagraph(paragraph);
        }
    }

    public void writeParagraph(final Paragraph paragraph) {
        try {
            if (!init) {
                init(paragraph.getAttributeIndex());
            }
            String paragraphId = paragraph.getId();
            if (paragraphId == null) {
                paragraphId = "";
            }
            final String header = "-DOCSTART FILE " + paragraphId;
            ow.write(header, 0, header.length());
            ow.newLine();
            for (final Sentence sentence : paragraph.getSentences()) {
                writeSentence(sentence);
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    private void writeSentence(final Sentence sentence) throws IOException {
        final List<Token> tokens = sentence.getTokens();
        for (int i = 0; i < tokens.size(); i++) {
            writeToken(i, tokens.get(i), sentence);
        }
        ow.newLine();
    }

    private void writeToken(final int idx, final Token token, final Sentence sentence)
            throws IOException {
        final StringJoiner line = new StringJoiner("\t");
        for (int i = 0; i < sentence.getAttributeIndex().getLength(); i++) {
            try {
                line.add(token.getAttributeValue(i));
            } catch (final IndexOutOfBoundsException e) {
                throw new TerminateException(String.format("Token attribute with index %d not found in [%s]", i, token.getAttributesAsString()));
            }
        }
        line.add(sentence.getTokenClassLabel(idx, null));
        ow.write(line.toString(), 0, line.length());
        ow.newLine();
    }
}
