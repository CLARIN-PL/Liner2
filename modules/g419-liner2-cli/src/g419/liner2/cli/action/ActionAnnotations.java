package g419.liner2.cli.action;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AnnotationArffWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationSet;
import g419.corpus.structure.Document;
import g419.liner2.api.LinerOptions;
import g419.liner2.api.features.AnnotationFeatureGenerator;
import g419.liner2.cli.CommonOptions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

/**
 * Generates an ARFF file with a list of annotations of defined types 
 * with given set of features.
 */
public class ActionAnnotations extends Action {

	public static final String OPTION_ANNOTATION_FEATURE = "a";
	public static final String OPTION_ANNOTATION_FEATURE_LONG = "annotation_features";
	
	private String output_file = null;
	private String input_file = null;
	private String input_format = null;
	private String features_file = null;
	private List<Pattern> types = new ArrayList<Pattern>();
		
	@SuppressWarnings("static-access")
	public ActionAnnotations() {
		super("annotations");
		this.setDescription("generates an arff file with a list of annotations with a defined set of features");
		
        this.options.addOption(OptionBuilder
        		.withArgName("annotation_features").hasArg()
                .withDescription("a file with a list of annotation features")
                .withLongOpt(ActionAnnotations.OPTION_ANNOTATION_FEATURE_LONG)
        		.isRequired()
                .create(ActionAnnotations.OPTION_ANNOTATION_FEATURE));
        this.options.addOption(CommonOptions.getTypesOption());
        this.options.addOption(CommonOptions.getOutputFileNameOption());
        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
	}

	@Override
	public void parseOptions(String[] args) throws ParseException, IOException {
		CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
        this.output_file = line.getOptionValue(CommonOptions.OPTION_OUTPUT_FILE);
        this.features_file = line.getOptionValue(ActionAnnotations.OPTION_ANNOTATION_FEATURE);
        String typesFile = line.getOptionValue(CommonOptions.OPTION_TYPES);
        if(typesFile != null){
            this.types = LinerOptions.getGlobal().parseTypes(typesFile);
        }
	}
	
	@Override
    public void run() throws Exception {
        List<String> annFeatures = parseAnnotationFeatures(this.features_file);
        AnnotationFeatureGenerator annGen = new AnnotationFeatureGenerator(annFeatures);

        AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(
    			this.input_file,
    			this.input_format);

        AnnotationArffWriter writer = WriterFactory.get().getArffAnnotationWriter(
        		this.output_file ,annFeatures);

        Document ps = reader.nextDocument();
        while ( ps != null ){
            for(AnnotationSet annotations: ps.getChunkings().values()){
                for(Annotation ann: annotations.chunkSet()){
                    if(!this.types.isEmpty()){
                        for(Pattern patt: this.types){
                            if(patt.matcher(ann.getType()).find()){
                                writer.writeAnnotation(ann.getType(), annGen.generate(ann));
                            }
                        }
                    }
                    else{
                        writer.writeAnnotation(ann.getType(), annGen.generate(ann));
                    }

                }
            }

            ps = reader.nextDocument();
        }
        writer.close();
        reader.close();
    }

   	/**
   	 *
   	 * @param path
   	 * @return
   	 * @throws IOException
   	 */
    private List<String> parseAnnotationFeatures(String path) throws IOException{
        List<String> annotationFeatures = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = br.readLine();
        while(line != null) {
            annotationFeatures.add(line);
            line = br.readLine();
        }
        return annotationFeatures;
    }

}
