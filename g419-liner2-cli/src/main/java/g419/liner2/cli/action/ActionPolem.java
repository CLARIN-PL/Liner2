package g419.liner2.cli.action;

import g419.lib.cli.Action;
import g419.liner2.core.chunker.factory.SharedLibUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import g419.polem.CascadeLemmatizer;

public class ActionPolem extends Action {

	public ActionPolem() {
		super("polem");
		this.setDescription("test lemmatization module powered by Polem");
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
		//System.load(SharedLibUtils.getPolemLibPath());
		System.load("/usr/local/lib/libpolemJava-dev.so");
		CascadeLemmatizer.assembleLemmatizer();
	}
	

}
