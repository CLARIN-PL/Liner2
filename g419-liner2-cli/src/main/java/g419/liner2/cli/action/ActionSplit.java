package g419.liner2.cli.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionSplit extends Action {

  public static final String OPTION_RATIO = "r";
  public static final String OPTION_RATIO_LONG = "ratio";
  public static final String OPTION_RATIO_DESC = "subset ratio in the form of n:1 (default n=1)";
  public static final String OPTION_RATIO_ARG = "n";
  public static final String OPTION_RATIO_DEFAULT = "1";

  private String inputFile = null;
  private String inputFormat = null;
  private String ratio = null;

  public ActionSplit() {
    super("split");
    setDescription("split set of documents into two parts according to given ratio");
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(getRatioOption());
  }


  public static Option getRatioOption() {
    return Option.builder(OPTION_RATIO).longOpt(OPTION_RATIO_LONG)
        .hasArg().argName(OPTION_RATIO_ARG).desc(OPTION_RATIO_DESC).build();
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFile = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    ratio = line.getOptionValue(OPTION_RATIO, OPTION_RATIO_DEFAULT);
  }

  /**
   * Module entry function.
   * <p>
   * Loads annotation recognizers.
   */
  @Override
  public void run() throws Exception {
    final List<Pair<String, Integer>> counts = Lists.newArrayList();
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat)) {
      reader.forEach(document -> {
        final int count = document.getSentences().stream().mapToInt(Sentence::getTokenNumber).sum();
        counts.add(new ImmutablePair<>(document.getName(), count));
        System.out.println(String.format("%4d — %s", count, document.getName()));
      });
    }

    final Pair<Set<String>, Set<String>> parts = split(counts, Double.parseDouble(ratio));

    writeIndices(parts, FilenameUtils.removeExtension(inputFile) + "-ratio" + ratio + "-");
  }

  private void writeIndices(final Pair<Set<String>, Set<String>> parts, final String outputPrefix) throws IOException {
    FileUtils.writeLines(
        new File(outputPrefix + "seta.list"), parts.getLeft().stream().sorted().collect(Collectors.toList()));
    FileUtils.writeLines(
        new File(outputPrefix + "setb.list"), parts.getRight().stream().sorted().collect(Collectors.toList()));
  }

  private void writeAll(final Pair<Set<String>, Set<String>> parts) throws Exception {
    try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
         final AbstractDocumentWriter writerA = WriterFactory.get().getStreamWriter("seta.iob", "iob");
         final AbstractDocumentWriter writerB = WriterFactory.get().getStreamWriter("setb.iob", "iob")
    ) {
      reader.forEach(document -> {
        if (parts.getLeft().contains(document.getName())) {
          writerA.writeDocument(document);
        } else if (parts.getRight().contains(document.getName())) {
          writerB.writeDocument(document);
        } else {
          getLogger().error("Document {} was not assigned to any set");
        }
      });
    }
  }

  private Pair<Set<String>, Set<String>> split(final List<Pair<String, Integer>> counts, final double ratio) {
    final double totalCount = counts.stream().mapToInt(Pair::getRight).sum();
    final double sizeA = Math.round(totalCount / (ratio + 1));
    final String line = "%-12s: %.2f";

    Collections.shuffle(counts);

    final Set<String> setA = Sets.newHashSet();
    final Set<String> setB = Sets.newHashSet();
    double currectSizeA = 0;

    for (final Pair<String, Integer> p : counts) {
      if (p.getRight() + currectSizeA < sizeA) {
        setA.add(p.getLeft());
        currectSizeA += p.getRight();
      } else {
        setB.add(p.getLeft());
      }
    }

    System.out.println(String.format(line, "Set A", currectSizeA));
    System.out.println(String.format(line, "Set B", totalCount - currectSizeA));
    System.out.println(String.format(line, "Total", totalCount));
    System.out.println(String.format(line, "Final ratio", (totalCount - currectSizeA) / currectSizeA));

    return new ImmutablePair<>(setA, setB);
  }

}
