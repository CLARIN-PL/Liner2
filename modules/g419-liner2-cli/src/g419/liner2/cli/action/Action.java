package g419.liner2.cli.action;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public abstract class Action {

	String name = null;
	String description = null;
	Options options = new Options();
	
	/**
	 * @param name -- nazwa trybu pracy.
	 */
	public Action(String name){
		this.name = name;
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
	public abstract void parseOptions(String[] args) throws ParseException;
	
	public Options getOptions(){
		return this.options;
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
        new HelpFormatter().printHelp(
        		String.format("./liner2-cli %s [options]", this.getName()), this.options);
	}
	
	abstract public void run() throws Exception;
	
}
