package g419.liner2.core.chunker.factory;


import org.ini4j.Ini;

import g419.liner2.core.chunker.Chunker;


public class ChunkerFactoryItemEnsemble extends ChunkerFactoryItem {

	public ChunkerFactoryItemEnsemble() {
		super("ensemble");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
	    return ChunkerFactory.getChunkerPipe(description.get("description"), cm);

	}
}
