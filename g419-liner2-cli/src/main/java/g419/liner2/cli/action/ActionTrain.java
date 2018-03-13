package g419.liner2.cli.action;

import g419.lib.cli.Action;
import g419.lib.cli.CommonOptions;
import g419.liner2.core.LinerOptions;
import g419.liner2.core.chunker.factory.ChunkerManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

/**
 * Train chunkers.
 * @author Michał Marcińczuk
 *
 */
public class ActionTrain extends Action{

	public ActionTrain(){
		super("train");
        this.setDescription("trains chunkers");
        this.options.addOption(CommonOptions.getModelFileOption());
	}

	@Override
	public void parseOptions(final CommandLine line) throws Exception {
        LinerOptions.getGlobal().parseModelIni(line.getOptionValue(CommonOptions.OPTION_MODEL));
	}
	
	/**
	 * Module entry function.
	 * 
	 * Loads annotation recognizers.
	 */
	public void run() throws Exception{
        ChunkerManager cm = new ChunkerManager(LinerOptions.getGlobal());
        cm.loadChunkers();
	}
		
}
