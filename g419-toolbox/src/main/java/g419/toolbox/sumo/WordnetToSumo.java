package g419.toolbox.sumo;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;

/**
 *
 */
public class WordnetToSumo {

  private final Pattern synsetUnitPatern = Pattern.compile("(\\{|\\p{Z})(([^\\p{Z}]+)-([0-9]+))");
  /* Mapowanie lematów słów w zbiór pojęć */
  private HashMap<String, Set<String>> wordMapping = null;

  /* Mapowanie lemat+sens, np. firma-1 w zbiór pojęć */
  private final HashMap<String, Set<String>> wordSenseMapping;

  /* Mapowanie id synsetu w zbiór pojęć */
  private HashMap<String, Set<String>> synsetIdMapping = null;

  /**
   * Wczytuje mapowanie z domyślnym modelem, tj. mapping-26.05.2015-Serdel.csv.gz
   *
   * @throws IOException
   * @throws DataFormatException
   */
  public WordnetToSumo() throws IOException, DataFormatException {
    wordMapping = new HashMap<>();
    wordSenseMapping = new HashMap<>();
    synsetIdMapping = new HashMap<>();

    //String location = "/mapping-26.05.2015-Serdel.csv.gz";
    final String location = "/sumo/mapping-28.01.2016-Serdel.csv.gz";
    final InputStream resource = getClass().getResourceAsStream(location);
    final GZIPInputStream gzip = new GZIPInputStream(resource);

    if (resource == null) {
      throw new MissingResourceException("Resource not found: " + location,
          getClass().getName(), location);
    }
    final Reader serdelReader = new InputStreamReader(gzip);
    parseMapping(serdelReader);
    serdelReader.close();
  }


  /**
   * Tworzy obiekt na podstawie pliku z mapowaniem Serdela.
   *
   * @param serdelMapping
   * @throws IOException
   * @throws DataFormatException
   */
  public WordnetToSumo(final String serdelMapping) throws IOException, DataFormatException {
    wordMapping = new HashMap<>();
    wordSenseMapping = new HashMap<>();
    final File mapping = new File(serdelMapping);
    if (mapping.exists()) {
      Reader serdelReader = null;
      serdelReader = new FileReader(mapping);
      parseMapping(serdelReader);
      if (serdelReader != null) {
        serdelReader.close();
      }
    } else {
      throw new DataFormatException("Serdel mapping file does not exist: " + serdelMapping);
    }
  }

  /**
   * Tworzy obiekt na podstawie strumienia z mapowaniem Serdela.
   *
   * @param serdelReader
   * @throws IOException
   * @throws DataFormatException
   */
  public WordnetToSumo(final Reader serdelReader) throws IOException, DataFormatException {
    wordMapping = new HashMap<>();
    wordSenseMapping = new HashMap<>();
    parseMapping(serdelReader);
  }

  public Set<String> getLemmaConcepts(final String word) {
    return wordMapping.get(word);
  }

  public Set<String> getConcept(final String word, final int sense) {
    return wordSenseMapping.get(word + "-" + sense);
  }

  public Set<String> getSynsetConcepts(final String synsetId) {
    return synsetIdMapping.get(synsetId);
  }

  private void parseMapping(final Reader mappingReader) throws IOException, DataFormatException {
    final BufferedReader reader = new BufferedReader(mappingReader);
    /* Pierwsze linia to nagłówek, więc pomijam */
    String line = reader.readLine();
    while ((line = reader.readLine()) != null) {
      final String[] attrs = line.split(";");
      if (attrs.length > 3) { //&& !attrs[attrs.length - 1].equals("R")){
        final String synsetId = attrs[0].trim();
        final String sumoClass = attrs[attrs.length - 2];
        final String synset = attrs[2];
        final HashMap<String, String> synsetUnits = parseSynset(synset);
        /* Dodaj mapowanie dla lematów i lematów+sene */
        for (final String wordAndSense : synsetUnits.keySet()) {
          addMapping(wordAndSense, sumoClass, wordSenseMapping);
          addMapping(synsetUnits.get(wordAndSense), sumoClass, wordMapping);
        }
        /* Dodaj mapowanie dla identyfikatora synsetu */
        Set<String> classes = synsetIdMapping.get(synsetId);
        if (classes == null) {
          classes = new HashSet<>();
          synsetIdMapping.put(synsetId, classes);
        }
        classes.add(sumoClass);
      }
    }
  }

  private void addMapping(final String key, final String sumoClass, final HashMap<String, Set<String>> mapping) {
    if (mapping.containsKey(key)) {
      mapping.get(key).add(sumoClass);
    } else {
      final HashSet<String> classes = new HashSet<>();
      classes.add(sumoClass);
      mapping.put(key, classes);
    }
  }

  private HashMap<String, String> parseSynset(final String synset) {
    final HashMap<String, String> units = new HashMap<>();
    final Matcher m = synsetUnitPatern.matcher(synset);
    while (m.find()) {
      units.put(m.group(2), m.group(3));
    }
    return units;
  }
}
