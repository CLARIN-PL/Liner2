package g419.liner2.core.chunker.factory;

import g419.liner2.core.chunker.ChunkRelChunker;
import g419.liner2.core.chunker.Chunker;

import org.ini4j.Profile.Section;

public class ChunkerFactoryItemChunkRel extends ChunkerFactoryItem {

	public ChunkerFactoryItemChunkRel() {
		super("chunkrel");
	}

	@Override
	public Chunker getChunker(Section description, ChunkerManager cm) throws Exception {
		return new ChunkRelChunker(description.get("python_path"), description.get("chunkrel_path"), description.get("chunkrel_config_path"), description.get("chunkrel_model_path"), description.get("tmp_output_folder"));
	}

}
