package g419.liner2.api.chunker.factory;


import g419.lib.cli.ParameterException;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.PropagateChunker;
import g419.corpus.Logger;

import org.ini4j.Ini;


public class ChunkerFactoryItemPropagate extends ChunkerFactoryItem {

	public ChunkerFactoryItemPropagate() {
		super("propagate");
	}
	
	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        Logger.log("--> Chunk propagation");

        String baseChunkerName = description.get("base-chunker");
        Chunker baseChunker = cm.getChunkerByName(baseChunkerName);
        if (baseChunker == null)
            throw new ParameterException("Propagate Chunker: undefined base chunker: " + baseChunkerName);
        return new PropagateChunker(baseChunker);
	}

}
