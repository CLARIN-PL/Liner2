package g419.spatial.io.writer;

import g419.corpus.io.UnknownFormatException;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.spatial.io.SpatialOutputFormat;

import java.io.OutputStream;

public class SpatialWriterFactory {

  public static AbstractDocumentWriter create(final SpatialOutputFormat format, final OutputStream os)
      throws UnknownFormatException {
    switch (format) {
      case TSV:
        return new SpatialTsvWriter(os);
      case TREE:
        return new SpatialTreeWriter(os);
    }
    throw new UnknownFormatException(format.toString());
  }
}
