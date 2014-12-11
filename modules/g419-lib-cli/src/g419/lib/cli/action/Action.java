package g419.lib.cli.action;

import g419.corpus.Logger;
import g419.lib.cli.CommonOptions;
import g419.liner2.api.tools.ParameterException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.HashSet;

public abstract class Action {

	protected String name = null;
	String description = null;
	protected Options options = new Options();
	protected HashSet<String> multipleValueOptions = new HashSet<String>();
	
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
		checkOptionRepetition(line);
        if(line.hasOption(CommonOptions.OPTION_VERBOSE)){
            Logger.verbose = true;
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

	public void checkOptionRepetition(CommandLine line){
		try {
			HashSet<String> argNames = new HashSet<String>();
			for(Option opt: line.getOptions()){
				String argName = opt.getOpt();
				if(!multipleValueOptions.contains(argName) && argNames.contains(argName)){
					throw new ParameterException("Repeated argument: " + argName);
				}
				else{
					argNames.add(argName);
				}
			}
		} catch (ParameterException e) {
			System.out.println(e);
			System.exit(1);
		}
	}
	
}
