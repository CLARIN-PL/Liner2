package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.AnnotationToPropertyChunker;
import org.ini4j.Ini;

/*
 * @author Jan Kocoń
 */

public class ChunkerFactoryItemAnnotationToProperty extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationToProperty() {
		super("ann-to-prop");
	}

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
	    String propertyPattern = description.get("property_pattern");
        String annotationPattern = description.get("annotation_pattern");
        return new AnnotationToPropertyChunker(propertyPattern, annotationPattern);
    }
}
