package g419.liner2.api.chunker.factory;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.IkarAnnotationAdderChunker;

import org.ini4j.Ini;

/*
 * @author Adam Kaczmarek
 */

public class ChunkerFactoryItemAnnotationAdderOnlyZero extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationAdderOnlyZero() {
		super("annotation_adder_only_zero");
	}

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        return new IkarAnnotationAdderChunker(false, false, true);
    }
}
