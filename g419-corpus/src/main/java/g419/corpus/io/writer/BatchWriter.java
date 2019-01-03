package g419.corpus.io.writer;

import g419.corpus.structure.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * @author Michał Marcińczuk
 */
public class BatchWriter extends AbstractDocumentWriter {

    private String outputRootFolder = null;
    private final String format;
    private String extension = ".txt";
    private final BufferedWriter indexWriter;
    private String gzExtension = "";

    /**
     * @param outputIndex -- path that will be appended to the document URI
     */
    public BatchWriter(final String outputIndex, final String format) throws IOException {
        this.format = format;
        boolean gz = false;

        String outputFormatNoGz = format;
        if (format.endsWith(":gz")) {
            outputFormatNoGz = format.substring(0, format.length() - 3);
            gz = true;
        }

        final File index = new File(outputIndex);
        if (!index.getAbsoluteFile().getParentFile().exists()) {
            index.getParentFile().mkdirs();
        }
        outputRootFolder = index.getAbsoluteFile().getParent();
        indexWriter = new BufferedWriter(new FileWriter(index, false));
        if (outputFormatNoGz.startsWith("ccl")) {
            extension = ".xml";
        }
        if (outputFormatNoGz.startsWith("tsv")) {
            extension = ".tsv";
        } else if (outputFormatNoGz.equals("iob")) {
            extension = ".iob";
        } else if (outputFormatNoGz.equals("tuples")) {
            extension = ".txt";
        } else if (outputFormatNoGz.equals("tokens")) {
            extension = ".txt";
        } else if (outputFormatNoGz.equals("arff")) {
            extension = ".arff";
        } else if (outputFormatNoGz.equals("tei")) {
            extension = "";
        } else if (outputFormatNoGz.equals("json-frames")) {
            extension = ".txt";
        } else if (outputFormatNoGz.equals("verb_eval")) {
            extension = ".az";
        } else if (outputFormatNoGz.equals("bsnlp")) {
            extension = ".txt";
        }

        if (!"tei".equals(outputFormatNoGz) && gz) {
            gzExtension += ".gz";
        }

    }

    @Override
    public void flush() {
        /** Nothing to do for this writer. */
    }

    @Override
    public void close() {
        try {
            indexWriter.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeDocument(final Document document) {
        final String name = document.getName();
        if (name == null) {
            System.err.println("Error: Document name is not specified (null value)");
        } else {
            final File file = new File(outputRootFolder, name + extension);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                final AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(file.getAbsolutePath(), format);
                writer.writeDocument(document);
                writer.close();
                indexWriter.write(name + extension + gzExtension + "\n");
                indexWriter.flush();
            } catch (final Exception e) {
                getLogger().error("Exception rised", e);
            }
        }

    }

}
