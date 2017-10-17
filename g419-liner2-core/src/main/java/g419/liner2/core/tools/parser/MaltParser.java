package g419.liner2.core.tools.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;

/**
 * Created with IntelliJ IDEA.
 * User: michal
 * Date: 8/5/13
 * Time: 10:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class MaltParser {

    private static HashMap<String, MaltParserService> parsers = new HashMap<String, MaltParserService>();

    MaltParserService parser;

    public MaltParser(String modelPath){
        if(isInitialized(modelPath)){
            parser = getParser(modelPath);
        }
        else{
            parser = addParser(modelPath);
        }

    }

    public DependencyStructure parseTokensToDependencyStructure(String [] dataForMalt) throws MaltChainedException {
        return parser.parse(dataForMalt);
    }

    public String [] parseTokens(String [] dataForMalt) throws MaltChainedException {
        if ( dataForMalt.length == 0 ){
           return new String[0];
        }
        else{
           return parser.parseTokens(dataForMalt);
        }
    }
    
    public void parse(MaltSentence sentence) throws MaltChainedException {
    	List<MaltSentenceLink> links = new ArrayList<MaltSentenceLink>();
    	String[] output = this.parseTokens(sentence.getMaltData());
    	sentence.setMaltData(output);
    	int i = 0;
    	for ( String line : output ){
    		String[] parts = line.split("\t");
    		int targetIndex = Integer.parseInt(parts[8]) - 1;
    		String relationType = parts[9];
    		links.add(new MaltSentenceLink(i++, targetIndex, relationType));
    	}
    	sentence.setLinks(links);
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

