package g419.liner2.api.chunker.factory;

import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.IobberChunker;

import org.ini4j.Profile.Section;

public class ChunkerFactoryItemIobber extends ChunkerFactoryItem {

	public ChunkerFactoryItemIobber() {
		super("iobber");
	}

	@Override
	public Chunker getChunker(Section description, ChunkerManager cm) throws Exception {
		return new IobberChunker();
	}

}
