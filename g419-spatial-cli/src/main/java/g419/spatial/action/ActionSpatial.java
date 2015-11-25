package g419.spatial.action;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.structure.SpatialRelation;
import g419.spatial.tools.SpatialRelationRecognizer;

public class ActionSpatial extends Action {
	
	private final static String OPTION_FILENAME_LONG = "filename";
	private final static String OPTION_FILENAME = "f";
	
	private String filename = null;
	private String inputFormat = null;
	

	/**
	 * 
	 */
	public ActionSpatial() {
		super("spatial");
		this.setDescription("recognize spatial relations");
		this.options.addOption(this.getOptionInputFilename());		
		this.options.addOption(CommonOptions.getInputFileFormatOption());		
	}
	
	/**
	 * Create Option object for input file name.
	 * @return Object for input file name parameter.
	 */
	private Option getOptionInputFilename(){
		return Option.builder(ActionSpatial.OPTION_FILENAME).hasArg().argName("FILENAME").required()
						.desc("path to the input file").longOpt(OPTION_FILENAME_LONG).build();			
	}

	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
        this.filename = line.getOptionValue(ActionSpatial.OPTION_FILENAME);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);				
		SpatialRelationRecognizer recognizer = new SpatialRelationRecognizer();
				
		Document document = null;
		while ( ( document = reader.nextDocument() ) != null ){					
			System.out.println("=======================================");
			System.out.println("Document: " + document.getName());
			System.out.println("=======================================");
			
			for (Paragraph paragraph : document.getParagraphs()){
				for (Sentence sentence : paragraph.getSentences()){
					
					List<SpatialRelation> relations = recognizer.recognize(sentence);
					
					for ( SpatialRelation rel : relations ){
						System.out.println(rel.toString());
					}
				}
			}		
		}
			
		reader.close();
	}
		
}
