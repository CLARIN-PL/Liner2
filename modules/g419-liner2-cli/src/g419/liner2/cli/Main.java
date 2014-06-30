package g419.liner2.cli;

import g419.liner2.api.LinerOptions;
import g419.liner2.api.tools.ParameterException;
import g419.liner2.cli.action.Action;
import g419.liner2.cli.action.ActionConvert;
import g419.liner2.cli.action.ActionEval;
import g419.liner2.cli.action.ActionEvalCV;
import g419.liner2.cli.action.ActionInteractive;
import g419.liner2.cli.action.ActionNull;
import g419.liner2.cli.action.ActionPipe;
import g419.liner2.cli.action.ActionTime;
import g419.liner2.cli.action.ActionTrain;

import org.apache.commons.cli.UnrecognizedOptionException;


/**
 * Run the module. 
 * 
 * @author Michał Marcińczuk
 * @author Maciej Janicki
 */
public class Main {
    
    /**
     * Here the story begins.
     */
    public static void main(String[] args) throws Exception {

    	Action action = null;
 
    	try{
            LinerOptions.getGlobal().parse(args);
	    	String mode = LinerOptions.getGlobal().mode;
	    	action = Main.getAction(mode);	    	
	    	if (action == null){
	            throw new ParameterException(String.format("Mode '%s' not recognized", mode));
	    	}
	    	else{	    	
	    		action.run();
	    	}
    	}
    	catch (UnrecognizedOptionException ex) {
            LinerOptions.getGlobal().printHelp();
            System.out.println(">> " + ex.getMessage() );            
		}
    	catch(ParameterException ex){
            LinerOptions.getGlobal().printHelp();
            System.out.println(">> " + ex.getMessage() );            
    	}
    	catch(Exception ex){
            ex.printStackTrace();
            System.out.println(">> " + ex.getMessage());
    	}
    }
    
    /**
     * Create Action object according to given name.
     * @param mode
     * @return
     */
    private static Action getAction(String mode){
    	Action action = null;
    	
    	if (mode.equals("eval")) {
          action = new ActionEval();
        } else if (mode.equals("interactive")) {
            action = new ActionInteractive();
    	} else if (mode.equals("null") ) {
            action = new ActionNull();
        } else if (mode.equals("convert") ) {
            action = new ActionConvert();
        } else if (mode.equals("pipe")) {
            action = new ActionPipe();
        } else if (mode.equals("train") ) {
            action = new ActionTrain();
        } else if (mode.equals("time") ) {
            action = new ActionTime();
        }

		LinerOptions.getGlobal().printConfigurationDescription();

    	return action;
    }
}
