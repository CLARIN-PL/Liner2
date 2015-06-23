package g419.liner2.api.tools;

import java.io.File;
import java.util.HashMap;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/5/13
 * Time: 10:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class Maltparser {

    private static HashMap<String, MaltParserService> parsers = new HashMap<String, MaltParserService>();

    MaltParserService parser;

    public Maltparser(String modelPath){
        if(isInitialized(modelPath)){
            parser = getParser(modelPath);
        }
        else{
            parser = addParser(modelPath);
        }

    }

    public String [] parseTokens(String [] dataForMalt) throws MaltChainedException {
        return parser.parseTokens(dataForMalt);
    }

    private static MaltParserService addParser(String modelPath){
        try {
            MaltParserService parser =  new MaltParserService();
            File modelFile = new File(modelPath);
            parser.initializeParserModel(String.format("-c %s -m parse -w %s", modelFile.getName(), modelFile.getParent()));
            parsers.put(modelPath, parser);
            return parser;
        } catch (MaltChainedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static MaltParserService getParser(String modelPath){
        return parsers.get(modelPath);
    }

    private static boolean isInitialized(String modelPath){
        return parsers.containsKey(modelPath);
    }
}

