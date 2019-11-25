package g419.liner2.cli.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ActionSplitCv extends Action {

  public static final String OPTION_FOLDS = "n";
  public static final String OPTION_FOLDS_LONG = "folds-number";
  public static final String OPTION_FOLDS_DESC = "number of folds (n>1)";
  public static final String OPTION_FOLDS_ARG = "n";

  private String inputFile = null;
  private String inputFormat = null;
  private String foldsNumber = null;

  public ActionSplitCv() {
    super("split-cv");
    setDescription("split set of documents into two parts according to given ratio");
    options.addOption(CommonOptions.getInputFileFormatOption());
    options.addOption(CommonOptions.getInputFileNameOption());
    options.addOption(getRatioOption());
  }


  public static Option getRatioOption() {
    return Option.builder(OPTION_FOLDS).longOpt(OPTION_FOLDS_LONG)
        .hasArg().argName(OPTION_FOLDS_ARG).desc(OPTION_FOLDS_DESC).build();
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
    inputFile = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
    inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
    foldsNumber = line.getOptionValue(OPTION_FOLDS);
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

    final List<Set<String>> parts = split(counts, Integer.parseInt(foldsNumber));

    //writeIndices(parts, FilenameUtils.removeExtension(inputFile) + "-ratio" + ratio + "-");
  }

//    private void writeIndices(final Pair<Set<String>, Set<String>> parts, final String outputPrefix) throws IOException {
//        FileUtils.writeLines(
//                new File(outputPrefix + "seta.list"), parts.getLeft().stream().sorted().collect(Collectors.toList()));
//        FileUtils.writeLines(
//                new File(outputPrefix + "setb.list"), parts.getRight().stream().sorted().collect(Collectors.toList()));
//    }

//    private void writeAll(final Pair<Set<String>, Set<String>> parts) throws Exception {
//        try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
//             final AbstractDocumentWriter writerA = WriterFactory.get().getStreamWriter("seta.iob", "iob");
//             final AbstractDocumentWriter writerB = WriterFactory.get().getStreamWriter("setb.iob", "iob")
//        ) {
//            reader.forEach(document -> {
//                if (parts.getLeft().contains(document.getName())) {
//                    writerA.writeDocument(document);
//                } else if (parts.getRight().contains(document.getName())) {
//                    writerB.writeDocument(document);
//                } else {
//                    getLogger().error("Document {} was not assigned to any set");
//                }
//            });
//        }
//    }

  private List<Set<String>> split(final List<Pair<String, Integer>> counts, final int foldsNumber) {
    final List<Pair<Set<String>, Integer>> folds = IntStream.range(0, foldsNumber).mapToObj(n -> new ImmutablePair<Set<String>, Integer>(Sets.newHashSet(), 0)).collect(Collectors.toList());
    counts.stream().sorted((f1, f2) -> Integer.compare(f2.getRight(), f1.getRight())).forEach(pair -> {
      final Pair<Set<String>, Integer> next = folds.stream().sorted(Comparator.comparing(Pair::getValue)).findFirst().get();
      next.setValue(next.getValue() + pair.getValue());
      next.getKey().add(pair.getKey());
    });

    folds.forEach(pair -> {
      System.out.println(String.format(" Fold: tokens %10d, documents %3d", pair.getValue(), pair.getKey().size()));
    });

    return folds.stream().map(Pair::getKey).collect(Collectors.toList());
  }

}
