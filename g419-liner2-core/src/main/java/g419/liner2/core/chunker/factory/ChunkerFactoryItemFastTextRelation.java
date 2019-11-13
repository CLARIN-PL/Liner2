package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.ClosestRelationChunker;
import g419.liner2.core.chunker.FastTextRelationChunker;
import org.ini4j.Ini;

/*
 * @author Jan Kocoń
 */

public class ChunkerFactoryItemFastTextRelation extends ChunkerFactoryItem {

	public ChunkerFactoryItemFastTextRelation() {
		super("fasttext-rel");
	}

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
	    String modelPath = description.get("model");
        String annotationPattern = description.get("annotation_pattern");
        boolean content = description.get("content").equals("true");
        return new FastTextRelationChunker(modelPath, annotationPattern, content);
    }
}
