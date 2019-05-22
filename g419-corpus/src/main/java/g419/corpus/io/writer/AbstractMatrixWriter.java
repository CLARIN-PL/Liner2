package g419.corpus.io.writer;

import java.io.IOException;
import java.util.List;

/**
 * Represent a generic write for matrix-based data.
 *
 * @author czuk
 */
public abstract class AbstractMatrixWriter {

  public abstract void writeHeader(String name, List<String> headers) throws IOException;

  public abstract void writeRow(List<String> values) throws IOException;

  public abstract void flush();

  public abstract void close();

}
