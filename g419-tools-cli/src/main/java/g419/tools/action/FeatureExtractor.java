package g419.tools.action;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.ArffAttributeType;
import g419.corpus.io.writer.SparseArffWriter;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.tools.parser.MaltParser;
import g419.liner2.core.tools.parser.MaltSentence;
import g419.liner2.core.tools.parser.MaltSentenceLink;
import g419.tools.maltfeature.MaltFeatureGenerator;
import g419.tools.maltfeature.MaltPattern;
import g419.tools.utils.Counter;
import g419.tools.utils.SparseMatrixCounter;
import g419.tools.utils.SparseMatrixValue;

/**
 * 
 * @author czuk
 *
 */
public class FeatureExtractor extends Action{

    public static final String OPTION_MALT = "m";
    public static final String OPTION_MALT_LONG = "malt";

    public static final String OPTION_PATTERNS = "p";
    public static final String OPTION_PATTERNS_LONG = "patterns";

    private String inputFile = null;
    private String inputFormat = null;
    private String filenamePatterns = null;
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
        this.inputFile = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        this.modelPath = line.getOptionValue(OPTION_MALT);
        this.filenamePatterns = line.getOptionValue(OPTION_PATTERNS);
    }

    @Override
    public void run() throws Exception {
        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.inputFile, this.inputFormat);
        Document ps = null;
        MaltParser malt = new MaltParser(this.modelPath);

        List<MaltPattern> patterns = new ArrayList<MaltPattern>();

        for ( String pattern : Files.readAllLines(Paths.get(this.filenamePatterns) )){
        	pattern = pattern.trim();
        	if ( pattern.length() > 0 ){
        		MaltPattern p = new MaltPattern(pattern);
       			patterns.add(p);
        	}
        }
        MaltFeatureGenerator featureGenerator = new MaltFeatureGenerator(this.modelPath, patterns);
    	SparseMatrixCounter counters = new SparseMatrixCounter();
    	SparseMatrixValue values = new SparseMatrixValue();
    	Map<String, ArffAttributeType> attributeTypes = new HashMap<String, ArffAttributeType>();
        
    	for ( String row : counters.getRows() ){
    		for ( String column : counters.getColumns() ){
    			Counter counter = counters.getCounter(row, column);
    			String value = counter == null ? "" : counter.getValue().toString();
    			values.setValue(row, column, value);
    		}
    	}
    	
    	int i = 1;    	
        while ( (ps = reader.nextDocument()) != null ){
        	for ( Sentence sentence : ps.getSentences() ){
        		MaltSentence maltSent = new MaltSentence(sentence, sentence.getChunks());
        		malt.parse(maltSent);
        		
                for ( Annotation ann : sentence.getChunks() ) {
                	String annId = String.format("an%d", i++);
                	this.rowLabels.put(annId, ann.getType());
                	this.classes.add(ann.getType());
//            		for ( String feature : featureGenerator.extractFeatures(ann, maltSent) ){
//            			counters.addItem(annId, feature);
//            		}
                	String parentBase = null;
                	MaltSentenceLink parentLink = maltSent.getLink(ann.getHead());
                	if ( parentLink != null ){
                		int parentIndex = parentLink.getTargetIndex();
                		if ( parentIndex > -1 ){
                			parentBase = sentence.getTokens().get(parentIndex).getDisambTag().getBase();
                		}
                	}
            		values.setValue(annId, "phraseOrth", ann.getText());
            		values.setValue(annId, "phraseBase", ann.getBaseText());
            		values.setValue(annId, "headBase", ann.getHeadToken().getDisambTag().getBase());
            		values.setValue(annId, "parentBase", parentBase );
                }
        	}
        }
        reader.close();
        
        /* Numeric attributes */
        for ( String attribute : counters.getColumns() ){
        	attributeTypes.put(attribute, ArffAttributeType.NUMERIC);
        }
        /* String attributes */
        for ( String attribute : values.getColumns() ){
        	attributeTypes.put(attribute, ArffAttributeType.STRING);
        }
        
        SparseArffWriter writer = new SparseArffWriter(
        		new FileOutputStream("output.arff"), "ner-categories", attributeTypes, this.classes);
        for ( String row : values.getRows() ){
        	writer.writeInstance(this.rowLabels.get(row), values.getRowValuesSparse(row));
        }
        writer.close();
            	
    }
    
}
