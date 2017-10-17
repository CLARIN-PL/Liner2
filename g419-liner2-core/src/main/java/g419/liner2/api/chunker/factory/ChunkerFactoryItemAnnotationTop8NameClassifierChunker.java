package g419.liner2.api.chunker.factory;


import org.ini4j.Ini;

import g419.lib.cli.ParameterException;
import g419.liner2.api.chunker.AnnotationTop8NameClassifierChunker;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.tools.TypedDictionary;

public class ChunkerFactoryItemAnnotationTop8NameClassifierChunker extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationTop8NameClassifierChunker() {
		super("top8classifier");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        String inputClassifier = description.get("base-chunker");
        Chunker baseChunker = null;

		if ( inputClassifier != null ){
			baseChunker = cm.getChunkerByName(inputClassifier);
			if (baseChunker == null){
				throw new ParameterException("Undefined base chunker: " + inputClassifier);
			}
		}
		
		TypedDictionary indicatorTypes = new TypedDictionary();
		if ( description.containsKey("indicators") ){
			indicatorTypes = TypedDictionary.loadFromFile(description.get("indicators"));
		}
				
		return new AnnotationTop8NameClassifierChunker(baseChunker, indicatorTypes);
	}

}
