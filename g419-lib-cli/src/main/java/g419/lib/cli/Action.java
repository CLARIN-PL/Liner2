package g419.lib.cli;

import com.google.common.collect.Sets;
import g419.corpus.ConsolePrinter;

import org.apache.commons.cli.*;

import java.util.HashSet;

public abstract class Action {

	final protected String name;
	protected String description = "";
	protected String example = "";
	protected Options options = new Options();
	protected HashSet<String> multipleValueOptions = Sets.newHashSet();
	
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
     *
     * @param example
     */
	public void setExample(String example){
		this.example = example;
    }

	/**
	 * Parse an array with options. With the set of options is valid the return true.
	 * In other case return false. The getErrorMessage() can be used to obtain the 
	 * error message;
	 * @param args
	 */
	public void parseOptions(final String[] args) throws Exception{
		final CommandLine line = new DefaultParser().parse(options, args);
		checkOptionRepetition(line);
		if(line.hasOption(CommonOptions.OPTION_VERBOSE)){
			ConsolePrinter.verbose = true;
		}
		parseOptions(line);
	}

	public abstract void parseOptions(final CommandLine line) throws Exception;

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

    /**
     * Get an example of the output produced by the action.
     * @return
     */
	public String getExample(){
	    return this.example;
    }

	public void printOptions(){
		StringBuilder footer = new StringBuilder();
		if ( this.getExample() != null && this.getExample().length() > 0 ){
			footer.append("\n");
			footer.append("Example:\n");
			footer.append("--------\n");
			footer.append(this.getExample());
		}

		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(98);
        formatter.printHelp(
        		String.format("./liner2-cli %s [options]",
						this.getName()), this.getDescription(), this.getOptions(), footer.toString());
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
