package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import io.vavr.control.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ActionPoleval2019 extends Action {

    private String inputFile = null;
    private String inputFormat = null;
    private String outputFile = null;
    private final AtomicInteger globalId = new AtomicInteger();

    public ActionPoleval2019() {
        super("poleval2019");
        setDescription("generates data for PolEval 2019 task");
        options.addOption(CommonOptions.getInputFileFormatOption());
        options.addOption(CommonOptions.getInputFileNameOption());
        options.addOption(CommonOptions.getOutputFileNameOption());
    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        inputFile = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        outputFile = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
    }

    @Override
    public void run() throws Exception {
        try (final AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(inputFile, inputFormat);
             final CSVPrinter tsv = new CSVPrinter(Files.newBufferedWriter(Paths.get(outputFile)), CSVFormat.TDF)) {
            reader.forEachRemaining(d -> processDocument(d, tsv));
        }
    }

    private void processDocument(final Document document, final CSVPrinter csv) {
        document.getSentences().forEach(s -> processSentence(s, csv));
    }

    private void processSentence(final Sentence sentence, final CSVPrinter csv) {
        sentence.getChunks().stream()
                .filter(this::validAnnotation)
                .filter(this::lemmaDiffersFromOrth)
                .filter(an -> an.getLemma() != null)
                .map(this::annotationToKeyAnn)
                .collect(Collectors.groupingBy(Pair::getKey))
                .values().stream()
                .map(List::iterator)
                .map(Iterator::next)
                .map(Pair::getValue)
                .sorted(Comparator.comparing(Annotation::getBegin)
                        .thenComparing(Annotation::getTokenCount, Comparator.reverseOrder()))
                .forEach(an -> writeAnnotation(csv, sentence.getDocument(), an));
    }

    private void writeAnnotation(final CSVPrinter csv, final Document document, final Annotation annotation) {
        try {
            csv.printRecord(globalId.incrementAndGet(), document.getName(), annotation.getText(), annotation.getLemma());
        } catch (final IOException ex) {
            getLogger().error("Exception thrown while writing to CSV", ex);
        }
    }

    private Pair<String, Annotation> annotationToKeyAnn(final Annotation an) {
        return new ImmutablePair<>(String.format("%d:%d", an.getBegin(), an.getEnd()), an);
    }

    private boolean validAnnotation(final Annotation an) {
        return ("keyword".equals(an.getType()) && an.getTokens().size() > 1)
                || (Option.of(an.getType()).getOrElse("").startsWith("nam_"));
    }

    private boolean lemmaDiffersFromOrth(final Annotation an) {
        return !Objects.equals(an.getText(), an.getLemma());
    }
}
