package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

/**
 * Chunking in pipe mode.
 *
 * @author Maciej Janicki, Michał Marcińczuk
 */
public class ActionNormalizerEval3 extends Action {

    private String input_file = null;
    private String input_format = null;
    private String point_from = null;
    private String point_what = null;
    private String point_how = null;


    public static final String OPTION_CONFIGURATION = "c";
    public static final String OPTION_CONFIGURATION_LONG = "configuration";

    public ActionNormalizerEval3() {
        super("normalizer-eval3");
        this.setDescription("processes data with given model");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());
        this.options.addOption(Option.builder(OPTION_CONFIGURATION)
                .longOpt(OPTION_CONFIGURATION_LONG)
                .hasArg().argName("POINT:WHAT:HOW")
                .desc("WHAT will be compared and from what POINT, e.g.: TEXT:ANN:STRICT, " +
                        "TEXT:VAL:RELAXED, ANN:LVAL:RELAXED, LVAL:VAL:RELAXED").build());
    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
        String[] configuration = line.getOptionValue(OPTION_CONFIGURATION).split(":");
        this.point_from = configuration[0];
        this.point_what = configuration[1];
        this.point_how = configuration[2];


    }

    /**
     * Module entry function.
     */
    public ArrayList<Document> read_documents() throws Exception {
        ArrayList<Document> outputList = new ArrayList<Document>();


        AbstractDocumentReader reader = getInputReader();


        Document ps = reader.nextDocument();

        while (ps != null) {
            outputList.add(ps);
            ps = reader.nextDocument();
        }
        reader.close();

        return outputList;
    }

    public void run() throws Exception {
        //todo: remove these two lines
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(new NullAppender());

        if (!LinerOptions.isGlobalOption(LinerOptions.OPTION_USED_CHUNKER)) {
            throw new ParameterException("Parameter 'chunker' in 'main' section of model not set");
        }

        TokenFeatureGenerator gen = null;

        if (!LinerOptions.getGlobal().features.isEmpty()) {
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

		/* Create all defined chunkers. */
        ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
        cm.loadChunkers();

        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

        ArrayList<Document> documents = read_documents();

        double intersectionSize = 0, systemSize = 0, referenceSize = 0;
        for (Document referenceDocument : documents) {
            //System.out.println("Document: " + referenceDocument.getName());
            gen.generateFeatures(referenceDocument);
            chunker.prepare(referenceDocument);
            Document cloneDocument = referenceDocument.clone();

            if (this.point_from.equals("TEXT"))
                cloneDocument.removeAnnotations();
            else if (this.point_from.equals("ANN")) {
                cloneDocument.removeMetadata("lval");
                cloneDocument.removeMetadata("val");
            } else if (this.point_from.equals("LVAL")) {
                cloneDocument.removeMetadata("val");
            }

            //Set<String> typeSet = new HashSet<>(Arrays.asList("t3_date", "t3_time", "t3_duration", "t3_set"));
            //Set<String> typeSet = new HashSet<>(Arrays.asList("t3_date"));
            //Set<String> typeSet = new HashSet<>(Arrays.asList("t3_time"));
            //Set<String> typeSet = new HashSet<>(Arrays.asList("t3_date", "t3_time"));
            //Set<String> typeSet = new HashSet<>(Arrays.asList("t3_duration"));
            Set<String> typeSet = new HashSet<>(Arrays.asList("t3_date", "t3_time", "t3_duration"));
            //Set<String> typeSet = new HashSet<>(Arrays.asList("t3_set"));
            chunker.chunkInPlace(cloneDocument);
            //HashMap<Sentence, AnnotationSet> chunkings = chunker.chunk(cloneDocument);

            ArrayList<Sentence> referenceSentences = referenceDocument.getSentences();
            ArrayList<Sentence> cloneSentences = cloneDocument.getSentences();

            for (int i = 0; i < referenceSentences.size(); i++) {
                HashSet<Annotation> referenceAnnotationSet = referenceSentences.get(i).getChunks().stream()
                        .filter(p -> typeSet.contains(p.getType()))
                        .collect(Collectors.toCollection(HashSet::new));
                HashSet<Annotation> systemAnnotationSet = cloneSentences.get(i).getChunks().stream()
                        .filter(p -> typeSet.contains(p.getType()))
                        .collect(Collectors.toCollection(HashSet::new));

                referenceSize += referenceAnnotationSet.size();
                systemSize += systemAnnotationSet.size();


                if (this.point_what.equals("ANN") && this.point_how.equals("STRICT")) {
                    //(1) How many entities are correctly identified
                    HashSet<Annotation> intersectionSet = new HashSet<>(systemAnnotationSet);
                    intersectionSet.retainAll(referenceAnnotationSet);
                    intersectionSize += intersectionSet.size();
                }


                for (Annotation referenceAnnotation : referenceAnnotationSet)
                    for (Annotation systemAnnotation : systemAnnotationSet) {
                        if (this.point_how.equals("RELAXED")) {
                            if (this.point_what.equals("ANN")) {
                                // (2) If the extents for the entities are correctly identified
                                if (referenceAnnotation.getType().equals(systemAnnotation.getType()) &&
                                        referenceAnnotation.getTokens().stream()
                                                .filter(p -> systemAnnotation.getTokens().contains(p))
                                                .collect(Collectors.toSet()).size() > 0) {
                                    intersectionSize += 1;
                                }
                            } else if (this.point_what.equals("LVAL")) {
                                // (3) How many entity attributes are correctly identified - LVAL
                                if (referenceAnnotation.getType().equals(systemAnnotation.getType()) &&
                                        referenceAnnotation.getTokens().stream()
                                                .filter(p -> systemAnnotation.getTokens().contains(p))
                                                .collect(Collectors.toSet()).size() > 0 &&
                                        referenceAnnotation.metaDataMatchesKey("lval", systemAnnotation)) {
                                    intersectionSize += 1;
                                }
                                //log!
                                if (referenceAnnotation.getType().equals(systemAnnotation.getType()) &&
                                        referenceAnnotation.getTokens().stream()
                                                .filter(p -> systemAnnotation.getTokens().contains(p))
                                                .collect(Collectors.toSet()).size() > 0) {
                                    if (!referenceAnnotation.metaDataMatchesKey("lval", systemAnnotation))
                                    System.out.println(
                                                    referenceAnnotation.getType() + "\t" +
                                                    referenceDocument.getName() + "\t" +
                                                    referenceAnnotation.toString() + "\t|" +
                                                    referenceAnnotation.getBaseText(false) + "|\t" +
                                                    referenceAnnotation.getMetadata().get("lval") + "\t" +
                                                    systemAnnotation.getMetadata().get("lval") + "\t" +
                                                    referenceAnnotation.metaDataMatchesKey("lval", systemAnnotation)
                                    );
                                }

                            } else if (this.point_what.equals("VAL")) {
                                // (3) How many entity attributes are correctly identified - VAL
                                if (referenceAnnotation.getType().equals(systemAnnotation.getType()) &&
                                        referenceAnnotation.getTokens().stream()
                                                .filter(p -> systemAnnotation.getTokens().contains(p))
                                                .collect(Collectors.toSet()).size() > 0 &&
                                        referenceAnnotation.metaDataMatchesKey("val", systemAnnotation)) {
                                    intersectionSize += 1;
                                }
                                //log!
                                if (referenceAnnotation.getType().equals(systemAnnotation.getType()) &&
                                        referenceAnnotation.getTokens().stream()
                                                .filter(p -> systemAnnotation.getTokens().contains(p))
                                                .collect(Collectors.toSet()).size() > 0) {
                                    if (!referenceAnnotation.metaDataMatchesKey("val", systemAnnotation)){
                                    System.out.println(
                                            referenceAnnotation.getType() + "\t" +
                                                    referenceDocument.getName() + "\t" +
                                                    referenceAnnotation.toString() + "\t" +
                                                    referenceAnnotation.getBaseText(false) + "\t" +
                                                    referenceAnnotation.getMetadata().get("lval") + "\t" +
                                                    referenceAnnotation.getMetadata().get("val") + "\t" +
                                                    systemAnnotation.getMetadata().get("val") + "\t" +
                                                    referenceAnnotation.metaDataMatchesKey("val", systemAnnotation)
                                    );}
                                }
                            }
                        }


                    }


            }
        }
        double precision = intersectionSize / systemSize;
        double recall = intersectionSize / referenceSize;
        double fmeasure = 2 * precision * recall / (precision + recall);
        int o = 0;
        NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
        System.out.println(format.format(precision * 100));
        System.out.println(format.format(recall * 100));
        System.out.println(format.format(fmeasure * 100));

        System.out.println(precision + " " + recall + " " + fmeasure);
        System.out.format("%,5f", precision);

    }

    /**
     * Get document reader defined with the -i and -f options.
     *
     * @return
     * @throws Exception
     */
    protected AbstractDocumentReader getInputReader() throws Exception {
        return ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
    }

}
