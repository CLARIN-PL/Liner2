package g419.spatial;

import g419.lib.cli.action.Action;
import g419.spatial.action.ActionSpatial;
import g419.spatial.action.ActionTest;
import g419.spatial.action.ActionTest2;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.PropertyConfigurator;

public class Main {

	private Map<String, Action> actions = new HashMap<String, Action>();

	/**
	 * Module entry point.
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		
		Main main = new Main();
		main.registerAction(new ActionTest());
		//main.registerAction(new ActionWordnet());
		main.registerAction(new ActionSpatial());
		main.registerAction(new ActionTest2());
			
		if ( args.length == 0 ){
			main.printError("Mode not given.");
		}
		else{
			String name = args[0];
			Action action = main.getAction(name);
			if ( action == null ){
				main.printError(String.format("Mode '%s' does not exist.", name));
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
	            	e.printStackTrace();
	            }
			}
		}
	}
	
	public void printCredits(){
		System.out.println("*-----------------------------------------------------------------------------------------------*");
		System.out.println("*-----------------------------------------------------------------------------------------------*");
		System.out.println();
	}
	
	/**
	 * Prints error message with usage description
	 * @param error The message to be printed
	 */
	public void printError(String error){
		this.printCredits();
		System.out.println("[Error] " + error);
		System.out.println();
		System.out.println(" Use one of the following modes:");
		this.printActions();
		System.out.println();
		System.out.println("usage: ./spatial <mode> [options]");
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
