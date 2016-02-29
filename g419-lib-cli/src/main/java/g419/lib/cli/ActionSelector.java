package g419.lib.cli;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.UnrecognizedOptionException;

import g419.corpus.TerminateException;

public class ActionSelector {

    /** List of known actions. */
    private HashMap<String, Action> actions = new HashMap<String, Action>();

    private String credits = null;
    private String cliScriptName = null;
    
    public ActionSelector(String cliScriptName){
    	this.cliScriptName = cliScriptName;
    }
    
    public void setCredits(String credits){
    	this.credits = credits;
    }
    
    public void run(String[] args){
        if ( args.length == 0 ){
            System.out.println(this.credits);
            System.out.println();
            System.out.println("[Error] Tool not given. \n\nUse one of the following tools:");
            this.printTools();
            System.out.println();
            System.out.println(String.format("usage: %s [action] [options]", this.cliScriptName));
            System.out.println();
        }
        else{
            String name = args[0];
            Action tool = this.actions.get(name);
            if ( tool == null ){
            	System.out.println(this.credits);
                System.out.println();
                System.out.println(String.format("[Error] Tool '%s' does not exist. \n\nUse one of the following tools:", name));
                this.printTools();
                System.out.println();
                System.out.println(String.format("usage: %s [action] [options]", this.cliScriptName));
                System.out.println();
            }
            else{
                try{
                    tool.parseOptions(args);
                    tool.run();
                }
                catch (TerminateException e){
                    System.out.println(e.getMessage());
                }
                catch (ParseException | MissingOptionException | UnrecognizedOptionException e) {
                	System.out.println(this.credits);
                    System.out.println();
                    System.out.println(String.format("[Option error] %s\n", e.getMessage()));
                    tool.printOptions();
                    System.out.println();
                }
                catch (Exception e) {
                    System.out.println(e);
                    e.printStackTrace();
                }
            }
        }    	
    }
    
    /**
     * Dodaje instancje klas rozszerzające klasę Action znajdujących się we wskazanym pakiecie.
     * @param packageName
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IOException
     */
    public void addActions(String packageName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException{
        for ( Action action : ActionFinder.find(packageName) ){
   			this.add( action );
    	}    	
    }

    /**
     * Register a new action. The action must have unique name.
     * @param tool -- object used to run the tool.
     */
    public void add(Action action){
        this.actions.put(action.getName(), action);
    }
    
    /**
     * Prints a list of available actions.
     */
    public void printTools(){
        int maxLength = 1;
        for ( String name : this.actions.keySet())
            maxLength = Math.max(maxLength, name.length());

        String lineFormat = " - %-" + maxLength + "s -- %s";

        String newLine = String.format("   %"+maxLength+"s    ", " ");

        Set<String> actionNames = new TreeSet<String>();
        for (Action tool : this.actions.values()){
        	actionNames.add(tool.getName());
        }
        
        for (String name : actionNames){
        	Action tool = this.actions.get(name);
            System.out.println(String.format(lineFormat,
                    tool.getName(),
                    tool.getDescription()).replaceAll("#", "\n" + newLine));
        }
    }
    
}
