package liner2;

import liner2.action.Action;
import liner2.action.ActionConvert;
//import liner2.action.ActionBatch;
//import liner2.action.ActionDict;
//import liner2.action.ActionDictStats;
//import liner2.action.ActionEval;
//import liner2.action.ActionEvalCV;
import liner2.action.ActionNull;
//import liner2.action.ActionPipe;
//import liner2.action.ActionTag;
//import liner2.action.ActionTrain;

public class Main {
    
    /**
     * Here the story begins.
     */
    public static void main(String[] args) throws Exception {
    	
    	try{
            LinerOptions.get().parse(args);
    	}
    	catch(Exception ex){
            LinerOptions.get().printHelp();
            System.out.println(ex.getMessage());
            return;
    	}
    	
    	try{
	    	Action action = null;
	    	String mode = LinerOptions.get().mode;
	
	    	// Create action object if recognized    	
//	    	if (mode.equals("eval")) {
//	          action = new ActionEval();
//	        } else if (mode.equals("evalcv")) {
//	            action = new ActionEvalCV();
//	        } else if (mode.equals("dict")) {
//	            action = new ActionDict();
//	        } else if (mode.equals("batch")) {
//	            action = new ActionBatch();
//	        } else if (mode.equals("pipe")) {
//	            action = new ActionPipe();
//	        } else if (mode.equals("dicts")) {
//	            action = new ActionDictStats();
//	        } else if (mode.equals("tag") ) {
//	            action = new ActionTag();
//	        } else 
	    	if (mode.equals("null") ) {
	            action = new ActionNull();
	        } 
	    	else if (mode.equals("convert") ) {
	            action = new ActionConvert();
	        } 
//            else if (mode.equals("train") ) {
//	            action = new ActionTrain();
//	        }
	
			LinerOptions.get().printConfigurationDescription();
	
	    	// If the action object was created then pass the arguments and execute it
	    	if (action == null){
	            System.out.println("Mode '" + mode + "' not found!");
	    	}else{    		
	            action.run();
	    	}
	    	
	    	// Finish feature generator if it was initialized
//	    	if (RegexLineTagParser.featureGenerator != null)
//	    		RegexLineTagParser.featureGenerator.finish();
	    }
    	catch(Exception ex){
//	    	if (RegexLineTagParser.featureGenerator != null)
//	    		RegexLineTagParser.featureGenerator.finish();
    		
	    	throw ex;
    	}
    }
        
    public static void log(String text){
    	Main.log(text, false);
    }
    
    public static void log(String text, boolean details){
    	if (LinerOptions.get().verboseDetails || (!details && LinerOptions.get().verbose) )
    		System.out.println(text);
    }
    
    public static void print(String text){
    	System.out.println(text);
    }

}
