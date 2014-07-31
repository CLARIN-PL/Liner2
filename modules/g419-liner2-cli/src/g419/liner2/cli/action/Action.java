package g419.liner2.cli.action;

import g419.liner2.api.LinerOptions;
import g419.liner2.cli.CommonOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

public abstract class Action {

	String name = null;
	String description = null;
	Options options = new Options();
	
	/**
	 * @param name -- nazwa trybu pracy.
	 */
	public Action(String name){
		this.name = name;
        this.options.addOption(CommonOptions.getVerboseOption());
	}

	/**
	 * Set the description of the action.
	 * @param description -- the action description
	 */
	public void setDescription(String description){
		this.description = description;
	}
	
	/**
	 * Parse an array with options. With the set of options is valid the return true.
	 * In other case return false. The getErrorMessage() can be used to obtain the 
	 * error message;
	 * @param args
	 * @return
	 */
	public abstract void parseOptions(String[] args) throws Exception;
	
	public Options getOptions(){
		return this.options;
	}

    protected void parseDefault(CommandLine line){
        if(line.hasOption(CommonOptions.OPTION_VERBOSE)){
            LinerOptions.getGlobal().verbose = true;
        }
    }
	
	/**
	 * Returns name of the mode.
	 * @return
	 */
	public String getName(){
		return this.name;
	}
	
	public String getDescription(){
		return this.description;
	}

	public void printOptions(){
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(98);
        formatter.printHelp(
        		String.format("./liner2-cli %s [options]", this.getName()), this.options);
	}
	
	abstract public void run() throws Exception;
	
}
