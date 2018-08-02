package g419.liner2.cli.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import g419.corpus.ConsolePrinter;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.ChunkerEvaluator;
import g419.liner2.core.tools.ChunkerEvaluatorMuc;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Porównuje zbiory anotacji dla wskazanych korpusów. Korpusy porównywane są parami.
 *
 * @author Michał Marcińczuk
 */
public class ActionAgreement2 extends Action {

    public static final String OPTION_SINGLE_DOCUMENT = "L";
    public static final String OPTION_SINGLE_DOCUMENT_LONG = "load-as-single-document";
    public static final String OPTION_SINGLE_DOCUMENT_DESC = "load sets of document and transform them into a single document";


    final List<Pattern> types = Lists.newArrayList(Pattern.compile(".+"));
    private String[] inputFiles = null;
    private String[] inputFormats = null;
    private boolean loadAsSingleDocument = false;

    public ActionAgreement2() {
        super("agreement2");
        setDescription("compare sets of annotations for each pair of corpora. One set is treated as a reference set and the other as a set to evaluate. It is a refactored version of the agreement action.");
        options.addOption(CommonOptions.getInputFileFormatsOption());
        options.addOption(CommonOptions.getInputFileNamesOption());
        options.addOption(CommonOptions.getVerboseDeatilsOption());
        options.addOption(getLoadAsSingleDocumentOption());
    }

    public static Option getLoadAsSingleDocumentOption() {
        return Option.builder(OPTION_SINGLE_DOCUMENT)
                .longOpt(OPTION_SINGLE_DOCUMENT_LONG)
                .desc(OPTION_SINGLE_DOCUMENT_DESC).build();
    }


    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        inputFiles = line.getOptionValues(CommonOptions.OPTION_INPUT_FILE);
        inputFormats = line.getOptionValues(CommonOptions.OPTION_INPUT_FORMAT);
        loadAsSingleDocument = line.hasOption(OPTION_SINGLE_DOCUMENT);
        if (line.hasOption(CommonOptions.OPTION_VERBOSE_DETAILS)) {
            ConsolePrinter.verboseDetails = true;
        }

    }

    @Override
    public void run() throws Exception {
        for (int i1 = 0; i1 < inputFiles.length; i1++) {
            for (int i2 = i1 + 1; i2 < inputFiles.length; i2++) {
                compare(inputFiles[i1], getSetFormat(inputFormats, i1, "ccl"), inputFiles[i2], getSetFormat(inputFormats, i2, "ccl"), loadAsSingleDocument);
            }
        }
    }

    private String getSetFormat(final String[] formats, final int index, final String defaultValue) {
        if (formats.length == 0) {
            return defaultValue;
        } else if (index < formats.length) {
            return formats[index];
        } else {
            return formats[0];
        }
    }

    private void compare(final String referenceSet, final String referenceSetFormat,
                         final String compareSet, final String compareSetFormat, final boolean loadAsSingleDocument) throws Exception {
        final ChunkerEvaluator globalEval = new ChunkerEvaluator(types, false);
        final ChunkerEvaluatorMuc globalEvalMuc = new ChunkerEvaluatorMuc(types);

        int onlyInReferenceSet = 0;
        int onlyInCompareSet = 0;
        int differentSentenceCount = 0;
        int foundInBoth = 0;

        final Map<String, Document> documentSet1 = loadDocuments(compareSet, compareSetFormat, loadAsSingleDocument);
        final Map<String, Document> documentSet2 = loadDocuments(referenceSet, referenceSetFormat, loadAsSingleDocument);

        final Set<String> names = Stream.of(documentSet1, documentSet2)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        for (final String name : names) {
            final Document d1 = documentSet1.get(name);
            final Document d2 = documentSet2.get(name);

            if (d1 == null || d2 == null) {
                getLogger().warn("Dokument {} znajduje się tylko w {}", name, d1 == null ? compareSet : referenceSet);
                if (d1 == null) {
                    onlyInCompareSet++;
                } else {
                    onlyInReferenceSet++;
                }
            } else if (d1.getSentences().size() != d2.getSentences().size()) {
                getLogger().warn("Dokument {} ma różną liczbę zdań: {} ({}) vs {} ({})",
                        name, d1.getSentences().size(), compareSet, d2.getSentences().size(), referenceSet);
                differentSentenceCount++;
            } else {
                foundInBoth++;
                for (int i = 0; i < d1.getSentences().size(); i++) {
                    final Sentence s1 = d1.getSentences().get(i);
                    final Sentence s2 = d2.getSentences().get(i);
                    final AnnotationSet set1 = new AnnotationSet(s1, s1.getChunks());
                    final AnnotationSet set2 = new AnnotationSet(s2, s2.getChunks());
                    globalEval.evaluate(s1, set1, set2);
                    globalEvalMuc.evaluate(s1, set1, set2);
                }
            }
        }

        System.out.println();
        System.out.println(StringUtils.repeat("-", 90));
        System.out.println("Reference set: " + referenceSet);
        System.out.println("Testing set  : " + compareSet);
        System.out.println(StringUtils.repeat("-", 90));
        System.out.println(String.format("Documents only in 'reference set'             : %4d", onlyInReferenceSet));
        System.out.println(String.format("Documents only in 'compare set'               : %4d", onlyInCompareSet));
        System.out.println(String.format("Documents with different number of sentences  : %4d", differentSentenceCount));
        System.out.println(String.format("Documents found in both sets                  : %4d", foundInBoth));
        globalEval.printResults();
        globalEvalMuc.printResults();
    }

    private Map<String, Document> loadDocuments(final String filename, final String inputFormat, final boolean loadAsSingleDocument) throws Exception {
        final Map<String, Document> documents = Maps.newHashMap();
        ReaderFactory.get().getStreamReader(filename, inputFormat).forEach(doc -> documents.put(doc.getName(), doc));
        return transformIfRequired(documents, loadAsSingleDocument);
    }

    private Map<String, Document> transformIfRequired(final Map<String, Document> documents, final boolean loadAsSingleDocument) {
        if (loadAsSingleDocument && documents.size() > 0) {
            final TokenAttributeIndex tai = documents.values().iterator().next().getAttributeIndex();
            final Paragraph p = new Paragraph("p1", tai);
            documents.values().stream()
                    .map(Document::getSentences)
                    .flatMap(Collection::stream)
                    .filter(s -> s.getTokenNumber() > 0)
                    .forEach(p::addSentence);
            int n = 1;
            for (final Sentence s : p.getSentences()) {
                System.out.println((n++) + " " + s.toString());
            }
            final Document doc = new Document("joined", Lists.newArrayList(p), tai);
            final Map<String, Document> newSet = Maps.newHashMap();
            newSet.put(doc.getName(), doc);
            return newSet;
        } else {
            return documents;
        }
    }

}
