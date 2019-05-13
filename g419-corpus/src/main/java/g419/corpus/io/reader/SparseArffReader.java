package g419.corpus.io.reader;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author czuk
 */
public class SparseArffReader {


  public SparseArffReader(String filename) throws IOException {

    List<String> lines = Files.readAllLines(Paths.get(filename));
    List<String> features = new ArrayList<String>();
    boolean dataBlock = false;

    for (String line : lines) {
      line = line.trim();

      if (line.length() > 0) {
        if (dataBlock) {

        } else if (line.startsWith("@attribute")) {

        } else if (line.startsWith("@feature")) {

        } else if (line.equals("@data")) {
          dataBlock = true;
        }
      }
    }

  }

}
