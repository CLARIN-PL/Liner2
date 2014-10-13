package g419.liner2.api.chunker.factory;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.MinosChunker;
import org.ini4j.Ini;

/*
 * @author Michał Marcińczuk
 */

public class ChunkerFactoryItemMinos extends ChunkerFactoryItem {

	public ChunkerFactoryItemMinos() {
		super("minos");
	}

    @Override
    public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        return new MinosChunker();
    }
}
