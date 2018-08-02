package g419.corpus.io.writer.tei;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import g419.corpus.io.Tei;
import g419.corpus.structure.Document;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public abstract class TeiFileWriter {

    final private OutputStream stream;
    final private XMLStreamWriter writer;
    final protected String filename;
    final protected TeiPointerManager pointers;
    private int indent = 0;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public TeiFileWriter(final OutputStream stream, final String filename, final TeiPointerManager pointers) throws XMLStreamException {
        this(stream, filename, pointers, Maps.newHashMap());
    }

    public TeiFileWriter(final OutputStream stream, final String filename, final TeiPointerManager pointers, final Map<String, String> attributes) throws XMLStreamException {
        this.stream = stream;
        this.filename = filename;
        this.pointers = pointers;
        writer = stream == null ? null : XMLOutputFactory.newFactory().createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(stream)));
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeCharacters("\n");
        writelnStartElement(Tei.TAG_CORPUS,
                ImmutableMap.of("xmlns", "http://www.tei-c.org/ns/1.0", "xmlns:xi", "http://www.w3.org/2001/XInclude", "xmlns:nkjp", "http://www.nkjp.pl/ns/1.0"));
        writelnStartElement(Tei.TAG_TEI);
        writelnStartElement(Tei.TAG_TEXT, attributes);
    }

    public abstract void writeDocument(final Document document) throws XMLStreamException;

    public void close() throws XMLStreamException, IOException {
        while (indent > 0) {
            writelnEndElement();
        }
        if (writer != null) {
            writer.close();
        }
        if (stream != null && stream instanceof GZIPOutputStream) {
            ((GZIPOutputStream) stream).finish();
        }
        if (stream != null) {
            stream.close();
        }
    }

    protected void writeIndent(final int size) throws XMLStreamException {
        writer.writeCharacters(StringUtils.repeat("  ", size));
    }

    protected void writelnComment(final String comment) throws XMLStreamException {
        writeIndent(indent);
        writer.writeComment(" " + comment.replaceAll("[-]+", "-") + " ");
        writeln();
    }

    protected void writelnEmptyElement(final String text, final Map<String, String> attributes) throws XMLStreamException {
        writeIndent(indent);
        writer.writeEmptyElement(text);
        writeAttributes(attributes);
        writeln();
    }

    protected void writelnElement(final String text, final Map<String, String> attributes, final String characters) throws XMLStreamException {
        writeIndent(indent);
        writer.writeStartElement(text);
        writeAttributes(attributes);
        writer.writeCharacters(characters);
        writer.writeEndElement();
        writeln();
    }

    protected void writelnStartElement(final String text) throws XMLStreamException {
        writeIndent(indent++);
        writer.writeStartElement(text);
        writeln();
    }

    protected void writelnStartElement(final String text, final Map<String, String> attributes) throws XMLStreamException {
        writeIndent(indent++);
        writer.writeStartElement(text);
        writeAttributes(attributes);
        writeln();
    }

    protected void writeAttributes(final Map<String, String> attributes) throws XMLStreamException {
        for (final Map.Entry<String, String> entry : attributes.entrySet()) {
            writer.writeAttribute(entry.getKey(), entry.getValue());
        }
    }

    protected void writelnEndElement() throws XMLStreamException {
        writeIndent(--indent);
        writer.writeEndElement();
        writeln();
    }

    protected void writeln() throws XMLStreamException {
        writer.writeCharacters("\n");
    }

    /**
     * Write the following structure to the xml file:
     * <pre>
     * <f name="NAME">
     *   <string>VALUE</string>
     * </f>
     * </pre>
     *
     * @param name
     * @param value
     */
    protected void writeElementFeatureString(final String name, final String value) throws XMLStreamException {
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", name));
        writelnElement(Tei.TAG_STRING, Maps.newHashMap(), value);
        writelnEndElement();
    }

    /**
     * Write the following structure to the xml file:
     * <pre>
     * <f name="NAME">
     *   <symbol value="VALUE"/>
     * </f>
     * </pre>
     *
     * @param name
     * @param value
     */
    protected void writeElementFeatureSymbol(final String name, final String value) throws XMLStreamException {
        writelnStartElement(Tei.TAG_FEATURE, ImmutableMap.of("name", name));
        writelnEmptyElement(Tei.TAG_SYMBOL, ImmutableMap.of("value", value));
        writelnEndElement();
    }

}
