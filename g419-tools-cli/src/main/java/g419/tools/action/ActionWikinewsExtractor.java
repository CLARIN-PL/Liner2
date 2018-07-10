package g419.tools.action;

import g419.lib.cli.Action;
import g419.tools.utils.WikinewsExtractor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.log4j.Logger;

import java.io.File;

public class ActionWikinewsExtractor extends Action {
	
	private final String OPTION_WIKINEWS_DUMP_LONG = "wikinews-dump";
	private final String OPTION_WIKINEWS_DUMP = "w";

	private final String OPTION_OUTPUT_LONG = "output";
	private final String OPTION_OUTPUT = "o";

	private String wikinewsFilename = null;
	private String outputFolder = null;
	
	public ActionWikinewsExtractor() {
		super("wikinews-extractor");
		this.setDescription("konwertuje dump Wikinews do postaci zbioru plików txt");
        this.options.addOption(Option.builder(OPTION_WIKINEWS_DUMP).longOpt(OPTION_WIKINEWS_DUMP_LONG).hasArg().argName("phrase")
        		.desc("fraza do znalezienia").required().build());
        this.options.addOption(Option.builder(OPTION_OUTPUT).longOpt(OPTION_OUTPUT_LONG).hasArg().argName("folder")
        		.desc("katalog, do którego zostaną zapisane pliki").required().build());
	}
	
	/**
	 * Parse action options
	 * @param line The array with command line parameters
	 */
	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        this.wikinewsFilename = line.getOptionValue(OPTION_WIKINEWS_DUMP_LONG);
        this.outputFolder = line.getOptionValue(OPTION_OUTPUT);
    }

	@Override
	public void run() throws Exception {
		Logger.getLogger(this.getClass()).info("Wikinews dump: " + this.wikinewsFilename);
		
		File wikinewsFile = new File(this.wikinewsFilename);
		if ( !wikinewsFile.exists() ){
			System.err.println(String.format("Plik '%s' nie istnieje", wikinewsFile));
			return;
		}
		
		File outputFolder = new File(this.outputFolder);
		if ( !outputFolder.exists() ){
			outputFolder.mkdirs();
		}
		
		WikinewsExtractor.extract(this.wikinewsFilename, this.outputFolder);
	}

}
