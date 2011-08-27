package liner2;

import liner2.action.Action;
import liner2.action.ActionConvert;
import liner2.action.ActionTrain;
import liner2.action.ActionNull;
import liner2.action.ActionPipe;
import liner2.reader.FeatureGenerator;
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
            LinerOptions.get().parse(args);            
	    	String mode = LinerOptions.get().mode;
	    	action = Main.getAction(mode);	    	
	    	if (action == null){
	            throw new ParameterException(String.format("Mode '%s' not recognized", mode));
	    	}
	    	else{	    	
	    		action.run();
	    	}
    	}
    	catch(ParameterException ex){
            LinerOptions.get().printHelp();
            System.out.println(">> " + ex.getMessage() );            
    	}
    	catch(Exception ex){
            ex.printStackTrace();
            System.out.println(">> " + ex.getMessage());
    	}
    	finally{
    		FeatureGenerator.close();
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
    	if (LinerOptions.get().verboseDetails || (!details && LinerOptions.get().verbose) )
    		System.out.println(text);
    }
    
    /**
     * Create Action object according to given name.
     * @param mode
     * @return
     */
    private static Action getAction(String mode){
    	Action action = null;
    	
    	// Create action object if recognized    	
//    	if (mode.equals("eval")) {
//          action = new ActionEval();
//        } else if (mode.equals("evalcv")) {
//            action = new ActionEvalCV();
//        } else if (mode.equals("dict")) {
//            action = new ActionDict();
//        } else if (mode.equals("batch")) {
//            action = new ActionBatch();
//        } else if (mode.equals("dicts")) {
//            action = new ActionDictStats();
//        } else if (mode.equals("tag") ) {
//            action = new ActionTag();
//        } else 
    	if (mode.equals("null") ) {
            action = new ActionNull();
        } else if (mode.equals("convert") ) {
            action = new ActionConvert();
        } else if (mode.equals("pipe")) {
            action = new ActionPipe();
        } else if (mode.equals("train") ) {
            action = new ActionTrain();
        }

		LinerOptions.get().printConfigurationDescription();

    	return action;
    }
}
