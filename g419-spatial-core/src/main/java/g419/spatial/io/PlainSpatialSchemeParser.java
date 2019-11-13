package g419.spatial.io;

import g419.spatial.structure.SpatialRelationSchema;
import g419.spatial.structure.SpatialRelationSchemaMatcher;
import g419.toolbox.sumo.Sumo;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PlainSpatialSchemeParser {

  private BufferedReader reader = null;
  private Sumo sumo = null;
  private int lineNo = 0;
  private String currentLine = null;
  private final Logger logger = Logger.getLogger(PlainSpatialSchemeParser.class);

  public PlainSpatialSchemeParser(final Reader reader, final Sumo sumo) {
    this.reader = new BufferedReader(reader);
    this.sumo = sumo;
  }

  private String nextLine() throws IOException {
    lineNo++;
    currentLine = reader.readLine();
    return currentLine;
  }

  public SpatialRelationSchemaMatcher parse() throws IOException {
    nextLine();
    final List<SpatialRelationSchema> patterns = new LinkedList<>();

    while (currentLine != null) {
      if (currentLine.startsWith("si:")) {
        final Set<String> sis = new HashSet<>();
        for (final String si : currentLine.substring(3).trim().split(",")) {
          sis.add(si.trim());
        }
        nextLine();
        if (currentLine == null || !currentLine.startsWith("tr:")) {
          Logger.getLogger(PlainSpatialSchemeParser.class).warn(
              String.format("Linia nr %d: oczekiwano 'tr:', ale napotkano '%s'", lineNo, currentLine));
          continue;
        }
        final Set<String> trajectorConcepts = parseConcepts(currentLine.substring(3).trim());

        nextLine();
        if (currentLine == null || !currentLine.startsWith("lm:")) {
          logger.warn(
              String.format("Linia nr %d: oczekiwano 'lm:', ale napotkano '%s'", lineNo, currentLine));
          continue;
        }
        final Set<String> landmarkConcepts = parseConcepts(currentLine.substring(3).trim());

        if (trajectorConcepts.size() > 0 && landmarkConcepts.size() > 0) {
          patterns.add(new SpatialRelationSchema(String.join("-", sis), "loc", sis, trajectorConcepts, landmarkConcepts));
        } else if (trajectorConcepts.size() == 0) {
          logWarning("Pusty zbiór trajectorConcept");
        } else if (landmarkConcepts.size() == 0) {
          logWarning("Pusty zbiór landmarkConcept");
        }

        nextLine();
      } else if (currentLine.length() > 0) {
        logger.warn(
            String.format("Linia nr %d: oczekiwano 'si:', ale napotkano '%s'", lineNo, currentLine));
      }
      nextLine();
    }

    logger.info(String.format("Liczba wczytanych wzorców: %d", patterns.size()));
    return new SpatialRelationSchemaMatcher(patterns, sumo);
  }

  private Set<String> parseConcepts(final String line) {
    final Set<String> concepts = new HashSet<>();
    for (String part : line.split(",( )*")) {
      part = part.trim().substring(1);
      if (sumo.containsClass(part)) {
        concepts.add(part);
      } else {
        logWarning(String.format("Concept '%s' not found in SUMO", part));
      }
    }
    return concepts;
  }

  private void logWarning(final String message) {
    logger.warn(String.format("Linia nr %d: %s", lineNo, message));
  }
}
