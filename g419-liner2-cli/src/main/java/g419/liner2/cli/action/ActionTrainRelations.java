package g419.liner2.cli.action;

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
import g419.liner2.core.chunker.factory.ChunkerManager;
import g419.liner2.core.features.TokenFeatureGenerator;
import org.apache.commons.cli.CommandLine;

import java.io.PrintWriter;
import java.util.*;

/**
 * Chunking in pipe mode.
 *
 * @author Maciej Janicki, Michał Marcińczuk
 */
public class ActionTrainRelations extends Action {

    private String input_file = null;
    private String input_format = "batch:cclrel";
    private String output_file = null;
    private int avgLen = 0;
    private int all = 0;
    private int longer = 0;

    public ActionTrainRelations() {
        super("train-rel");
        this.setDescription("processes data with given model");

        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getModelFileOption());

    }

    protected ActionTrainRelations(final String name) {
        super(name);
    }

    @Override
    public void parseOptions(final CommandLine line) throws Exception {
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
    }


    public String getRepresentation(Annotation annotationFrom, Annotation annotationTo, String type) throws IllegalArgumentException{
        Sentence s = annotationFrom.getSentence();
        if (!annotationTo.getSentence().equals(s))
            throw new IllegalArgumentException("Annotations " + annotationFrom + " and " + annotationTo + " are not from the same sentence!");
        boolean reverseRelation = annotationFrom.getBegin() > annotationTo.getBegin();
        int firstToken = reverseRelation ? annotationTo.getBegin() : annotationFrom.getBegin();
        int lastToken = reverseRelation ? annotationFrom.getEnd() : annotationTo.getEnd();
        if (lastToken - firstToken > 7)
            return null;

        List<Token> sentenceTokens = s.getTokens();
        List<String> representation = new LinkedList<>();
        representation.add("__label__" + type);
        representation.add(reverseRelation ? "1" : "0");
        Set<Token> tokensFrom = new HashSet<>(annotationFrom.getTokenTokens());
        Set<Token> tokensTo = new HashSet<>(annotationTo.getTokenTokens());
        for (int i = firstToken; i <= lastToken; i++){
            Token tok = sentenceTokens.get(i);
            Tag tag = tok.getDisambTag();
            String tokenType = "null";
            int tokenRelation = 0;
            if (tokensFrom.contains(tok)) {
                tokenRelation = 1;
                tokenType = annotationFrom.getType();
            }
            else if (tokensTo.contains(tok)) {
                tokenRelation = 2;
                tokenType = annotationTo.getType();
            }
            //representation.add(tok.getOrth() + "\t" + tag.getBase() + "\t" + tag.getPos() + "\t" + tokenRelation + "\t" + tokenType);
            representation.add(tag.getBase().toLowerCase());
        }
        if (type.equals("slink")) {
            avgLen += (representation.size() - 2);
            all += 1;
            if (representation.size() - 2 <= 8) {
                longer += 1;
            }
        }

        return String.join(" ", representation);
    }

    /**
     * Module entry function.
     */
    public void run() throws Exception {


        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);

        TokenFeatureGenerator gen = null;

        String RELATION_TYPE = "slink";

        if (!LinerOptions.getGlobal().features.isEmpty()) {
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

        /* Create all defined chunkers. */
        //ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
        //cm.loadChunkers();

        //Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

        PrintWriter writer = new PrintWriter(this.output_file, "UTF-8");
        Document ps = reader.nextDocument();
        while (ps != null) {
            //RelationSet relations = ps.getRelations();
            if (gen != null)
                gen.generateFeatures(ps);
            Set<Relation> relations = ps.getRelationsSet();

            Set<Map.Entry<Annotation, Annotation>> relationAnnotations = new HashSet<>();
            for (Relation relation : relations) {
                Annotation annotationFrom = relation.getAnnotationFrom();
                Annotation annotationTo = relation.getAnnotationTo();
                String type = relation.getType();
                if (type.equals(RELATION_TYPE)) {
                    Map.Entry<Annotation, Annotation> entry = new AbstractMap.SimpleEntry<>(annotationFrom, annotationTo);
                    relationAnnotations.add(entry);
                }
            }


            Set<Map.Entry<Annotation, Annotation>> unrelatedAnnotations = new HashSet<>();
            for (Map.Entry<Sentence, AnnotationSet> entry : ps.getChunkings().entrySet()) {
                Sentence sentence = entry.getKey();
                LinkedHashSet<Annotation> annotationSet = entry.getValue().chunkSet();
                if (annotationSet.size() > 1)
                    for (Annotation annotationFrom : annotationSet)
                        for (Annotation annotationTo : annotationSet)
                            if (!annotationFrom.equals(annotationTo)) {
                                Map.Entry<Annotation, Annotation> annotationEntry = new AbstractMap.SimpleEntry<>(annotationFrom, annotationTo);
                                String representation = null;
                                if (!relationAnnotations.contains(annotationEntry)) {
                                    unrelatedAnnotations.add(annotationEntry);
                                    representation = getRepresentation(annotationFrom, annotationTo, "null");
                                }
                                else
                                    representation = getRepresentation(annotationFrom, annotationTo, RELATION_TYPE);
                                if (representation != null)
                                    writer.println(representation);
                            }
            }

            //chunker.chunkInPlace(ps);
            //ps.setRelations(relations);
            ps = reader.nextDocument();
        }

        reader.close();
        writer.close();
        System.out.println(avgLen);
        System.out.println(all);
        System.out.println(avgLen / (float)all);
        System.out.println(longer);
    }


}
