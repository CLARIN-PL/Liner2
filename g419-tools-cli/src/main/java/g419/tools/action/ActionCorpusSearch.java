package g419.tools.action;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;

import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;

public class ActionCorpusSearch extends Action {
	
	public ActionCorpusSearch() {
		super("corpus-search");
		this.setDescription("wyszukuje zdania zawierające określone frazy");
        this.options.addOption(CommonOptions.getInputFileFormatOption());
        this.options.addOption(CommonOptions.getInputFileNameOption());
        this.options.addOption(Option.builder("q").longOpt("query").hasArg().argName("phrase")
        		.desc("fraza do znalezienia").required().required().build());
	}
	
	/**
	 * Parse action options
	 * @param arg0 The array with command line parameters
	 */
	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new DefaultParser().parse(this.options, args);
        parseDefault(line);
    }

	@Override
	public void run() throws Exception {

	}
	
}
