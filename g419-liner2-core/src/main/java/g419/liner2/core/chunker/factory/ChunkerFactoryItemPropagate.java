package g419.liner2.core.chunker.factory;


import g419.corpus.ConsolePrinter;
import g419.lib.cli.ParameterException;
import g419.liner2.core.chunker.Chunker;
import g419.liner2.core.chunker.PropagateChunker;
import org.ini4j.Ini;


public class ChunkerFactoryItemPropagate extends ChunkerFactoryItem {

	public ChunkerFactoryItemPropagate() {
		super("propagate");
	}
	
	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        ConsolePrinter.log("--> Chunk propagation");

        String baseChunkerName = description.get("base-chunker");
        Chunker baseChunker = cm.getChunkerByName(baseChunkerName);
        if (baseChunker == null)
            throw new ParameterException("Propagate Chunker: undefined base chunker: " + baseChunkerName);
        return new PropagateChunker(baseChunker);
	}

}
