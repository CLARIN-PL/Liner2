package g419.spatial.io;

import g419.corpus.HasLogger;
import g419.spatial.structure.SpatialRelationSchema;
import g419.spatial.structure.SpatialRelationSchemaMatcher;
import g419.toolbox.sumo.Sumo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Klasa do parsowania schematów ontologicznych wyrażeń przestrzennych zapisanych w formacie CSV.
 * Przykładowy plik: g419-spatial/src/main/resources/g419/spatial/resources/spatial_schemes.csv
 *
 * @author czuk
 */
public class CsvSpatialSchemeParser implements HasLogger {

  private BufferedReader reader = null;
  private Sumo sumo = null;
  private final Logger logger = Logger.getLogger(CsvSpatialSchemeParser.class);
  private boolean general = true;

  public CsvSpatialSchemeParser(final Reader reader, final Sumo sumo) {
    this.reader = new BufferedReader(reader);
    this.sumo = sumo;
  }

  /**
   * @param reader  Reader z którego będą wczytywane schematy
   * @param sumo    Obiekt SUMO do sprawdzenia poprawności konceptów
   * @param general Czy użyć ogólnych konceptów (true), czy szczegółowych (false)
   */
  public CsvSpatialSchemeParser(final Reader reader, final Sumo sumo, final boolean general) {
    this.reader = new BufferedReader(reader);
    this.sumo = sumo;
    this.general = general;
  }

  /**
   * Parsuje schematy
   *
   * @return
   * @throws IOException
   */
  public SpatialRelationSchemaMatcher parse() throws IOException {
    // Kolumny zawierające uogólnione koncepty dla trajektorów i landmarków
    int columnTrajector = 12;
    int columnLandmark = 14;

    // Kolumny zawierające uszczegółowione koncepty dla trajektorów i landmarków
    if (!general) {
      columnTrajector = 13;
      columnLandmark = 15;
    }
    final List<SpatialRelationSchema> patterns = new LinkedList<>();

    final CSVParser csv = new CSVParser(reader, CSVFormat.DEFAULT);

    final Iterator<CSVRecord> it = csv.iterator();

    // Pomiń nagłówki
    it.next();
    it.next();

    while (it.hasNext()) {
      final CSVRecord record = it.next();
      if (record.size() < 15 || record.get(0).trim().length() == 0) {
        continue;
      }

      final String preposition = record.get(0).trim().toLowerCase();
      final String id = preposition + "#" + record.get(1);
      final String cas = record.get(2).trim();
      final boolean use = record.get(3).toLowerCase().trim().equals("t");

      if (!use) {
        Logger.getLogger(getClass()).info(String.format("Schemat %s został pominięty (use=N)", id));
        continue;
      }

      if (cas.length() == 0) {
        getLogger().info("Przypadek nie został określony dla {}", id);
        continue;
      }

      final String[] trajectorIds = record.get(columnTrajector).trim().split("( )*,( )*");
      final String[] landmarkIds = record.get(columnLandmark).trim().split("( )*,( )*");

      if (record.get(columnTrajector).trim().length() == 0 || trajectorIds.length == 0) {
        getLogger().warn("Pusty zbiór trajector (schemat {})", id);
        continue;
      }

      if (record.get(columnLandmark).trim().length() == 0 || landmarkIds.length == 0) {
        getLogger().warn("Pusty zbiór landmark (schemat {})", id);
        continue;
      }

      final Set<String> sis = new HashSet<>();
      for (final String si : preposition.split(",")) {
        sis.add(si.trim());
      }

      final Set<String> trajectorConcepts = parseConcepts(id, trajectorIds);
      final Set<String> landmarkConcepts = parseConcepts(id, landmarkIds);

      if (trajectorConcepts.size() > 0 && landmarkConcepts.size() > 0) {
        patterns.add(new SpatialRelationSchema(id, cas, sis, trajectorConcepts, landmarkConcepts));
      } else if (trajectorConcepts.size() == 0) {
        getLogger().warn("Pusty zbiór trajector (schemat {})", id);
      } else if (landmarkConcepts.size() == 0) {
        getLogger().warn("Pusty zbiór landmark (schemat {})", id);
      }

    }
    csv.close();

    logger.info(String.format("Liczba wczytanych wzorców: %d", patterns.size()));
    return new SpatialRelationSchemaMatcher(patterns, sumo);
  }

  /**
   * @param id
   * @param conceptsIds
   * @return
   */
  private Set<String> parseConcepts(final String id, final String[] conceptsIds) {
    final Set<String> concepts = new HashSet<>();
    for (String part : conceptsIds) {
      if (part.length() < 2) {
        getLogger().warn("Niepoprawna nazwa konceptu; koncepty: {} (schemat {})", String.join(", ", conceptsIds), id);
      } else if (!part.startsWith("#")) {
        getLogger().warn("Nazwa konceptu nie zaczyna się od #: {} (schemat {})", part, id);
      } else {
        part = part.trim().substring(1);
        if (sumo.containsClass(part)) {
          concepts.add(part);
        } else {
          getLogger().warn("Koncept '{}' nie został znaleziony w SUMO (schemat {})", part, id);
        }
      }
    }
    return concepts;
  }

}
