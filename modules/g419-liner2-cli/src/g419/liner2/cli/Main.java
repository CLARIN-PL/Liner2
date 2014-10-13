package g419.liner2.cli;

import g419.liner2.cli.action.Action;
import g419.liner2.cli.action.ActionAnnotations;
import g419.liner2.cli.action.ActionAgreement;
import g419.liner2.cli.action.ActionConvert;
import g419.liner2.cli.action.ActionEval;
import g419.liner2.cli.action.ActionFeatureSelection;
import g419.liner2.cli.action.ActionInteractive;
import g419.liner2.cli.action.ActionLearningCurve;
import g419.liner2.cli.action.ActionPipe;
import g419.liner2.cli.action.ActionTime;
import g419.liner2.cli.action.ActionTrain;

import java.util.HashMap;

import org.apache.commons.cli.ParseException;


/**
 * Run the module. 
 * 
 * @author Michał Marcińczuk
 * @author Maciej Janicki
 */
public class Main {
    	
	/** List of known actions. */ 
	private HashMap<String, Action> actions = new HashMap<String, Action>();
	
    /**
     * Here the story begins.
     */
    public static void main(String[] args) throws Exception {    	
    	
    	Main main = new Main();
    	main.registerAction(new ActionAnnotations());
    	main.registerAction(new ActionConvert());
    	main.registerAction(new ActionEval());
    	main.registerAction(new ActionInteractive());
    	main.registerAction(new ActionPipe());
    	main.registerAction(new ActionFeatureSelection());
    	main.registerAction(new ActionTime());
    	main.registerAction(new ActionTrain());
    	main.registerAction(new ActionAgreement());
    	main.registerAction(new ActionLearningCurve());
    	
    	if ( args.length == 0 ){
    		main.printCredits();
    		System.out.println("[Error] Mode not given. \n\nUse one of the following modes:");
    		main.printActions();
    		System.out.println();
    		System.out.println("usage: ./liner2-cli <mode> [options]");
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
        		System.err.println("usage: ./liner2-cli <mode> [options]");
        		System.out.println();
    		}
    		else{
    			try{
    				action.parseOptions(args);
    				action.run();
    			}
    			catch (ParseException e) {
    				main.printCredits();
    				System.out.println(String.format("[Options parse error] %s\n", e.getMessage()));
    				action.printOptions();
    				System.out.println();    				
				}
                catch (Exception e) {
                    System.out.println(e);
                }
    		}
    	}
    }
    
    public void printCredits(){
		System.out.println("*-----------------------------------------------------------------------------------------------*");
		System.out.println("* A framework for multitask sequence labeling, including: named entities, temporal expressions. *");
		System.out.println("*                                                                                               *");
		System.out.println("* Authors: Michał Marcińczuk (2010–2014), Michał Krautforst (2013-2014), Jan Kocoń (2014)       *");
		System.out.println("*          Dominik Piasecki (2013), Maciej Janicki (2011)                                       *");
		System.out.println("* Contact: michal.marcinczuk@pwr.wroc.pl                                                        *");
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
