package g419.tools;

import g419.corpus.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by michal on 11/5/14.
 */
public abstract class Tool {

    String name = null;
    String description = null;
    Options options = new Options();

    /**
     * @param name -- nazwa trybu pracy.
     */
    public Tool(String name){
        this.name = name;
//        this.options.addOption(CommonOptions.getVerboseOption());
    }

    /**
     * Set the description of the tool.
     * @param description -- the tool description
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
//        if(line.hasOption(CommonOptions.OPTION_VERBOSE)){
//            Logger.verbose = true;
//        }
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
                String.format("./tools %s [options]", this.getName()), this.options);
    }

    abstract public void run() throws Exception;
}
