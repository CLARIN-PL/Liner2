package g419.tools.actions;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.SparseArffWriter;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.CommonOptions;
import g419.liner2.api.tools.parser.MaltParser;
import g419.liner2.api.tools.parser.MaltSentence;
import g419.tools.maltfeature.MaltFeatureGenerator;
import g419.tools.maltfeature.MaltPattern;
import g419.tools.utils.SparseMatrixCounter;

/**
 * 
 * @author czuk
 *
 */
public class FeatureExtractor extends Tool{

    public static final String OPTION_MALT = "m";
    public static final String OPTION_MALT_LONG = "malt";

    public static final String OPTION_PATTERNS = "p";
    public static final String OPTION_PATTERNS_LONG = "patterns";

    private String input_file = null;
    private String input_format = null;
    private String filename_patterns = null;
    private String modelPath = null;
    private Map<String, String> rowLabels = new HashMap<String, String>();
    private Set<String> classes = new HashSet<String>();

    public FeatureExtractor() {
        super("feature-extractor");
        this.setDescription("Generuje wektor cech dla wskazanych anotacji");

        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getFeaturesOption());
        this.options.addOption(CommonOptions.getModelFileOption());

        this.options.addOption(
        		Option.builder(OPTION_MALT).longOpt(OPTION_MALT_LONG).hasArg().argName("malt")
        		.desc("path to maltparser model").required().build());

        this.options.addOption(
        		Option.builder(OPTION_PATTERNS).longOpt(OPTION_PATTERNS_LONG).hasArg().argName("patterns")
        		.desc("path to file with patterns").required().build());
    }

    @Override
    public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        this.modelPath = line.getOptionValue(OPTION_MALT);
        this.filename_patterns = line.getOptionValue(OPTION_PATTERNS);
    }

    @Override
    public void run() throws Exception {
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
        Document ps = null;
        MaltParser malt = new MaltParser(this.modelPath);

        ArrayList<MaltPattern> patterns = new ArrayList<>();

        for ( String pattern : Files.readAllLines(Paths.get(this.filename_patterns) )){
        	pattern = pattern.trim();
        	if ( pattern.length() > 0 ){
        		MaltPattern p = new MaltPattern(pattern);
        		if ( p != null ){
        			patterns.add(p);
        		}
        		else{
        			Logger.getLogger(this.getClass()).error("Błąd parsowania wzorca: " + pattern);
        		}
        	}
        }
        MaltFeatureGenerator featureGenerator = new MaltFeatureGenerator(this.modelPath, patterns);
    	SparseMatrixCounter counter = new SparseMatrixCounter();
        
    	int i = 1;
    	
        while ( (ps = reader.nextDocument()) != null ){
        	for ( Sentence sentence : ps.getSentences() ){
        		MaltSentence maltSent = new MaltSentence(sentence, sentence.getChunks());
        		malt.parse(maltSent);
        		
                for ( Annotation ann : maltSent.getAnnotations() ) {
                	String annId = "an" + i++;
                	this.rowLabels.put(annId, ann.getType());
                	this.classes.add(ann.getType());
            		for ( String feature : featureGenerator.extractFeatures(ann, maltSent) ){
            			counter.addItem(annId, feature);
            		}
                }
        	}
        }
        reader.close();
        
//        Set<String> removeColumns = new HashSet<String>();
//        // Usuń kolumny o łącznej liczności < 10
//        for ( String column : counter.getColumns() ){
//        	int sum = counter.sumColumn(column);
//        	if ( sum < 10 ){
//        		removeColumns.add(column);
//        	}
//        }
//        Logger.getLogger(this.getClass()).info(String.format("Liczba kolumn do usunięcia : %d", removeColumns.size()));
//        counter.removeColumns(removeColumns);
//        Logger.getLogger(this.getClass()).info(String.format("Liczba kolumn pozostawioych: %d", counter.getColumns().size()));
        
        SparseArffWriter writer = new SparseArffWriter(
        		new FileOutputStream("output.arff"), "ner-categories", 
        			new ArrayList<String>(counter.getColumns()), this.classes);
        for ( String row : counter.getRows() ){
        	writer.writeInstance(this.rowLabels.get(row), counter.getRowValues(row));
        }
        writer.close();
            	
    }

}
