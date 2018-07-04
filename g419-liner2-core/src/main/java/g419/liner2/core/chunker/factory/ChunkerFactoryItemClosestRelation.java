package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.AnnotationToPropertyChunker;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.ClosestRelationChunker;
import org.ini4j.Ini;

/*
 * @author Jan Kocoń
 */

public class ChunkerFactoryItemClosestRelation extends ChunkerFactoryItem {

	public ChunkerFactoryItemClosestRelation() {
		super("closest-rel");
	}

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
	    String annotationFromPattern = description.get("annotation_from_pattern");
        String annotationToPattern = description.get("annotation_to_pattern");
        return new ClosestRelationChunker(annotationFromPattern, annotationToPattern);
    }
}
