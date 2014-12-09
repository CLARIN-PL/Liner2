package g419.crete.cli.action;

import g419.crete.api.Crete;
import g419.lib.cli.action.Action;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;

public class ActionPipe extends Action{

	public ActionPipe(){
		super("pipe");
        this.setDescription("processes data with given model");
	}

	@Override
	public void parseOptions(String[] args) throws Exception {
        CommandLine line = new GnuParser().parse(this.options, args);
        parseDefault(line);
	}
	
	/**
	 * Module entry function.
	 */
	public void run() throws Exception{
		Crete c = new  Crete("path.ini");
		System.out.println("Hello world");
	}
		
}
