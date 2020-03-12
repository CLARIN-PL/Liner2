package g419.toolbox.wordnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import pl.wroc.pwr.ci.plwordnet.plugins.princetonadapter.da.PrincetonDataRaw;

/**
 * Klasa reprezentuje mapowanie kategorii jednostek identyfikacyjnych na koncepcje sumo.
 *
 * @author czuk
 */
public class NamToWordnet {

  private Wordnet3 wordnet = null;
  private final Map<String, Set<String>> mapping = new HashMap<>();

  public NamToWordnet(final Wordnet3 wordnet) throws IOException {
    this.wordnet = wordnet;
    final Reader reader = new InputStreamReader(getClass().getResourceAsStream("/sumo/nam2wordnet.txt"));
    parse(reader);
  }

  public NamToWordnet(final Reader reader, final Wordnet3 wordnet) throws IOException {
    this.wordnet = wordnet;
    parse(reader);
  }

  public void parse(final Reader reader) throws IOException {
    final BufferedReader br = new BufferedReader(reader);
    String line = null;
    while ((line = br.readLine()) != null) {
      line = line.trim();
      final String[] cols = line.split("\t");
      if (cols.length == 2) {
        final String type = cols[0];
        final String[] lexicalUnits = cols[1].split("[ ]*,[ ]*");
        final Set<String> lus = new HashSet<>();
        for (final String lu : lexicalUnits) {
          final String word = lu.substring(0, Math.max(lu.lastIndexOf(' '), 0));
          if (word.length() > 0) {
            lus.add(word);
          }
        }
        mapping.put(type, lus);
      }
    }
  }

  public Set<PrincetonDataRaw> getSynsets(final String type) {
    final Set<PrincetonDataRaw> synsets = new HashSet<>();
    if (mapping.containsKey(type)) {
      for (final String word : mapping.get(type)) {
        synsets.addAll(wordnet.getSynsets(word));
      }
    }
    return synsets;
  }

}