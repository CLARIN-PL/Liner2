package g419.liner2.api.chunker.factory;


import g419.lib.cli.ParameterException;
import g419.liner2.api.chunker.AduChunker;
import g419.liner2.api.chunker.Chunker;
import g419.corpus.Logger;

import org.ini4j.Ini;


public class ChunkerFactoryItemAdu extends ChunkerFactoryItem {

	public ChunkerFactoryItemAdu() {
		super("adu");
	}

	@Override
	public Chunker getChunker(Ini.Section description, ChunkerManager cm) throws Exception {
        Logger.log("--> Automatic Dictionary Update chunker");
        String baseChunkerName = description.get("base-chunker");
        Chunker baseChunker = cm.getChunkerByName(baseChunkerName);
        if (baseChunker == null)
            throw new ParameterException("ADU Chunker: undefined base chunker: " + baseChunkerName);
        boolean one = Boolean.parseBoolean(description.get("one"));
        AduChunker chunker = new AduChunker();
        chunker.setSettings(baseChunker, one);
        return chunker;
	}
}
