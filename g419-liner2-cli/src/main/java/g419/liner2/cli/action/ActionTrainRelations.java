package g419.liner2.cli.action;

import fasttext.Args;
import fasttext.Pair;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.*;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.FastTextRelationChunker;
import g419.liner2.core.chunker.factory.ChunkerManager;
import g419.liner2.core.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import fasttext.FastText;
import org.apache.commons.cli.Option;


/**
 * Training model with in-sentence relations like slink, alink
 *
 * @author Jan Kocoń
 */
public class ActionTrainRelations extends Action {

    private String input_file = null;
    private String input_format = "batch:cclrel";
    private String output_prefix = null;
    private String mode = null;
    private Set<String> chosenRelations = null;
    private boolean content = false;

    public ActionTrainRelations() {
        super("train-rel");
        this.setDescription("processes data with given model");

        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getModelFileOption());
        this.options.addOption(Option.builder("mode").longOpt("mode")
                .required()
                .hasArg().argName("mode").desc("choose mode (train, test)").build());
        this.options.addOption(Option.builder("relations")
                .longOpt("relations")
                .hasArg().argName("relations").desc("define relation subset, e.g.: alink,slink,null").build());
        this.options.addOption(Option.builder("content")
                .longOpt("content")
                .desc("include content between annotations in training/testing data").build());

    }

    protected ActionTrainRelations(final String name) {
        super(name);
    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        this.output_prefix = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.mode = line.getOptionValue("mode");
        this.content = line.hasOption("content");
        if (!this.mode.equals("train") && !this.mode.equals("test") )
            throw new Exception("mode must be 'train' or 'test'!");
        this.chosenRelations = new HashSet<String>(Arrays.asList(line.getOptionValue("relations").split(",")));
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
    }



    public String getRepresentation(Annotation annotationFrom, Annotation annotationTo, String type) throws IllegalArgumentException{
        String representation = FastTextRelationChunker.getRepresentation(annotationFrom, annotationTo, this.content);
        if (representation != null)
            return "__label__" + type + " " + representation;
        return null;
    }

    /**
     * Module entry function.
     */
    public void run() throws Exception {


        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);

        TokenFeatureGenerator gen = null;

        if (!LinerOptions.getGlobal().features.isEmpty()) {
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

        PrintWriter writer = new PrintWriter(this.output_prefix + "." + this.mode + ".txt", "UTF-8");
        Document ps = reader.nextDocument();
        while (ps != null) {
            if (gen != null)
                gen.generateFeatures(ps);
            Set<Relation> relations = ps.getRelationsSet();

            Map<Map.Entry<Annotation, Annotation>, String> relationAnnotationTypes = new HashMap<>();
            Set<Map.Entry<Annotation, Annotation>> relationAnnotations = new HashSet<>();
            for (Relation relation : relations) {
                Annotation annotationFrom = relation.getAnnotationFrom();
                Annotation annotationTo = relation.getAnnotationTo();
                String type = relation.getType();
                if (this.chosenRelations.contains(type)) {
                    Map.Entry<Annotation, Annotation> entry = new AbstractMap.SimpleEntry<>(annotationFrom, annotationTo);
                    relationAnnotations.add(entry);
                    relationAnnotationTypes.put(entry, type);
                }
            }


            for (Map.Entry<Sentence, AnnotationSet> entry : ps.getChunkings().entrySet()) {
                Sentence sentence = entry.getKey();
                LinkedHashSet<Annotation> annotationSet = entry.getValue().chunkSet();
                if (annotationSet.size() > 1)
                    for (Annotation annotationFrom : annotationSet)
                        for (Annotation annotationTo : annotationSet)
                            if (!annotationFrom.equals(annotationTo)) {
                                Map.Entry<Annotation, Annotation> annotationEntry = new AbstractMap.SimpleEntry<>(annotationFrom, annotationTo);
                                String representation = null;
                                String relationType = "null";
                                if (relationAnnotations.contains(annotationEntry))
                                    relationType = relationAnnotationTypes.get(annotationEntry);
                                if (chosenRelations == null || chosenRelations.contains(relationType))
                                    representation = getRepresentation(annotationFrom, annotationTo, relationType);
                                if (representation != null)
                                    writer.println(representation);
                            }
            }

            ps = reader.nextDocument();
        }

        reader.close();
        writer.close();

        if (this.mode.equals("train")) {
            FastText fasttext = new FastText();
            Args a = new Args();
            a.parseArgs(new String[]{
                    "supervised",
                    "-input", this.output_prefix + ".train.txt",
                    "-output", this.output_prefix + ".model",
                    "-dim", "50",
                    "-epoch", "100",
                    "-ws", "5",
                    "-wordNgrams", "2",
                    "-minn", "0",
                    "-maxn", "3",
                    "-lr", "0.1",
                    "-loss", "softmax",
                    "-thread", "12",
                    "-label", "__label__"
            });
            fasttext.train(a);
        }
        else {
            FastText fasttext = new FastText();
            fasttext.loadModel(this.output_prefix + ".model.bin");
            fasttext.test(new FileInputStream(new File(this.output_prefix + ".test.txt")), 1);
        }

    }


}
