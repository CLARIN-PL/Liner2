package g419.liner2.api.chunker.factory;


import g419.liner2.api.chunker.BsnlpFixChunker;
import g419.liner2.api.chunker.Chunker;

import org.ini4j.Ini;


public class ChunkerFactoryItemBsnlpFixChunker extends ChunkerFactoryItem {

	public ChunkerFactoryItemBsnlpFixChunker() {
		super("bsnlp-fix");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        return new BsnlpFixChunker();

	}

}
