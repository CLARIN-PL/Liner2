package g419.tools.action;

import g419.lib.cli.Action;
import g419.liner2.core.tools.ProcessingTimer;
import g419.toolbox.wordnet.WordnetPl30;
import g419.toolbox.wordnet.struct.Synset;
import g419.toolbox.wordnet.struct.WordnetPl;
import io.vavr.control.Option;
import java.security.InvalidParameterException;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.tuple.Pair;

public class WordnetWuPalmerCli extends Action {

  WordnetPl wordnet;
  ProcessingTimer timer = new ProcessingTimer();
  boolean isRunning;

  public WordnetWuPalmerCli() {
    super("wordnet-wupalmer-cli");
    setDescription("calculate Wu-Palmer similarity for given two lemmas");
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
  }

  @Override
  public void run() throws Exception {
    getLogger().info("Loading wordnet ...");
    wordnet = WordnetPl30.load();
    getLogger().info("done.");

    isRunning = true;
    do {
      try {
        readWords()
            .peek(this::handleWords)
            .onEmpty(() -> isRunning = false);
      } catch (final InvalidParameterException ex) {
        System.out.println("[ERROR] " + ex.getMessage());
      }
    } while (isRunning);
  }

  private Option<Pair<String, String>> readWords() throws InvalidParameterException {
    System.out.println();
    System.out.print("Enter two words (w1;w2), leave empty to exit: ");
    final String line = System.console().readLine().trim();
    return parseWords(line);
  }

  private Option<Pair<String, String>> parseWords(final String line)
      throws InvalidParameterException {
    if (line.length() == 0) {
      return Option.none();
    }
    final String[] words = line.split(";");
    if (words.length != 2) {
      throw new InvalidParameterException(
          String.format("Invalid number of words. Expected 2 but got %d", words.length));
    }
    return Option.of(Pair.of(words[0].trim(), words[1].trim()));
  }

  private void handleWords(final Pair<String, String> words) {
    System.out.println("Words: " + words);
    final Set<Synset> synsets1 = wordnet.getSynsetsWithLemma(words.getLeft());
    final Set<Synset> synsets2 = wordnet.getSynsetsWithLemma(words.getRight());
    for (final Synset s1 : synsets1) {
      System.out.println();
      System.out.println(formatSynset(s1));
      synsets2.stream()
          .map(s -> Pair.of(wordnet.simWuPalmer(s1, s), s))
          .sorted((a, b) -> Double.compare(b.getLeft(), a.getLeft()))
          .map(p -> String.format(" %5.3f: %s", p.getLeft(), formatSynset(p.getRight())))
          .forEach(System.out::println);
    }
  }

  private String formatSynset(final Synset synset) {
    return String.format("[%d] %s (%s) -- %s",
        synset.getId(), synset.getLexicalUnitsStr(), synset.getPos(), synset.getDefinition());
  }

}
