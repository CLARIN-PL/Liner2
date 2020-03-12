package g419.toolbox.wordnet;

import g419.toolbox.wordnet.struct.WordnetPl;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class WordnetPl30 extends WordnetPl {

  static String resourcePath = "/plwordnet-3.0.xml.gz";

  public static WordnetPl load() throws IOException, ParserConfigurationException, SAXException {
    final WordnetPl wordnet;
    try (
        final InputStream stream = WordnetPl30.class.getResourceAsStream(resourcePath);
        final InputStream streamGz = new GZIPInputStream(stream)
    ) {
      wordnet = WordnetXmlReader.load(streamGz);
    }
    return wordnet;
  }

}
