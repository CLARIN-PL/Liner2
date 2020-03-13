package g419.tools.action;

import com.google.common.collect.Lists;
import g419.lib.cli.Action;
import g419.liner2.core.tools.ProcessingTimer;
import g419.toolbox.wordnet.WordnetPl30;
import g419.toolbox.wordnet.struct.Synset;
import g419.toolbox.wordnet.struct.WordnetPl;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.commons.cli.CommandLine;

public class WordnetWuPalmerBenchmark extends Action {

  WordnetPl wordnet;
  ProcessingTimer timer = new ProcessingTimer();

  public WordnetWuPalmerBenchmark() {
    super("wordnet-wupalmer-benchmark");
    setDescription("benchmark Wu-Palmer similarity measure");
  }

  @Override
  public void parseOptions(final CommandLine line) throws Exception {
  }

  @Override
  public void run() throws Exception {
    final int documentNumber = 500;
    final List<Integer> synsetsAids =
        Lists.newArrayList(236411, 10063, 28394, 464809, 10738, 11749, 27125, 246981, 32311, 247562);
    final List<Integer> synsetsBids =
        Lists.newArrayList(28089, 14213, 235099, 7280, 85319, 10063, 10738, 464809, 582);

    timer.startTimer("Wordnet loading");
    wordnet = WordnetPl30.load();
    timer.stopTimer();

    timer.startTimer(
        String.format("Finding synsets (%d synsets)", synsetsAids.size() + synsetsBids.size()));
    final List<Synset> synsetsA = idToSynsets(synsetsAids);
    final List<Synset> synsetsB = idToSynsets(synsetsBids);
    timer.stopTimer();

    printSynsets(synsetsA);
    printSynsets(synsetsB);

    timer.startTimer(String.format("Wu-Palmer (%d^2 x 10^2 comparisons)", documentNumber));
    compareAll(synsetsA, synsetsB, true);
    runMultithreadBenchmark(documentNumber, synsetsA, synsetsB, 30);
    timer.stopTimer();

    timer.printStats();
  }

  private List<Synset> idToSynsets(final List<Integer> ids) {
    final List<Synset> synsets = Lists.newArrayList();
    for (final int id : ids) {
      final Synset synset = wordnet.getSynset(id);
      if (synset == null) {
        getLogger().error("Synset with id {} not found", id);
      } else {
        synsets.add(synset);
      }
    }
    return synsets;
  }

  private void printSynsets(final List<Synset> synsets) {
    synsets.stream()
        .map(s -> String.format("%10d: %s", s.getId(), s.getLexicalUnitsStr()))
        .forEach(System.out::println);
  }

  private void compareAll(final List<Synset> sa, final List<Synset> sb, final boolean print) {
    for (final Synset s1 : sa) {
      if (print) {
        System.out.println(String.format("\n[%d] %s", s1.getId(), s1.getLexicalUnitsStr()));
      }
      for (final Synset s2 : sb) {
        final double sim = wordnet.simWuPalmer(s1, s2);
        if (print) {
          System.out.println(
              String.format(" %.2f: [%d] %s", sim, s2.getId(), s2.getLexicalUnitsStr()));
        }
      }
    }
  }

  private void runMultithreadBenchmark(final int documentNumber,
                                       final List<Synset> as,
                                       final List<Synset> bs,
                                       final int threads) {
    final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
    final List<CompletableFuture<?>> futures = Lists.newArrayList();

    for (int i = 0; i < documentNumber; i++) {
      for (int j = 0; j < documentNumber; j++) {
        futures.add(CompletableFuture.runAsync(() -> compareAll(as, bs, false), pool));
      }
    }
    final CompletableFuture<?>[] futuresArray
        = futures.toArray(new CompletableFuture<?>[futures.size()]);
    CompletableFuture.allOf(futuresArray).join();
    pool.shutdown();
  }
}
