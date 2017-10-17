package g419.liner2.core.chunker.factory;

import org.apache.log4j.Logger;
import org.ini4j.Ini;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.RuleRoadChunker;
import g419.liner2.core.tools.TrieDictNode;

/**
 * @author Michał Marcińczuk
 */
public class ChunkerFactoryItemRuleRoad extends ChunkerFactoryItem {

	public static String PARAM_ANNOTATION = "annotation";
	public static String PARAM_DICTIONARY = "dictionary";

	public ChunkerFactoryItemRuleRoad() {
		super("rule-road");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        TrieDictNode dict = null;
        String annotationType = null;
        
        String dictionaryPath = description.get(PARAM_DICTIONARY);
        if ( dictionaryPath == null ){
        	dict = new TrieDictNode(false);
        	Logger.getLogger(this.getClass()).error("Brak parametru 'dictionary' w opisie chunkera rule-road");
        }
        else{
        	dict = TrieDictNode.loadPlain(dictionaryPath);
        }
        
        annotationType = description.get(PARAM_ANNOTATION);
        
        return new RuleRoadChunker(annotationType, dict);
	}

}
