package g419.liner2.cli.action;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import g419.corpus.Logger;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.action.Action;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.normalizer.Normalizer;
import g419.liner2.api.tools.NormalizerEvaluator;
import g419.liner2.api.tools.ParameterException;
import g419.liner2.api.tools.ProcessingTimer;
import g419.liner2.api.tools.Result;
import g419.lib.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionConstituentsEval extends Action {

    private String input_file = null;
    private String input_format = null;
    private TokenFeatureGenerator gen = null;
    private Set<String> inKeys;
    private Set<String> outKeys;

    @SuppressWarnings("static-access")
    public ActionConstituentsEval() {
        super("constituents-eval");
        this.setDescription("evaluates normalizer against a specific set of documents (-i batch:FORMAT, -i FORMAT)");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getModelFileOption());
        this.options.addOption(CommonOptions.getVerboseDeatilsOption());
        this.options.addOption(OptionBuilder.withArgName("inKeys").hasArg().
                        withLongOpt("inKeys").
                        withDescription("Set of metadata keys (joined with ;), used as input annotation metadata").
                        create("inKeys")
        );
        this.options.addOption(OptionBuilder.withArgName("outKeys").hasArg().
                        withLongOpt("outKeys").
                        withDescription("Set of metadata keys (joined with ;), used as output annotation metadata").
                        create("outKeys")
        );
    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
        if(line.hasOption(CommonOptions.OPTION_VERBOSE_DETAILS)) {
            Logger.verboseDetails = true;
        }
//        if (!line.hasOption("inKeys"))
//            throw new IllegalStateException("Provide 'inKeys' argument!"); //todo: specialize
        inKeys = new HashSet<>(
                line.hasOption("inKeys") ?
                        Arrays.asList(line.getOptionValue("inKeys").split("[;]")) :
                        new ArrayList<>()
        );
        if (!line.hasOption("outKeys"))
            throw new IllegalStateException("Provide 'outKeys' argument!"); //todo: specialize
        outKeys = new HashSet<>(Arrays.asList(line.getOptionValue("outKeys").split("[;]")));
    }


    /**
     *
     */
    public void run() throws Exception {

        if (!LinerOptions.isGlobalOption(LinerOptions.OPTION_USED_CHUNKER)) {
            throw new ParameterException("Parameter 'chunker' in 'main' section of model configuration not set");
        }

        if (!LinerOptions.getGlobal().features.isEmpty()) {
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

        Normalizer normalizer = getNormalizer();

        System.out.print("Annotations to evaluate:");
            for (Pattern pattern : normalizer.getNormalizedChunkTypes())
                System.out.print(" " + pattern);
        System.out.println();

        evaluate(ReaderFactory.get().getStreamReader(this.input_file, this.input_format), gen, normalizer);
    }

    protected Normalizer getNormalizer() throws Exception {
        ChunkerManager manager = new ChunkerManager(LinerOptions.getGlobal());
        manager.loadChunkers();
        manager.loadTestData(ReaderFactory.get().getStreamReader(this.input_file, this.input_format), gen);
        Chunker chunker = manager.getChunkerByName(LinerOptions.getGlobal().getOptionUse());
        if (!(chunker instanceof Normalizer)) {
            ClassCastException up = new ClassCastException("Specified Chunker is not Normalizer, but "+chunker.getClass().getSimpleName());
            throw up;
        }
        Normalizer normalizer = (Normalizer) chunker;
        if (normalizer.getNormalizedChunkTypes().isEmpty()) {
            RuntimeException up = new RuntimeException("This should be checked in factory item!");
            throw up;
        }
        manager.resetChunkers();
        Logger.log("Using normalizer: "+normalizer);
        return normalizer;

    }

    private Map<String, Map<String, Map<Result, Integer>>> results = new HashMap<>();

    private Map<Result, Integer> getTypeResults(String type) {
        Map<Result, Integer> out = new HashMap<>();
        for (Result r: Result.values())
            out.put(r, 0);
        for (String part: parts){
            for (Result r: Result.values()) {
                ensurePlaceholder(type, part, r);
                int outVal = out.get(r);
                int resultVal = results.get(type).get(part).get(r);
                out.put(r, outVal+resultVal);
            }
        }
        return out;
    }

    private Map<Result, Integer> getPartResults(String part) {
        Map<Result, Integer> out = new HashMap<>();
        for (Result r: Result.values())
            out.put(r, 0);
        for (String type: results.keySet()){
            for (Result r: Result.values()) {
                ensurePlaceholder(type, part, r);
                int outVal = out.get(r);
                int resultVal = results.get(type).get(part).get(r);
                out.put(r, outVal+resultVal);
            }
        }
        return out;
    }

    private Map<Result, Integer> getAllResults(){
        Map<Result, Integer> out = new HashMap<>();
        for (String part: parts){
            Map<Result, Integer> partResults = getPartResults(part);
            for (Result r: Result.values()) {
                if (!out.containsKey(r))
                    out.put(r, 0);
                int outVal = out.get(r);
                int resultVal = partResults.get(r);
                out.put(r, outVal + resultVal);
            }
        }
        return out;
    }

    private void ensurePlaceholder(String type, String part, Result result){
        if (!results.containsKey(type)){
            results.put(type, new HashMap<String, Map<Result, Integer>>());
        }
        if (!results.get(type).containsKey(part)){
            results.get(type).put(part, new HashMap<Result, Integer>());
        }
        if (!results.get(type).get(part).containsKey(result))
            results.get(type).get(part).put(result, 0);
    }

    private void saveResult(String type, String part, Result result){
        ensurePlaceholder(type, part, result);
        int val = results.get(type).get(part).get(result) + 1;
        results.get(type).get(part).put(result, val);
    }

    static final List<String> parts = Arrays.asList(
            "year", "month", "day", "hour", "minute", "dayPart",
            "decade", "century", "season", "centuryPart",
            "yearPart", "monthPart"
    );
    static final Pattern partsPattern = Pattern.compile(
            "(?<year>^\\d\\d\\d\\d)(" +
            "[\\-]?((?<month>\\d\\d)|(?<season>[SFW][PUAI])|" +
            "(?<yearPart>[Q|H]\\d))(" +
            "[\\-]?((?<day>\\d\\d)|(?<monthPart>H\\d))(" +
            "[Tt]((?<hour>\\d\\d)|(?<dayPart>[MAEN][OIFV]))(" +
            "[\\:](?<minute>\\d\\d))?)?)?)?|" +
            "(?<decade>^\\d\\d\\d)|" +
            "((?<century>^\\d\\d)[\\-]?(?<centuryPart>[HQ]\\d)?)"
    );

    private Map<String, String> toPartMap(String val){
        Matcher matcher = partsPattern.matcher(val);
        if (!matcher.find()){
            return null;
        } else {
            Map<String, String> out = new HashMap<>();
            for (String part: parts){
                out.put(part, matcher.group(part));
            }
            return out;
        }

    }

    private void analyzeResult(Map<String, String> originalMeta, Annotation annotation){
        String type = annotation.getType();
        for (String key: outKeys) {
            if (!originalMeta.containsKey(key))
                throw new RuntimeException("All annotations must be pre-described with '"+key+"' metadata key!");
            Map<String, String> original = toPartMap(originalMeta.get(key));
            if (original == null)
                continue;
            Map<String, String> generated = annotation.getMetadata().containsKey(key) ?
                    toPartMap(annotation.getMetadata().get(key)) :
                    new HashMap<String, String>();
            if (generated == null)
                generated = new HashMap<String, String>();
            for (String part: parts){
                String originalPart = original.get(part);
                String generatedPart = generated.get(part);
                if (originalPart == null)
                    if (generatedPart == null)
                        saveResult(type, part, Result.TN);
                    else
                        continue;
                else
                    if (generatedPart == null)
                        saveResult(type, part, Result.FN);
                    else
                        if (generatedPart.equals(originalPart))
                            saveResult(type, part, Result.TP);
                        else
                            saveResult(type, part, Result.FP);
            }
        }
    }

    private void evaluate(AbstractDocumentReader dataReader, TokenFeatureGenerator gen, Normalizer normalizer) throws Exception {
        Document document = dataReader.nextDocument();
        while ( document != null ){
    		/* Generate features */
            if ( gen != null )
                gen.generateFeatures(document);
            normalizer.onNewDocument(document);
            for (Sentence sentence: document.getSentences()){
                normalizer.onNewSentence(sentence);
                annotationLoop:
                for (Annotation annotation: sentence.getAnnotations(normalizer.getNormalizedChunkTypes())){
                    normalizer.onNewAnnotation(annotation);
                    Map<String, String> originalMeta = annotation.getMetadata();
                    annotation.setMetadata(new HashMap<String, String>());
                    for (String key: inKeys)
                        if (originalMeta.containsKey(key))
                            annotation.getMetadata().put(key, originalMeta.get(key));
                    try {
                        normalizer.normalize(annotation);
                    } catch (IllegalStateException ignored){}
                    try {
                        analyzeResult(originalMeta, annotation);
                    } catch (Throwable t){
                        continue annotationLoop;
                    }
                    normalizer.onAnnotationEnd(annotation);
                    annotation.setMetadata(originalMeta);
                }
                normalizer.onSentenceEnd(sentence);
            }
            normalizer.onDocumentEnd(document);
            document = dataReader.nextDocument();
        }
        printResults();
    }

    private static final String separator = ";";

    private void printRow(int tp, int fp, int fn, String... strings){
        for (String s: strings){
            System.out.print(s+separator);
        }
        System.out.print(""+tp+separator+fp+separator+fn+separator);
        double precision = tp+fp==0 ? 0 : 1.0*tp/(tp+fp);
        double recall = tp+fn==0 ? 0 : 1.0*tp/(tp+fn);
        double f = precision+recall == 0 ? 0 : 2*precision*recall/(precision+recall);
        System.out.print(String.format("%.2f", precision)+separator);
        System.out.print(String.format("%.2f", recall)+separator);
        System.out.print(String.format("%.2f", f));
        System.out.println();
    }

    private void printResults(){
        List<String> types = new ArrayList<String>(results.keySet());
        Collections.sort(types);
        System.out.println(
                "Part"+separator+
                "TP"+separator+
                "FP"+separator+
                "FN"+separator+
                "Precision"+separator+
                "Recall"+separator+
                "F"
        );
//        System.out.println("===================================================================");
        for (String type: types){
//            System.out.println("Type:"+separator+type);
            Map<Result, Integer> typeResults = getTypeResults(type);
            printRow(typeResults.get(Result.TP), typeResults.get(Result.FP), typeResults.get(Result.FN), type);
//            for (String part: parts)
//                printRow(
//                        results.get(type).get(part).get(Result.TP),
//                        results.get(type).get(part).get(Result.FP),
//                        results.get(type).get(part).get(Result.FN),
//                        part
//                );
//            System.out.println("-------------------------------------------------------------------");
        }
//        System.out.println("SUMMARY");
        Map<Result, Integer> total = getAllResults();
        printRow(total.get(Result.TP), total.get(Result.FP), total.get(Result.FN), "*");
    }
}
