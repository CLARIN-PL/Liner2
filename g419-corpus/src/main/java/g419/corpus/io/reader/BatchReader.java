package g419.corpus.io.reader;

import com.google.common.collect.Lists;
import g419.corpus.io.DataFormatException;
import g419.corpus.structure.Document;
import g419.corpus.structure.TokenAttributeIndex;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.List;
import java.util.NoSuchElementException;


/**
 *
 */
public class BatchReader extends AbstractDocumentReader {

  private final TokenAttributeIndex attributeIndex;
  private int fileIndex = 0;
  private final List<String> files = Lists.newArrayList();
  private final File root;
  private final String format;

  /**
   * @param is   — the stream contains relative or absolute paths to ccl files,
   * @param root — absolute path to a root for absolute paths from the stream,
   * @throws DataFormatException
   * @throws IOException
   * @throws FileNotFoundException
   */
  public BatchReader(final InputStream is, final String root, final String format) throws DataFormatException, IOException {
    this.root = new File(root);
    this.format = format;
    attributeIndex = new TokenAttributeIndex();
    attributeIndex.addAttribute("orth");
    attributeIndex.addAttribute("base");
    attributeIndex.addAttribute("ctag");

    final BufferedReader ir = new BufferedReader(new InputStreamReader(is));
    while (true) {
      final String line;
      try {
        line = ir.readLine();
      } catch (final IOException ex) {
        throw new DataFormatException("I/O error.");
      }
      if (line == null) {
        break;
      }
      final String name = line.trim().split(";")[0];
      String cclFile = name;
      if (cclFile.length() == 0) {
        break;
      }

      if (!cclFile.startsWith("/")) {
        cclFile = new File(this.root, cclFile).getAbsolutePath();
      }

      if (!new File(cclFile).exists()) {
        getLogger().error("File not found while reading batch: {}", cclFile);
      } else {
        files.add(name);
      }
    }
    ir.close();
  }

  @Override
  public Document nextDocument() throws Exception {
    while (fileIndex < files.size()) {
      String name = files.get(fileIndex++);
      final String path;
      if (name.startsWith("/")) {
        path = name;
        final File tmp = new File(path);
        name = tmp.getName();
      } else {
        path = new File(root, name).getAbsolutePath();
      }
      try {
        getLogger().info("Reading {} from {}: {}", fileIndex, files.size(), path);
        final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(path, format);
        final Document document = reader.nextDocument();
        document.setName(getDocumnetBaseName(name, format));
        reader.close();
        return document;
      } catch (final Exception ex) {
        getLogger().error("Failed to read file {}", path);
      }
    }
    return null;
  }

  private String getDocumnetBaseName(final String filename, final String format) {
    String name = filename;
    if (name.endsWith(".gz")) {
      name = name.substring(0, name.length() - 3);
    }
    if (!"tei".equals(format)) {
      name = FilenameUtils.removeExtension(name);
    }
    return name;
  }

  @Override
  protected TokenAttributeIndex getAttributeIndex() {
    return attributeIndex;
  }

  @Override
  public void close() {
  }

  @Override
  public boolean hasNext() {
    return fileIndex < files.size();
  }

}
