package liner2;

import org.apache.commons.cli.UnrecognizedOptionException;

import liner2.action.*;
import liner2.tools.ParameterException;

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
     * Messages are print to std.out only with -verbose parameter.
     * @param text
     */
    public static void log(String text){ 
    	Main.log(text, false);
    }
    
    /**
     * Messages are print to std.out only with -verbose or -verboseDetails parameter.
     * @param text
     * @param details
     */
    public static void log(String text, boolean details){
    	if (LinerOptions.getGlobal().verboseDetails || (!details && LinerOptions.getGlobal().verbose) )
    		System.out.println(text);
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
        } else if (mode.equals("evalcv")) {
            action = new ActionEvalCV();
        } else if (mode.equals("evalcvbatch")) {
            action = new ActionEvalCvBatch();
        } else if (mode.equals("interactive")) {
            action = new ActionInteractive();
        } else if (mode.equals("batch")){
            action = new ActionBatch(); 
    	} else if (mode.equals("null") ) {
            action = new ActionNull();
        } else if (mode.equals("convert") ) {
            action = new ActionConvert();
        } else if (mode.equals("daemon") ) {
            action = new ActionDaemon();
        } else if (mode.equals("pipe")) {
            action = new ActionPipe();
        } else if (mode.equals("train") ) {
            action = new ActionTrain();
        }

		LinerOptions.getGlobal().printConfigurationDescription();

    	return action;
    }
}
