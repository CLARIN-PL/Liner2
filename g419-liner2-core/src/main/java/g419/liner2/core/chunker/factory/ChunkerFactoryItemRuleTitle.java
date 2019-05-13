package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.RuleTitleChunker;
import org.ini4j.Ini;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Chunker rozpoznaje tytuły na podstawie przesłanek kontekstowych.
 * <p>
 * Przykład konfiguracji w pliku ini:
 *
 * <code>
 * [chunker_rule_title]
 * type	   = rule-title
 * annotation = nam
 * prefixes   = {INI_PATH}/data/keywords2/quoted_title_prefix.txt
 * </code>
 *
 * @author Michał Marcińczuk
 */
public class ChunkerFactoryItemRuleTitle extends ChunkerFactoryItem {

  public static String PARAM_ANNOTATION = "annotation";
  public static String PARAM_PREFIXES = "prefixes";

  public ChunkerFactoryItemRuleTitle() {
    super("rule-title");
  }

  @Override
  public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
    String annotationType = "";
    Set<String> prefixes = new HashSet<String>();

    // TODO Definiowanie i sprawdzanie obowiązkowych parametrów przenieść na poziom klasy ChunkerFactoryItem
    if (!description.containsKey(PARAM_ANNOTATION)) {
      throw new Exception("A parameter in chunker description is missing: " + PARAM_ANNOTATION);
    }

    if (!description.containsKey(PARAM_PREFIXES)) {
      throw new Exception("A parameter in chunker description is missing: " + PARAM_PREFIXES);
    }

    annotationType = description.get(PARAM_ANNOTATION);

    // TODO sprawdzenie, czy plik istnieje
    prefixes.addAll(Files.readAllLines(Paths.get(description.get(PARAM_PREFIXES))));

    return new RuleTitleChunker(annotationType, prefixes);
  }

}
