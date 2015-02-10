package g419.crete.cli;

import g419.corpus.TerminateException;
import g419.crete.cli.action.ActionPipe;
import g419.lib.cli.action.Action;
import g419.crete.cli.action.ActionTrain;

import java.util.HashMap;

import org.apache.commons.cli.ParseException;

public class Main {
    	
	/** List of known actions. */ 
	private HashMap<String, Action> actions = new HashMap<String, Action>();
	
    /**
     * Here the story begins.
     */
    public static void main(String[] args) throws Exception {    	
    	
    	Main main = new Main();
    	main.registerAction(new ActionPipe());
    	main.registerAction(new ActionTrain());
    	
    	if ( args.length == 0 ){
    		main.printCredits();
    		System.out.println("[Error] Mode not given. \n\nUse one of the following modes:");
    		main.printActions();
    		System.out.println();
    		System.out.println("usage: ./crete-cli <mode> [options]");
    		System.out.println();
    	}
    	else{
    		String name = args[0];
    		Action action = main.getAction(name);
    		if ( action == null ){
        		main.printCredits();
    			System.out.println(String.format("[Error] Mode '%s' does not exist. \n\nUse one of the following modes:", name));
    			main.printActions();
    			System.out.println();
        		System.err.println("usage: ./crete-cli <mode> [options]");
        		System.out.println();
    		}
    		else{
    			try{
    				action.parseOptions(args);
    				action.run();
    			}
    			catch (TerminateException e){
    				System.out.println(e.getMessage());
    			}
    			catch (ParseException e) {
    				main.printCredits();
    				System.out.println(String.format("[Options parse error] %s\n", e.getMessage()));
    				action.printOptions();
    				System.out.println();    				
				}
                catch (Exception e) {
                	e.printStackTrace();
                    System.out.println(e);
                    e.printStackTrace();
                }
    		}
    	}
    }
    
    public void printCredits(){
		System.out.println("*-----------------------------------------------------------------------------------------------*");
		System.out.println("* todo                                                                                          *");
		System.out.println("*                                                                                               *");
		System.out.println("* Authors: todo                                                                                 *");
		System.out.println("* Contact: todo                                                                                 *");
		System.out.println("*                                                                                               *");
		System.out.println("*          G4.19 Research Group, Wrocław University of Technology                               *");
		System.out.println("*-----------------------------------------------------------------------------------------------*");
		System.out.println();
    }
    
    /**
     * Register a new action. The action must have unique name.
     * @param action -- object used to run the action.
     */
    public void registerAction(Action action){
    	this.actions.put(action.getName(), action);
    }
    
    /**
     * Prints a list of available actions.
     */
    public void printActions(){
    	int maxLength = 0;
    	for ( String name : this.actions.keySet())
    		maxLength = Math.max(maxLength, name.length());
    	
    	String lineFormat = " - %-" + maxLength + "s -- %s";
    	
    	String newLine = String.format("   %"+maxLength+"s    ", " ");
    	
		for (Action action : this.actions.values()){
			System.out.println(String.format(lineFormat, 
					action.getName(),  
					action.getDescription()).replaceAll("#", "\n" + newLine));
		}    	
    }
    
    public Action getAction(String name){
    	if ( this.actions.containsKey(name))
    		return this.actions.get(name);
    	else
    		return null;
    }
    
}
