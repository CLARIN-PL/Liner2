package g419.spatial.io.writer;

import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.structure.Document;
import g419.spatial.converter.DocumentToSpatialExpressionConverter;
import g419.spatial.formatter.ISpatialExpressionFormatter;
import g419.spatial.formatter.SpatialExpressionFormatterTsv;
import g419.spatial.structure.SpatialExpression;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;


public class SpatialTsvWriter extends AbstractDocumentWriter {
  private final BufferedWriter ow;
  final DocumentToSpatialExpressionConverter converter = new DocumentToSpatialExpressionConverter();
  final ISpatialExpressionFormatter formatter = new SpatialExpressionFormatterTsv();

  public SpatialTsvWriter(final OutputStream os) {
    ow = new BufferedWriter(new OutputStreamWriter(os));
    formatter.getHeader().forEach(this::writeLine);
  }

  @Override
  public void close() {
    try {
      ow.flush();
      ow.close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void writeDocument(final Document document) {
    final List<SpatialExpression> spatialExpressions = converter.convert(document);
    formatter.format(document, spatialExpressions)
        .forEach(this::writeLine);
  }

  private void writeLine(final String line) {
    try {
      ow.write(line + "\n");
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void flush() {
    try {
      ow.flush();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
