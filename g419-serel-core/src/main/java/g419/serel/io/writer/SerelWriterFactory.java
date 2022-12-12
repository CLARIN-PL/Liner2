package g419.serel.io.writer;

import g419.corpus.io.UnknownFormatException;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.ParseTreeGenerator;
import g419.serel.io.SerelOutputFormat;
import java.io.OutputStream;
import java.io.PrintWriter;

public class SerelWriterFactory {

  public static AbstractDocumentWriter create(final SerelOutputFormat format,
                                              final OutputStream os,
                                              final ParseTreeGenerator ptg,
                                              PrintWriter report)
      throws UnknownFormatException {
    switch (format) {
      case TSV:
        return new SerelTsvWriter(os,ptg,report);
      case PLAIN:
        return new SerelPlainWriter(os,ptg,report);
    }
    throw new UnknownFormatException(format.toString());
  }
}
