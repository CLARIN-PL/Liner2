package g419.liner2.cli.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Relation;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.FrequencyCounter;
import lombok.Data;
import org.apache.commons.cli.CommandLine;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionStats extends Action {

  private String input_file = null;
  private String input_format = null;

  public ActionStats() {
    super("stats");
    setDescription("prints corpus statistics");
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
  }

  @Override
  public void run() throws Exception {
    final CorpusStatsCollector collector = new CorpusStatsCollector();
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(input_file, input_format)) {
      reader.forEachRemaining(collector::addDocument);
    }
    final CorpusStats stats = collector.getStats();

    for (final Map.Entry<String, FrequencyCounter<String>> entry :
        stats.getAnnotationTypeLemmaFreq().entrySet()) {
      System.out.println(String.format("Annotation %s lemma frequency", entry.getKey()));
      entry.getValue().getSorted().stream()
          .map(e -> String.format("- %5d %s", e.getValue(), e.getKey()))
          .forEach(System.out::println);
    }

    final String line = "%-30s: %10d";
    final String lineLong = "%-60s: %10d";
    System.out.println(String.format(line, "Documents", stats.getDocumentCount()));
    System.out.println(String.format(line, "Sentences", stats.getSentenceCount()));
    System.out.println(String.format(line, "Tokens", stats.getTokenCount()));
    System.out.println(String.format(line, "Annotations", stats.getAnnotationCount()));
    System.out.println(String.format(line, "Relations", stats.getRelationCount()));

    System.out.println();
    System.out.println("Annotations by type:");
    stats.getAnnotationCountByType().entrySet().stream()
        .sorted(Comparator.comparing(Map.Entry::getKey))
        .map(tc -> String.format(line + " %10d", " - " + tc.getKey(), tc.getValue().get(),
            stats.getAnnotationDistinctValueCountByType().getOrDefault(tc.getKey(), Collections.emptySet()).size()))
        .forEach(System.out::println);

    System.out.println();
    System.out.println("Relations by type:");
    stats.getRelationCountByType().entrySet().stream()
        .sorted(Comparator.comparing(Map.Entry::getKey))
        .map(tc -> String.format(line, " - " + tc.getKey(), tc.getValue().get()))
        .forEach(System.out::println);

    System.out.println();
    System.out.println("Relations by type, source and target:");
    stats.getRelationCountByTypeAndAnotations().entrySet().stream()
        .sorted(Comparator.comparing(Map.Entry::getKey))
        .map(tc -> String.format(lineLong, " - " + tc.getKey(), tc.getValue().get()))
        .forEach(System.out::println);
  }

  @Data
  class CorpusStatsCollector {
    private final CorpusStats stats = new CorpusStats();

    public void addDocument(final Document document) {
      stats.getDocumentTokens().add(countTokens(document));
      stats.getDocumentSentences().add(document.getSentences().size());
      document.getAnnotations().forEach(this::addAnnotation);
      document.getRelations().forEach(this::addRelation);
    }

    public int countTokens(final Document document) {
      return document.getSentences().stream().mapToInt(Sentence::getTokenNumber).sum();
    }

    public void addAnnotation(final Annotation an) {
      stats.getAnnotationCountByType()
          .computeIfAbsent(an.getType(), k -> new AtomicInteger()).incrementAndGet();
      stats.getAnnotationDistinctValueCountByType()
          .computeIfAbsent(an.getType(), k -> Sets.newHashSet()).add(an.getBaseText().toLowerCase());
      stats.getAnnotationTypeLemmaFreq()
          .computeIfAbsent(an.getType(), k -> new FrequencyCounter<>())
          .add(an.getBaseText());
    }

    public void addRelation(final Relation rel) {
      stats.getRelationCountByType()
          .computeIfAbsent(rel.getType(), k -> new AtomicInteger()).incrementAndGet();

      final String relKey = String.format("%s-%s-%s", rel.getType(),
          rel.getAnnotationFrom().getType(),
          rel.getAnnotationTo().getType());
      final String relStr = String.format("%s-%s-%s", rel.getType(),
          rel.getAnnotationFrom().getText(),
          rel.getAnnotationTo().getText());
      System.out.println(
          String.format("[Relation relation-source-target] %s %s %s",
              rel.getDocument().getName(), relKey, relStr));
      stats.getRelationCountByTypeAndAnotations()
          .computeIfAbsent(relKey, k -> new AtomicInteger()).incrementAndGet();
    }
  }

  @Data
  class CorpusStats {
    List<Integer> documentSentences = Lists.newArrayList();
    List<Integer> documentTokens = Lists.newArrayList();
    Map<String, AtomicInteger> annotationCountByType = Maps.newHashMap();
    Map<String, Set<String>> annotationDistinctValueCountByType = Maps.newHashMap();
    Map<String, AtomicInteger> relationCountByType = Maps.newHashMap();
    Map<String, AtomicInteger> relationCountByTypeAndAnotations = Maps.newHashMap();
    Map<String, FrequencyCounter<String>> annotationTypeLemmaFreq = Maps.newHashMap();

    public int getDocumentCount() {
      return documentTokens.size();
    }

    public int getSentenceCount() {
      return documentSentences.stream().mapToInt(Integer::intValue).sum();
    }

    public int getTokenCount() {
      return documentTokens.stream().mapToInt(Integer::intValue).sum();
    }

    public int getAnnotationCount() {
      return annotationCountByType.values().stream().mapToInt(AtomicInteger::intValue).sum();
    }

    public int getRelationCount() {
      return getRelationCountByType().values().stream().mapToInt(AtomicInteger::intValue).sum();
    }
  }
}
