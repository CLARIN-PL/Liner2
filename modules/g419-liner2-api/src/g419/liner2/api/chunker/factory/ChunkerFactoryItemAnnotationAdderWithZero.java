package g419.liner2.api.chunker.factory;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.IkarAnnotationAdderChunker;

import org.ini4j.Ini;

/*
 * @author Adam Kaczmarek
 */

public class ChunkerFactoryItemAnnotationAdderWithZero extends ChunkerFactoryItem {

	public ChunkerFactoryItemAnnotationAdderWithZero() {
		super("annotation_adder_with_zero");
	}

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        return new IkarAnnotationAdderChunker(true, true, true);
    }
}
