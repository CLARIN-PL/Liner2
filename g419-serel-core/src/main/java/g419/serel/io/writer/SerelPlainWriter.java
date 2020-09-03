package g419.serel.io.writer;

import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.structure.Document;
import g419.liner2.core.tools.parser.MaltParser;
import g419.serel.converter.DocumentToSerelExpressionConverter;
import g419.serel.formatter.ISerelExpressionFormatter;
import g419.serel.formatter.SerelExpressionFormatterPlain;
import g419.serel.structure.SerelExpression;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;


@Slf4j
public class SerelPlainWriter extends AbstractDocumentWriter {
  private final BufferedWriter ow;
  final ISerelExpressionFormatter formatter = new SerelExpressionFormatterPlain();

  DocumentToSerelExpressionConverter converter;

  public SerelPlainWriter(final OutputStream os, MaltParser maltParser) {
    ow = new BufferedWriter(new OutputStreamWriter(os));
    formatter.getHeader().forEach(this::writeLine);
    converter = new DocumentToSerelExpressionConverter(maltParser);
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
    final List<SerelExpression> serelExpressions = converter.convert(document);
    log.error("SE number = "+serelExpressions.size());
    formatter.format(document, serelExpressions)
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
