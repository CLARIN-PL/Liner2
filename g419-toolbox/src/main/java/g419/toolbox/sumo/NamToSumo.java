package g419.toolbox.sumo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * Klasa reprezentuje mapowanie kategorii jednostek identyfikacyjnych na koncepcje sumo.
 *
 * @author czuk
 */
public class NamToSumo {

  private Sumo sumo = null;
  private final Map<String, Set<String>> mapping = new HashMap<>();

  /**
   * Tworzy domyślne mapowanie z wykorzystaniem domyślnej ontologii SUMO.
   *
   * @throws IOException
   */
  public NamToSumo() throws IOException {
    sumo = new Sumo(false);
    final Reader reader = new InputStreamReader(getClass().getResourceAsStream("/sumo/nam2sumo.txt"));
    parse(reader);
  }

  /**
   * Mapowanie wczytywanie jest ze strumienia reader, w którym każda linia zawiera mapowanie
   * jednej kategorii na listę konceptów z SUMO. Przykład linii z mapowaniem:
   * nam_org_company	Business, CommercialAgent
   * Nazwa kategorii i lista konceptów oddzielone są znakiem tabulacji. Koncepty oddzielone są przecinkami.
   * Przy wczytywaniu nazwy kategorii i konceptów rzutowane są do małych liter.
   *
   * @param reader Strumień z którego zostanie wczytanie mapowanie.
   * @param sumo   Obiekt reprezentujący ontologię sumo do sprawdzenia, czy wczytane koncepty istnieją w ontologii.
   * @throws IOException
   */
  public NamToSumo(final Reader reader, final Sumo sumo) throws IOException {
  }

  public void parse(final Reader reader) throws IOException {
    final BufferedReader br = new BufferedReader(reader);
    String line = null;
    while ((line = br.readLine()) != null) {
      line = line.trim();
      final String[] cols = line.split("\t");
      if (cols.length == 2) {
        final String name = cols[0];
        final String[] concepts = cols[1].split("[ ]*,[ ]*");
        Set<String> conceptsSet = mapping.get(name);
        if (conceptsSet == null) {
          conceptsSet = new HashSet<>();
          mapping.put(name, conceptsSet);
        }
        for (String concept : concepts) {
          concept = concept.toLowerCase();
          conceptsSet.add(concept);
          if (!sumo.containsClass(concept)) {
            Logger.getLogger(getClass()).warn(String.format("Concept '%s' not found in SUMO", concept));
          }
        }
      }
    }
  }

  public Set<String> getConcept(final String word) {
    return mapping.get(word);
  }

}