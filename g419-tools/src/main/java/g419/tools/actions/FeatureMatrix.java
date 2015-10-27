package g419.tools.actions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.zip.DataFormatException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.lib.cli.CommonOptions;
import g419.tools.maltfeature.MaltFeatureGenerator;
import g419.tools.maltfeature.MaltPattern;
import g419.tools.utils.GiniImpurity;
import g419.tools.utils.SparseMatrixCounter;

/**
 * Created by michal on 2/12/15.
 */
public class FeatureMatrix extends Tool{

    public static final String OPTION_MALT = "m";
    public static final String OPTION_MALT_LONG = "malt";

    public static final String OPTION_PATTERNS = "p";
    public static final String OPTION_PATTERNS_LONG = "patterns";

    private String input_file = null;
    private String input_format = null;
    private String filename_patterns = null;
    private String modelPath = null;

    public FeatureMatrix() {
        super("feature-matrix");
        this.setDescription("Generuje macierz wystąpień cech dla określonych klas anotacji.");

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
                
        while ( (ps = reader.nextDocument()) != null ){
        	featureGenerator.process(ps);
        }
        reader.close();
        
        // Wypisz macierz z impurity
        SparseMatrixCounter counter = featureGenerator.getMatrixCounter();
        System.out.println(counter.toStringHeader() + "\tGini");
    	for ( String row : counter.getRows() ){
    		List<Integer> values = counter.getRowValues(row);
    		int sum = values.stream().mapToInt(Integer::intValue).sum();
    		if ( sum > 10 ){
    			double impiruty = GiniImpurity.calculate(values);
    			System.out.println(String.format("%s\t%2.2f", counter.toString(row), impiruty));
    		}
    	}
    	
    }

}
