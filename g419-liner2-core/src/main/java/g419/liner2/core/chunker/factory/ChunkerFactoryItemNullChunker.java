package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.NullChunker;

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
