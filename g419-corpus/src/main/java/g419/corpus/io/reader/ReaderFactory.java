package g419.corpus.io.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

public class ReaderFactory {

  private static final ReaderFactory factory = new ReaderFactory();

  public static ReaderFactory get() {
    return ReaderFactory.factory;
  }

  public AbstractDocumentReader getStreamReader(String inputFile, String inputFormat) throws Exception {
    boolean gz = false;
    String intpuFormatNoGz = inputFormat;
    if (inputFormat.endsWith(":gz")) {
      gz = true;
      intpuFormatNoGz = inputFormat.substring(0, inputFormat.length() - 3);
    }

    if (inputFile == null) {
      return getStreamReader("System.in", System.in, null, intpuFormatNoGz, gz);
    } else if (intpuFormatNoGz.equals("tei")) {
      return getTEIStreamReader(inputFile, inputFile, gz);
    } else if (intpuFormatNoGz.startsWith("batch:")) {
      String format = inputFormat.substring(6);
      String root = new File(inputFile).getAbsoluteFile().getParent();
      return new BatchReader(new FileInputStream(inputFile), root, format);
    } else {
      return getStreamReader(inputFile, new FileInputStream(inputFile), (new File(inputFile)).getParent(), intpuFormatNoGz, gz);
    }
  }

  public AbstractDocumentReader getStreamReader(String uri, InputStream in, String inputFormat) throws Exception {
    return getStreamReader(uri, in, "", inputFormat);
  }

  public AbstractDocumentReader getStreamReader(String uri, InputStream in, String root, String inputFormat) throws Exception {
    boolean gz = false;
    if (inputFormat.endsWith(":gz")) {
      gz = true;
      inputFormat = inputFormat.substring(0, inputFormat.length() - 3);
    }
    return this.getStreamReader(uri, in, root, inputFormat, gz);
  }

  public AbstractDocumentReader getStreamReader(String uri, InputStream in, String root, String inputFormat, boolean gz) throws Exception {
    if (gz) {
      in = new GZIPInputStream(in);
    }
    if (inputFormat.equals("ccl")) {
      InputStream desc = null;
      String cclPath = Paths.get(uri).getFileName().toString();
      if (cclPath.endsWith(".xml")) {
        // Sama podmiana bez sprawdzenia rozszerzenia powoduje, że dla pliku z innym rozszerzenie niż xml
        // jako ini brany jest plik ccl.
        desc = getInputStream(root, Paths.get(uri).getFileName().toString().replace(".xml", ".ini"), gz);
      } else if (cclPath.endsWith(".tag")) {
        desc = getInputStream(root, Paths.get(uri).getFileName().toString().replace(".tag", ".ini"), gz);
      }
      return new CclSAXStreamReader(uri, in, desc, null);
    } else if (inputFormat.equals("cclrel")) {
      InputStream rel = getInputStream(root, uri.replace(root, "").replace(".xml", ".rel.xml"), gz);
      InputStream desc = getInputStream(root, Paths.get(uri).getFileName().toString().replace(".xml", ".ini"), gz);
      return new CclSAXStreamReader(uri, in, desc, rel);
    } else if (inputFormat.equals("cclrelr")) {
      InputStream rel = getInputStream(root, uri.replace(".xml", ".rel_r"), gz);
      InputStream desc = getInputStream(root, Paths.get(uri).getFileName().toString().replace(".xml", ".ini"), gz);
      return new CclSAXStreamReader(uri, in, desc, rel);
    } else if (inputFormat.equals("cclrelcls")) {
      InputStream rel = getInputStream(root, uri.replace(".xml", ".rel_cls"), gz);
      InputStream desc = getInputStream(root, Paths.get(uri).getFileName().toString().replace(".xml", ".ini"), gz);
      return new CclSAXStreamReader(uri, in, desc, rel);
    } else if (inputFormat.equals("iob")) {
      return new IobStreamReader(in);
    } else if (inputFormat.equals("csv")) {
      return new CsvStreamReader(in);
    } else if (inputFormat.equals("plain")) {
      return new PlainTextStreamReader(uri, in, "none");
    } else if (inputFormat.equals("plain:maca")) {
      return new PlainTextStreamReader(uri, in, "maca");
    } else if (inputFormat.equals("plain:wcrft")) {
      return new PlainTextStreamReader(uri, in, "wcrft");
    } else {
      throw new Exception("Input format " + inputFormat + " not recognized.");
    }
  }

  /**
   * @param inputFolder
   * @param docname
   * @return
   * @throws Exception
   */
  public AbstractDocumentReader getTEIStreamReader(String inputFolder, String docname) throws Exception {
    return this.getTEIStreamReader(inputFolder, docname, false);
  }

  /**
   * Creates reader for a document in the TEI format --
   *
   * @param inputFolder
   * @param docname
   * @return
   * @throws Exception
   */
  public AbstractDocumentReader getTEIStreamReader(String inputFolder, String docname, boolean gz) throws Exception {
    final InputStream annMorphosyntax = getInputStream(inputFolder, "ann_morphosyntax.xml", gz);
    final InputStream annSegmentation = getInputStream(inputFolder, "ann_segmentation.xml", gz);
    final InputStream annNamed = getInputStream(inputFolder, "ann_named.xml", gz);
    final InputStream annMentions = getInputStream(inputFolder, "ann_mentions.xml", gz);
    final InputStream annChunks = getInputStream(inputFolder, "ann_chunks.xml", gz);
    final InputStream annAnnotations = getInputStream(inputFolder, "ann_annotations.xml", gz);
    final InputStream annCoreference = getInputStream(inputFolder, "ann_coreference.xml", gz);
    final InputStream annGroups = getInputStream(inputFolder, "ann_groups.xml", gz);
    final InputStream annWords = getInputStream(inputFolder, "ann_words.xml", gz);
    final InputStream annRelations = getInputStream(inputFolder, "ann_relations.xml", gz);
    final InputStream annProps = getInputStream(inputFolder, "ann_props.xml", gz);
    final InputStream annMetadata = getInputStream(inputFolder, "metadata.xml", gz);
    return new TeiStreamReader(inputFolder, annMetadata, annMorphosyntax, annProps, annSegmentation,
        annNamed, annMentions, annChunks, annAnnotations,
        annCoreference, annWords, annGroups, annRelations, docname);
  }

  /**
   * Tworzy strumień do wczytania danych.
   * Jeżeli inputFile jest null to strumień danych wczytywany jest z System.in.
   * Jeżeli inputFile jest nazwą pliku, który nie istnieje to zwracany jest null.
   * Wpp. dane wczytywane są z pliku. Jeżeli gz jest true, to strumień zostaje opakowany w GZIPStreamInput.
   *
   * @param inputFile
   * @param gz
   * @return
   * @throws Exception
   */
  private InputStream getInputStream(String inputFolder, String inputFile, boolean gz) throws Exception {
    if (inputFile == null || inputFile.isEmpty()) {
      return System.in;
    } else {
      try {
        if (gz && !inputFile.endsWith(".gz")) {
          inputFile += ".gz";
        }
        File file = new File(inputFolder, inputFile); //todo: checking of existence returns always 'false' before ini reading!!
        InputStream stream = null;
        if (file.exists()) {
          stream = new FileInputStream(file);
          if (gz) {
            stream = new GZIPInputStream(stream);
          }
          return stream;
        } else {
          return null;
        }
      } catch (IOException ex) {
        throw new Exception("Unable to read input file: " + inputFile + " (" + ex.getMessage() + ")");
      }
    }
  }
}
