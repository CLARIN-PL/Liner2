package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.AnnotationToPropertyChunker;
import org.ini4j.Ini;

/*
 * @author Jan Kocoń
 */

public class ChunkerFactoryItemAnnotationToProperty extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationToProperty() {
		super("annotation_to_property");
	}

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        return new AnnotationToPropertyChunker();
    }
}
