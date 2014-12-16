package g419.liner2.api.chunker.factory;

import g419.liner2.api.chunker.ChunkRelChunker;
import g419.liner2.api.chunker.Chunker;

import org.ini4j.Profile.Section;

public class ChunkerFactoryItemChunkRel extends ChunkerFactoryItem {

	public ChunkerFactoryItemChunkRel() {
		super("chunkrel");
	}

	@Override
	public Chunker getChunker(Section description, ChunkerManager cm) throws Exception {
		return new ChunkRelChunker();
	}

}
