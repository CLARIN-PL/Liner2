package g419.lib.cli;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.lang3.StringUtils;

import g419.corpus.TerminateException;
import g419.corpus.io.UnknownFormatException;

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
                catch (UnknownFormatException e){
                	System.err.println("Error: " + e.getMessage());
                }
                catch (TerminateException e){
                    System.err.println("Error: " + e.getMessage());
                }
                catch (ParseException | MissingOptionException | UnrecognizedOptionException e) {
                	System.out.println(this.credits);
                    System.out.println();
                    System.out.println(String.format("[Option error] %s\n", e.getMessage()));
                    tool.printOptions();
                    System.out.println();
                }
                catch (Exception e) {
                	System.err.println("Error: " + e.getMessage());
                	System.err.println(StringUtils.repeat("-", 60));
                    e.printStackTrace();
                    System.err.println(StringUtils.repeat("-", 60));
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
     */
    public void add(Action action){
        this.actions.put(action.getName(), action);
    }
    
    /**
     * Prints a list of available actions.
     */
    public void printTools(){
        int maxLength = 1;
        for ( String name : this.actions.keySet()){
            maxLength = Math.max(maxLength, name.length());
        }

        String lineFormat = " - %-" + maxLength + "s -- %s";

        String newLine = String.format("   %"+maxLength+"s    ", " ");

        Set<String> actionNames = new TreeSet<String>();
        for (Action tool : this.actions.values()){
        	actionNames.add(tool.getName());
        }
        
        for (String name : actionNames){
        	Action tool = this.actions.get(name);
        	List<String> lines = this.splitIntoLines(tool.getDescription(), 90 - maxLength);
            System.out.println(String.format(lineFormat, tool.getName(), lines.get(0)) );
            for ( int i=1; i<lines.size(); i++){
            	System.out.println(StringUtils.repeat(" ", maxLength+7) + lines.get(i));
            }
        }
    }
    
    /**
     * Dzieli tekst po spacjach na linie nie dłuższe niż maxLength znaków.
     * @param text -- tekst do podziału
     * @param maxLength -- maksymalna długość linii
     * @return tekst podzielony na linie
     */
    public List<String> splitIntoLines(String text, int maxLength){
    	if ( text == null ){
    		text = "brak opisu";
    	}
    	List<String> lines = new ArrayList<String>();
    	int i = -1;
    	int lineStarts = 0;
    	int lastPossibleLineEnd = 0;
    	while ( ++i < text.length() ){
    		if ( i - lineStarts >= maxLength ){
    			// Trzeba uciąc obecny tekst
    			if ( lastPossibleLineEnd == lineStarts){
    				System.out.println("cut");
    			}
    			else{
    				lines.add(text.substring(lineStarts, lastPossibleLineEnd));
    				lineStarts = lastPossibleLineEnd+1;
    				lastPossibleLineEnd = lineStarts;
    			}
    		}
    		if ( text.charAt(i) == ' ' ){
    			lastPossibleLineEnd = i;
    		}
    	}
    	if ( lineStarts < i ){
    		lines.add(text.substring(lineStarts, i));
    	}
    	return lines;
    }
    
}
