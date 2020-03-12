package g419.toolbox.sumo;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

public class Sumo {

  private final Pattern subclassRelPattern = Pattern.compile("^\\p{Z}*\\(subclass (\\p{L}+) (\\p{L}+)\\)\\p{Z}*$");
  private final Graph graph = new Graph();
  private boolean caseSensitive = false;

  public Sumo(final String mapping) throws IOException, DataFormatException {
    final File mappingFile = new File(mapping);
    if (mappingFile.exists()) {
      parseMapping(new FileInputStream(mappingFile));
    } else {
      throw new DataFormatException("Mapping file does not exist: " + mapping);
    }
  }

  public Sumo() throws IOException {
    loadDeafultKifs();
  }

  public Sumo(final boolean caseSensitive) throws IOException {
    this.caseSensitive = caseSensitive;
    loadDeafultKifs();
  }

  public Set<String> getConcepts() {
    return graph.getNodes();
  }

  public boolean containsClass(String label) {
    if (caseSensitive == false) {
      label = label.toLowerCase();
    }
    return graph.containsClass(label);
  }

  private void loadDeafultKifs() throws IOException {
    parseMapping(getClass().getResourceAsStream("/sumo/Merge.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/Geography.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/Mid-level-ontology.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/Transportation.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/Economy.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/Cars.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/naics.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/Food.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/Media.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/TransportDetail.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/Dining.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/QoSontology.kif"));
    parseMapping(getClass().getResourceAsStream("/sumo/MilitaryDevices.kif"));
  }

  private void parseMapping(final InputStream mapping) throws IOException {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(mapping));
    String line = reader.readLine();
    while (line != null) {
      final Matcher m = subclassRelPattern.matcher(line);
      if (m.find()) {
        final String c1 = caseSensitive ? m.group(1) : m.group(1).toLowerCase();
        final String c2 = caseSensitive ? m.group(2) : m.group(2).toLowerCase();
        graph.addConnection(c1, c2);
      }
      line = reader.readLine();
    }
  }

  public Set<String> getSuperclasses(final String currentClass) {
    return graph.getSuperclasses(currentClass);
  }

  public Set<String> getSubclasses(final String upperClass) {
    final Set<String> subclasses = new HashSet<>();
    graph.getSubclasses(upperClass, subclasses);
    return subclasses;
  }

  public Set<String> getSubclasses(final Set<String> classes) {
    final Set<String> subclasses = new HashSet<>();
    for (final String cl : classes) {
      graph.getSubclasses(cl, subclasses);
    }
    return subclasses;
  }

  public boolean isSubclassOf(String subClass, String upperClass) {
    if (caseSensitive == false) {
      subClass = subClass.toLowerCase();
      upperClass = upperClass.toLowerCase();
    }
    return graph.isSubclassOf(subClass, upperClass);
  }

  public boolean isSubclassOf(final Set<String> subclasses, String upperClass) {
    if (caseSensitive == false) {
      upperClass = upperClass.toLowerCase();
    }
    for (String subClass : subclasses) {
      if (caseSensitive == false) {
        subClass = subClass.toLowerCase();
      }
      if (graph.isSubclassOf(subClass, upperClass)) {
        return true;
      }
    }
    return false;
  }

  public boolean isClassOrSubclassOf(String subClass, String upperClass) {
    if (caseSensitive == false) {
      subClass = subClass.toLowerCase();
      upperClass = upperClass.toLowerCase();
    }
    if (subClass.equals(upperClass)) {
      return true;
    } else {
      return isSubclassOf(subClass, upperClass);
    }
  }

  public boolean isClassOrSubclassOf(Set<String> subclasses, String upperClass) {
    if (caseSensitive == false) {
      upperClass = upperClass.toLowerCase();
      final Set<String> subclassesLower = new HashSet<>();
      for (final String cl : subclasses) {
        subclassesLower.add(cl.toLowerCase());
      }
      subclasses = subclassesLower;
    }
    if (subclasses.contains(upperClass)) {
      return true;
    } else {
      return isSubclassOf(subclasses, upperClass);
    }
  }

}
