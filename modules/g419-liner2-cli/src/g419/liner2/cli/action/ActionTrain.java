package g419.liner2.cli.action;

import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;

/**
 * Train chunkers.
 * @author Michał Marcińczuk
 *
 */
public class ActionTrain extends Action{

	public ActionTrain(){
		super("train");

        this.options.addOption(CommonOptions.getModelFileOption());
	}

	@Override
	public void parseOptions(String[] args) throws ParseException {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
	}
	
	/**
	 * Module entry function.
	 * 
	 * Loads annotation recognizers.
	 */
	public void run() throws Exception{
        ChunkerFactory.loadChunkers(LinerOptions.getGlobal());
	}
		
}
