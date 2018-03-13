package g419.spatial.action;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.spatial.tools.SpatialResources;
import g419.toolbox.sumo.Sumo;
import g419.toolbox.sumo.WordnetToSumo;
import org.apache.commons.io.IOUtils;

public class ActionRegions extends Action {
	
	private final static String OPTION_FILENAME_LONG = "filename";
	private final static String OPTION_FILENAME = "f";
	
	private String filename = null;
	private String inputFormat = null;

	public ActionRegions() throws IOException {
		super("regions");
		this.setDescription("Finds and prints patterns PREP REGION OBJECT, where REGION is a word from a dictionary of regions and OBJECT is a word which is linked with OBJECT concept from SUMO.");
		this.setExample(IOUtils.toString(this.getClass().getResource("ActionRegions.example.txt"), Charsets.UTF_8));
		this.options.addOption(this.getOptionInputFilename());		
		this.options.addOption(CommonOptions.getInputFileFormatOption());
	}
	
	/**
	 * Create Option object for input file name.
	 * @return Object for input file name parameter.
	 */
	private Option getOptionInputFilename(){
		return Option.builder(ActionRegions.OPTION_FILENAME).hasArg().argName("FILENAME").required()
						.desc("path to the input file").longOpt(OPTION_FILENAME_LONG).build();			
	}

	/**
	 * Parse action options
	 * @param args The array with command line parameters
	 */
	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        this.filename = line.getOptionValue(ActionRegions.OPTION_FILENAME);
        this.inputFormat = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT);
    }

	@Override
	public void run() throws Exception {
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.filename, this.inputFormat);
		Document document = null;
		Set<String> regions = SpatialResources.getRegions();
		WordnetToSumo wordnetToSumo = new WordnetToSumo();
		Sumo sumo = new Sumo();

		while ( ( document = reader.nextDocument() ) != null ){											
			for (Paragraph paragraph : document.getParagraphs()){
				for (Sentence sentence : paragraph.getSentences()){
					List<Token> tokens = sentence.getTokens();
					for ( int i = 0; i+2<tokens.size(); i++){						
						if ( tokens.get(i).getDisambTag().getPos().equals("prep")
								&& regions.contains(tokens.get(i+1).getDisambTag().getBase()) ){
							String base = tokens.get(i+2).getDisambTag().getBase();
							Set<String> concepts = wordnetToSumo.getLemmaConcepts(base);
							Set<String> superConcepts = new HashSet<String>();
							if ( concepts != null ){
								for ( String concept : concepts ){
									superConcepts.addAll(sumo.getSuperclasses(concept.toLowerCase()));
								}
							}
							if ( superConcepts.contains("object") ){
								System.out.println();
								System.out.println(String.format("BASE: %s %s %s", 
										tokens.get(i).getDisambTag().getBase(),
										tokens.get(i+1).getDisambTag().getBase(),
										tokens.get(i+2).getDisambTag().getBase()));
								System.out.println(String.format("ORTH: %s %s %s", 
										tokens.get(i).getOrth(),
										tokens.get(i+1).getOrth(),
										tokens.get(i+2).getOrth()));
								System.out.println(String.format("SUMO:"));
								for ( String concept : concepts ){
									System.out.println(String.format("  * %s %s %s",
											tokens.get(i).getDisambTag().getBase(),
											tokens.get(i+1).getDisambTag().getBase(),
											concept));
								}								
							}
						} 
					}
				}
			}		
		}
			
		reader.close();
	}
		
}
