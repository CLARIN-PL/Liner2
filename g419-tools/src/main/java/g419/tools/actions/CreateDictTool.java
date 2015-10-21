package g419.tools.actions;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.lib.cli.CommonOptions;
import g419.lib.cli.ParameterException;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.ValueComparator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by michal on 1/9/15.
 */
public class CreateDictTool extends Tool{

    public static final String OPTION_BASE_FORM = "b";
    public static final String OPTION_BASE_FORM_LONG = "base";

    private String input_file = null;
    private String input_format = null;
    private String output_file = null;
    private boolean baseForm = false;

    public CreateDictTool() {
        super("createDict");
        this.setDescription("ToDo");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());
        OptionBuilder.withDescription("use base forms of named entities");
        OptionBuilder.withLongOpt(OPTION_BASE_FORM_LONG);
        this.options.addOption(OptionBuilder.create(OPTION_BASE_FORM));
    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
        if(line.hasOption(OPTION_BASE_FORM)){
            baseForm = true;
        }
    }

    @Override
    public void run() throws Exception {
        if ( !LinerOptions.getGlobal().isOption(LinerOptions.OPTION_USED_CHUNKER) ){
            throw new ParameterException("Parameter 'chunker' in 'main' section of model not set");
        }

        AbstractDocumentReader reader = getInputReader();
        TokenFeatureGenerator gen = null;

        if (!LinerOptions.getGlobal().features.isEmpty()){
            gen = new TokenFeatureGenerator(LinerOptions.getGlobal().features);
        }

		/* Create all defined chunkers. */
        ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
        cm.loadChunkers();

        Chunker chunker = cm.getChunkerByName(LinerOptions.getGlobal().getOptionUse());

        getNamesFrequency(reader, gen, chunker);
//        getNamesWithSentences(reader, gen, chunker);



    }

    private void getNamesWithSentences(AbstractDocumentReader reader, TokenFeatureGenerator gen, Chunker chunker) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.output_file));
        Document ps = reader.nextDocument();
        while ( ps != null ) {
            if (gen != null) {
                gen.generateFeatures(ps);
            }
            chunker.chunkInPlace(ps);
            for (Annotation ann : ps.getAnnotations()) {
                String namedEntity = baseForm ? ann.getBaseText() : ann.getText();
                String category = ann.getType();
                String sentence = baseForm ? ann.getSentence().toBaseString() : ann.getSentence().toString();
                writer.write(namedEntity + "\t" + category + "\t" + sentence + "\n");

            }
            ps = reader.nextDocument();
        }
        reader.close();
        writer.close();
    }

    private void getNamesFrequency(AbstractDocumentReader reader, TokenFeatureGenerator gen, Chunker chunker) throws Exception {
        HashMap<String, HashMap<String, Integer>> allNamedEntities = new HashMap<String, HashMap<String, Integer>>();
        HashMap<String, Integer> numBooksNoCategory = new HashMap<String, Integer>();
        Document ps = reader.nextDocument();
        while ( ps != null ){
            HashMap<String, HashSet<String>> namedEntitiesInBook = new HashMap<String, HashSet<String>>();
            System.out.println(ps.getName());
            if ( gen != null )
                gen.generateFeatures(ps);
            chunker.chunkInPlace(ps);
            for(Annotation ann: ps.getAnnotations()){
                String namedEntity = baseForm ? ann.getBaseText() : ann.getText();
                        String category = ann.getType();
//                <<<<<<<<< liczenie po ksiazkach
//                if(!namedEntitiesInBook.containsKey(namedEntity)){
//                    namedEntitiesInBook.put(namedEntity, new HashSet<String>());
//                }
//                namedEntitiesInBook.get(namedEntity).add(category);
//               >>>>>>>>>>
                if(allNamedEntities.containsKey(namedEntity)){
                    HashMap<String, Integer> allCategories = allNamedEntities.get(namedEntity);
                    if(allCategories.containsKey(category)){
                        allCategories.put(category, allCategories.get(category) + 1);
                    }
                    else{
                        allCategories.put(category, 1);
                    }
                }
                else{
                    allNamedEntities.put(namedEntity, new HashMap<String, Integer>());
                    allNamedEntities.get(namedEntity).put(category, 1);
                }
            }
//          <<<<<<<<< liczenie po ksiazkach
//            for(String namedEntity: namedEntitiesInBook.keySet()){
//                if(allNamedEntities.containsKey(namedEntity)){
//                    HashMap<String, Integer> allCategories = allNamedEntities.get(namedEntity);
//                    for(String category: namedEntitiesInBook.get(namedEntity)){
//                        if(allCategories.containsKey(category)){
//                            allCategories.put(category, allCategories.get(category) + 1);
//                        }
//                        else{
//                            allCategories.put(category, 1);
//                        }
//                    }
//                }
//                else{
//                    allNamedEntities.put(namedEntity, new HashMap<String, Integer>());
//                    for(String category: namedEntitiesInBook.get(namedEntity)){
//                        allNamedEntities.get(namedEntity).put(category, 1);
//                    }
//                }
//
//                if(numBooksNoCategory.containsKey(namedEntity)){
//                    numBooksNoCategory.put(namedEntity, numBooksNoCategory.get(namedEntity) + 1);
//                }
//                else{
//                    numBooksNoCategory.put(namedEntity, 1);
//                }
//
//            }
//            >>>>>>>>>>

            ps = reader.nextDocument();
        }
        reader.close();
        BufferedWriter dictWriter = new BufferedWriter(new FileWriter(this.output_file));
        for(String namedEntity: allNamedEntities.keySet()){
            String entityInfo = namedEntity + "\t{sum}";
            int sum = 0;
            Map<String, Integer> allCategories = allNamedEntities.get(namedEntity);
            Map<String,Integer> sortedCategories = ValueComparator.sortByValues(allCategories, true);
            for(String category: sortedCategories.keySet()){
                int count = allCategories.get(category);
                sum += count;
                entityInfo += "\t" + category + "\t" + count;
            }
//            entityInfo = entityInfo.replace("{sum}",  numBooksNoCategory.get(namedEntity) + "");
            entityInfo = entityInfo.replace("{sum}",  sum + "");
            dictWriter.write(entityInfo + "\n");
        }
        dictWriter.close();
    }

    protected AbstractDocumentReader getInputReader() throws Exception{
        return ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
    }

}
