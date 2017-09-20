package g419.liner2.api.chunker.factory;


import org.ini4j.Ini;

import g419.lib.cli.ParameterException;
import g419.liner2.api.chunker.AnnotationTop8ClassifierChunker;
import g419.liner2.api.chunker.Chunker;

public class ChunkerFactoryItemAnnotationTop8ClassifierChunker extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationTop8ClassifierChunker() {
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

		return new AnnotationTop8ClassifierChunker(baseChunker);
	}

}
