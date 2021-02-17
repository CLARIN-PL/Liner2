package g419.serel.wordnet;

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class WordnetPl32 extends WordnetPl {


  static WordnetPl instance;

  static String resourcePath = "/plwordnet-3.2.xml.gz";

  public static WordnetPl load() throws IOException, ParserConfigurationException, SAXException {
    final WordnetPl wordnet;
    try (
        final InputStream stream = WordnetPl32.class.getResourceAsStream(resourcePath);
        final InputStream streamGz = new GZIPInputStream(stream)
    ) {
      wordnet = WordnetXmlReader.load(streamGz);
      wordnet.updateLemmaToSynsetIndex();
      wordnet.updateSynsetDepth();
    }

    instance = wordnet;

    return wordnet;
  }

}