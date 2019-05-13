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
  private Logger logger = Logger.getLogger(PlainSpatialSchemeParser.class);

  public PlainSpatialSchemeParser(Reader reader, Sumo sumo) {
    this.reader = new BufferedReader(reader);
    this.sumo = sumo;
  }

  private String nextLine() throws IOException {
    this.lineNo++;
    this.currentLine = this.reader.readLine();
    return this.currentLine;
  }

  public SpatialRelationSchemaMatcher parse() throws IOException {
    this.nextLine();
    List<SpatialRelationSchema> patterns = new LinkedList<SpatialRelationSchema>();

    while (this.currentLine != null) {
      if (this.currentLine.startsWith("si:")) {
        Set<String> sis = new HashSet<String>();
        for (String si : this.currentLine.substring(3).trim().split(",")) {
          sis.add(si.trim());
        }
        this.nextLine();
        if (this.currentLine == null || !this.currentLine.startsWith("tr:")) {
          Logger.getLogger(PlainSpatialSchemeParser.class).warn(
              String.format("Linia nr %d: oczekiwano 'tr:', ale napotkano '%s'", this.lineNo, this.currentLine));
          continue;
        }
        Set<String> trajectorConcepts = this.parseConcepts(this.currentLine.substring(3).trim());

        this.nextLine();
        if (this.currentLine == null || !this.currentLine.startsWith("lm:")) {
          logger.warn(
              String.format("Linia nr %d: oczekiwano 'lm:', ale napotkano '%s'", this.lineNo, this.currentLine));
          continue;
        }
        Set<String> landmarkConcepts = this.parseConcepts(this.currentLine.substring(3).trim());

        if (trajectorConcepts.size() > 0 && landmarkConcepts.size() > 0) {
          patterns.add(new SpatialRelationSchema(String.join("-", sis), "loc", sis, trajectorConcepts, landmarkConcepts));
        } else if (trajectorConcepts.size() == 0) {
          this.logWarning("Pusty zbiór trajectorConcept");
        } else if (landmarkConcepts.size() == 0) {
          this.logWarning("Pusty zbiór landmarkConcept");
        }

        this.nextLine();
      } else if (this.currentLine.length() > 0) {
        logger.warn(
            String.format("Linia nr %d: oczekiwano 'si:', ale napotkano '%s'", this.lineNo, this.currentLine));
      }
      this.nextLine();
    }

    logger.info(String.format("Liczba wczytanych wzorców: %d", patterns.size()));
    return new SpatialRelationSchemaMatcher(patterns, this.sumo);
  }

  private Set<String> parseConcepts(String line) {
    Set<String> concepts = new HashSet<String>();
    for (String part : line.split(",( )*")) {
      part = part.trim().substring(1);
      if (this.sumo.containsClass(part)) {
        concepts.add(part);
      } else {
        this.logWarning(String.format("Concept '%s' not found in SUMO", part));
      }
    }
    return concepts;
  }

  private void logWarning(String message) {
    this.logger.warn(String.format("Linia nr %d: %s", this.lineNo, message));
  }
}
