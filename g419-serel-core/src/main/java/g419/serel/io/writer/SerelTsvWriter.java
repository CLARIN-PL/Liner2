package g419.serel.io.writer;

import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.structure.Document;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.ParseTreeGenerator;
import g419.serel.converter.DocumentToSerelExpressionConverter;
import g419.serel.formatter.ISerelExpressionFormatter;
import g419.serel.formatter.SerelExpressionFormatterTsv;
import g419.serel.structure.SerelExpression;
import java.io.*;
import java.util.List;


public class SerelTsvWriter extends AbstractDocumentWriter {
  private final BufferedWriter ow;
  DocumentToSerelExpressionConverter converter;
  final ISerelExpressionFormatter formatter = new SerelExpressionFormatterTsv();
  private PrintWriter reportFile;

  public SerelTsvWriter(final OutputStream os, ParseTreeGenerator ptg,  PrintWriter report) {
    ow = new BufferedWriter(new OutputStreamWriter(os));
    formatter.getHeader().forEach(this::writeLine);
    converter = new DocumentToSerelExpressionConverter(ptg, report);
    reportFile = report;
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
