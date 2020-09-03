package g419.serel.io.writer;

import g419.corpus.io.UnknownFormatException;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.liner2.core.tools.parser.MaltParser;
import g419.serel.io.SerelOutputFormat;
import java.io.OutputStream;

public class SerelWriterFactory {

  public static AbstractDocumentWriter create(final SerelOutputFormat format,
                                              final OutputStream os,
                                              final MaltParser malt)
      throws UnknownFormatException {
    switch (format) {
      case TSV:
        return new SerelTsvWriter(os,malt);
//      case TREE:
//        return new SerelTreeWriter(os);
      case PLAIN:
        return new SerelPlainWriter(os,malt);
    }
    throw new UnknownFormatException(format.toString());
  }
}
