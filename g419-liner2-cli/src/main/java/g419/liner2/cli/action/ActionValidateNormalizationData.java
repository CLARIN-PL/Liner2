package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.*;
import java.util.regex.Pattern;

public class ActionValidateNormalizationData extends Action {
  private String input_file = null;
  private String input_format = null;
  protected List<Pattern> types;
  protected String metaKey = null;

  public ActionValidateNormalizationData() {
    super("normalizer-validate");
    setDescription("Read all annotation and their metadata and look for errors.");

    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());

    options.addOption(Option.builder("t").argName("types").hasArg().longOpt("types").
        desc("Annotation types that will be analyzed (separated with ;)").build());
    options.addOption(Option.builder("m").argName("metaKey").hasArg().longOpt("metaKey").
        desc("Metadata key that will be analysed (just pass 'lemma' here for now)").build());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    if (!line.hasOption("t")) {
      //todo: specialized exception
      final Exception up = new RuntimeException("You must specify annotation types!");
      throw up;
    }
    if (!line.hasOption("m")) {
      //todo: specialized exception
      final Exception up = new RuntimeException("You must specify metadata key!");
      throw up;
    }
    types = new ArrayList<>();
    for (final String type : line.getOptionValue("t").split("[;]")) {
      types.add(Pattern.compile(type));
    }
    metaKey = line.getOptionValue("m");
  }

  protected static class Tuple {
    final public String base;
    final public String type;
    final public String value;
    final public String documentName;
    final public String fullSentence;

    public Tuple(final String base, final String type, final String value, final String documentName, final String fullSentence) {
      this.base = base;
      this.type = type;
      this.value = value;
      this.documentName = documentName;
      this.fullSentence = fullSentence;
    }

    public String getKey() {
      return base + "|" + type;
    }
  }

  @Override
  public void run() throws Exception {
    final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(input_file, input_format);
    Document document;
    final Map<String, List<Tuple>> tuples = new HashMap<>();
    while ((document = reader.nextDocument()) != null) {
      for (final Sentence sentence : document.getSentences()) {
        for (final Annotation annotation : sentence.getAnnotations(types)) {
          final Tuple tuple = new Tuple(
              annotation.getBaseText(),
              annotation.getType(),
              annotation.getMetadata().get(metaKey),
              document.getName(),
              sentence.toString()
          );
          if (!tuples.containsKey(tuple.getKey())) {
            tuples.put(tuple.getKey(), new ArrayList<>());
          }
          tuples.get(tuple.getKey()).add(tuple);
        }
      }
    }
    println("Size:", tuples.size());
    while (!tuples.isEmpty()) {
      final String key = tuples.keySet().iterator().next();
      final List<Tuple> matching = tuples.get(key);
      tuples.remove(key);
      compareWithEachOther(key, matching);
    }
  }

  protected static String quote(final String txt) {
    return "\"" + txt + "\"";
  }

  protected void compareWithEachOther(final String key, final List<Tuple> tuples) {
    println("Comparing", tuples.size(), "tuples for key", key);
    final Set<String> vals = new HashSet<>();
    for (final Tuple t : tuples) {
      vals.add(t.value);
    }
    if (vals.size() != 1) {
      final Tuple example = tuples.iterator().next();
      println("Possible error!");
      println("Type:", example.type);
      println("Base:", example.base);
      println("Values:");
      for (final Tuple t : tuples) {
        println("\t", t.value);
        println("\t\tdocument:", t.documentName);
        println("\t\tsentence context:", t.fullSentence);
      }
    }
  }

  protected void println(final Object... args) {
    for (final Object a : args) {
      System.out.print(a.toString() + " ");
    }
    System.out.println();
  }
}
