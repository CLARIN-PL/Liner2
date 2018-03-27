package g419.liner2.cli.action;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Sentence;
import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;

/**
 * Prints corpora statistics.
 * 
 * @author Michał Marcińczuk
 *
 */
public class ActionStats extends Action{

    private String input_file = null;
    private String input_format = null;

	public ActionStats(){
		super("stats");
        this.setDescription("prints corpus statistics");
        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
	}

	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        this.input_file = line.getOptionValue(CommonOptions.OPTION_INPUT_FILE);
        this.input_format = line.getOptionValue(CommonOptions.OPTION_INPUT_FORMAT, "ccl");
	}
	
	/**
	 * Module entry function.
	 * 
	 * Loads annotation recognizers.
	 */
	public void run() throws Exception{
		AbstractDocumentReader reader = ReaderFactory.get().getStreamReader(this.input_file, this.input_format);
		Document document = null;
		int documents = 0;
		int sentences = 0;
		int tokens = 0;
		int annotations = 0;
		while ( (document = reader.nextDocument()) != null ){	
			documents++;
			for ( Sentence sentence : document.getSentences() ){
				sentences++;
				tokens += sentence.getTokenNumber();
				annotations += sentence.getChunks().size();
			}			
		}

		String line = "%20s: %10d";
		System.out.println(String.format(line, "Documents", documents));
		System.out.println(String.format(line, "Sentences", sentences));
		System.out.println(String.format(line, "Tokens", tokens));
		System.out.println(String.format(line, "Annotations", annotations));
	}
		
}
