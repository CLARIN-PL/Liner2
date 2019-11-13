package g419.liner2.cli.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import io.vavr.collection.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionAnonimization extends Action {

  class PhraseType {
    private String phrase;
    private String type;

    public PhraseType(final String phrase, final String type) {
      this.phrase = phrase;
      this.type = type;
    }

    public String getPhrase() {
      return phrase;
    }

    public void setPhrase(final String phrase) {
      this.phrase = phrase;
    }

    public String getType() {
      return type;
    }

    public void setType(final String type) {
      this.type = type;
    }
  }

  private final String OPTION_DICTIONARY_FILE = "d";

  private String inputFile = null;
  private String inputFormat = null;
  private String outputFolder = null;
  private String outputFormat = null;
  private String dictFile = null;

  public ActionAnonimization() {
    super("anonim");
    setDescription("anonimize according to given dictionary");
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(CommonOptions.getOutputFileNameOption());
    options.addOption(CommonOptions.getOutputFileFormatOption());
    options.addOption(getDictionaryFile());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFile = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    outputFolder = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
    outputFormat = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FORMAT, "ccl");
    dictFile = line.getOptionValue(OPTION_DICTIONARY_FILE);
  }

  public Option getDictionaryFile() {
    return Option.builder(OPTION_DICTIONARY_FILE).longOpt("dict")
        .hasArg().argName("path").desc("dictionary of phrases per document").build();
  }


  @Override
  public void run() throws Exception {
    final Map<String, List<PhraseType>> phrases = loadPhrases(Paths.get(dictFile));
    getLogger().info("Number of documents with phrases to anonymize: {}", phrases.size());

    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
         final AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(outputFolder, inputFormat)) {
      reader.forEachRemaining(document -> {
        final String docId = document.getName();
        anonymizePhrases(document, phrases.computeIfAbsent(docId, d -> Lists.newArrayList()));
        anonymizePhrases(document, phrases.computeIfAbsent("", d -> Lists.newArrayList()));
        writer.writeDocument(document);
      });
    }
  }

  private void anonymizePhrases(final Document d, final List<PhraseType> documentPhrases) {
    getLogger().info("[{}] Number of phrases: {}", d.getName(), documentPhrases.size());
    final Set<String> replaced = Sets.newHashSet();
    documentPhrases.forEach(phrase -> {
      if (replaced.contains(phrase.getPhrase())) {
        getLogger().error("[{}] PHRASE '{}' already replaced", d.getName(), phrase.getPhrase());
      } else {
        final int replaceCount = anonymizePhrase(d, phrase);
        if (replaceCount > 0) {
          getLogger().info("[{}] PHRASE '{}' found {} times and replaced with '@{}'",
              d.getName(), phrase.getPhrase(), replaceCount, phrase.getType());
          replaced.add(phrase.getPhrase());
        } else {
          getLogger().error("[{}] PHRASE '{}' not found", d.getName(), phrase.getPhrase());
        }
      }
    });
  }

  private int anonymizePhrase(final Document d, final PhraseType phrase) {
    final List<List<Token>> matches = matchPhrase(d, phrase.getPhrase());
    matches.forEach(m -> anonymizePhrase(m, "@" + phrase.getType()));
    return matches.size();
  }

  private void anonymizePhrase(final List<Token> tokens, final String label) {
    tokens.forEach(t -> {
      t.setOrth(label);
      t.getTags().forEach(tag -> tag.setBase(label));
    });
  }

  private List<List<Token>> matchPhrase(final Document d, final String phrase) {
    return d.getSentences()
        .stream()
        .map(s -> matchPhrase(s, phrase))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }


  private List<List<Token>> matchPhrase(final Sentence s, final String phrase) {
    final List<Token> tokens = s.getTokens();
    final List<List<Token>> matches = Lists.newArrayList();
    int n = 0;
    while (n < tokens.size()) {
      final List<Token> match = matchPhraseAtPos(tokens, phrase, n);
      if (match != null) {
        matches.add(match);
        n += match.size();
      }
      n++;
    }
    return matches;
  }

  private List<Token> matchPhraseAtPos(final List<Token> tokens, final String phrase, final int pos) {
    String concat = tokens.get(pos).getOrth();
    int n = pos;
    while (!concat.equals(phrase) && n + 1 < tokens.size() && concat.length() < phrase.length()) {
      if (!tokens.get(n).getNoSpaceAfter()) {
        concat += " ";
      }
      concat += tokens.get(++n).getOrth();
    }
    if (concat.equals(phrase)) {
      return Stream.rangeClosed(pos, n)
          .map(tokens::get)
          .collect(Collectors.toList());
    } else {
      return null;
    }
  }

  public Map<String, List<PhraseType>> loadPhrases(final Path path) throws IOException {
    final CSVParser csv = new CSVParser(new FileReader(path.toFile()), CSVFormat.DEFAULT);
    final Map<String, List<PhraseType>> phrases = Maps.newHashMap();
    csv.forEach(record -> {
      final String docId = record.get(0);
      final PhraseType phraseType = new PhraseType(record.get(4), record.get(3));
      phrases.computeIfAbsent(docId, d -> Lists.newArrayList()).add(phraseType);
    });
    return phrases;
  }

}
