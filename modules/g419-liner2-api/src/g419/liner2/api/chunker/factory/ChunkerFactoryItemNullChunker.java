package g419.liner2.api.chunker.factory;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.NullChunker;

import org.ini4j.Ini;

/*
 * @author Adam Kaczmarek
 */

public class ChunkerFactoryItemNullChunker extends ChunkerFactoryItem {

	public ChunkerFactoryItemNullChunker() {
		super("null_chunker");
	}

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        return new NullChunker();
    }
}
